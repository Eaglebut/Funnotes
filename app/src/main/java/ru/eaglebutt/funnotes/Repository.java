package ru.eaglebutt.funnotes;

import android.content.Context;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.eaglebutt.funnotes.API.APIService;
import ru.eaglebutt.funnotes.API.APIServiceConstructor;
import ru.eaglebutt.funnotes.DB.MainDB;
import ru.eaglebutt.funnotes.Model.AllUsersResponseData;
import ru.eaglebutt.funnotes.Model.Event;
import ru.eaglebutt.funnotes.Model.User;

public class Repository {
    private static MainDB db = null;
    private static APIService apiService = null;
    private static Repository INSTANCE = null;

    private ObservableField<User> observableUser = new ObservableField<>();
    private ObservableList<Event> observableEventList = new ObservableArrayList<>();
    private ObservableField<String> observableString = new ObservableField<>();


    public ObservableField<String> getObservableString() {
        return observableString;
    }

    public void setObservableString(ObservableField<String> observableString) {
        this.observableString = observableString;
    }


    public void setObservableUser(ObservableField<User> observableUser) {
        this.observableUser = observableUser;
    }

    private ObservableBoolean isLoading = new ObservableBoolean(false);

    public ObservableBoolean getIsLoading() {
        return isLoading;
    }

    public void setIsLoading(ObservableBoolean isLoading) {
        this.isLoading = isLoading;
    }

    private Repository(Context context){
        db = MainDB.get(context);
        apiService = APIServiceConstructor.createService(APIService.class);
    }

    public static Repository getInstance(Context context) {
        if(INSTANCE == null){
            INSTANCE = new Repository(context);
        }
        return INSTANCE;
    }

    public void getUser(String email, String password)  {
        isLoading.set(true);
        Call<User> userCall = apiService.getUser(email, password);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                isLoading.set(false);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        User user = response.body();
                        user.setSynchronized(true);
                        new InsertUserIntoDBThread(user).start();
                        observableUser.set(user);
                    }
                }
                new GetUserFromDBThread(observableUser).start();
                updateObservableString();
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                isLoading.set(false);
                new GetUserFromDBThread(observableUser).start();
            }
        });
    }

    private void updateObservableString(){
        StringBuilder builder = new StringBuilder();
        builder.append(observableUser.get().toString())
                .append("\n");
        for (Event event: observableEventList) {
            builder.append(event.toString());
        }
        observableString.set(builder.toString());
    }

    public void addUser(User user){
        isLoading.set(true);
        observableUser.set(null);
        Call<Void> putUser = apiService.putUser(user);
        putUser.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()){
                    user.setSynchronized(true);
                    new InsertUserIntoDBThread(user).start();
                    observableUser.set(user);
                }
                else {
                    observableUser.set(null);
                }
                updateObservableString();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
                observableUser.set(null);
            }
        });
    }

    public void deleteUser(String email, String password){
        isLoading.set(true);
        Call<Void> deleteUser = apiService.deleteUser(email, password);
        deleteUser.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()){
                    new DeleteUserFromDBThread().start();
                    observableUser.set(null);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }


    public void updateUser(String email, String password, User user){
        user.setId(observableUser.get().getId());
        isLoading.set(true);
        user.setSynchronized(false);
        observableUser.set(user);
        new UpdateUserInDBThread(user).start();
        Call<Void> updateUser = apiService.updateUser(email, password, user);
        updateUser.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()){
                    user.setSynchronized(true);
                    new UpdateUserInDBThread(user);
                    observableUser.set(user);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public void getUserAndEvents(String email, String password){
        isLoading.set(true);
        Call<AllUsersResponseData> userCall = apiService.getAllUserData(email, password);
        userCall.enqueue(new Callback<AllUsersResponseData>() {
            @Override
            public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {
                isLoading.set(false);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        User user = response.body().getUser();
                        user.setSynchronized(true);
                        new InsertUserIntoDBThread(user).start();
                        observableUser.set(user);
                        observableEventList.clear();
                        for (Event event: response.body().getEvents()) {
                            event.setSynchronized(true);
                            observableEventList.add(event);
                        }
                        new ReplaceEventListInDBThread(observableEventList).start();
                        updateObservableString();
                    }
                }
            }
            @Override
            public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public void addEvent(String email, String password, Event event){
        event.update();
        event.setSynchronized(false);
        isLoading.set(true);
        observableEventList.add(event);
        updateObservableString();
        new AddEventInDBThread(event).start();
        Call<Void> putEvent = apiService.putEvent(email, password, event);
        putEvent.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                new Thread() {
                    @Override
                    public void run() {
                        isLoading.set(false);
                        if (response.isSuccessful()) {
                            new GetAllEventsFromDBThread(observableEventList).start();

                            Event updateEvent = findEventByUpdateTime(observableEventList, event.getLastUpdateTime());
                            observableEventList.remove(updateEvent);
                            new DeleteEventFromDBThread(updateEvent);
                            updateEvent.setSynchronized(true);
                            new AddEventInDBThread(event);
                            observableEventList.add(event);
                            updateObservableString();
                        }
                    }
                }.start();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public void deleteEvent(String email, String password, int id){
        if (id == 0){
            return;
        }

        isLoading.set(true);
        Call<Void> deleteEvent = apiService.deleteEvent(email, password, id);

        deleteEvent.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                new Thread(){
                    @Override
                    public void run() {
                        Event event = db.service().findEventByID(id);
                        db.service().delete(event);
                        observableEventList.clear();
                        observableEventList.addAll(db.service().getEvents());
                        updateObservableString();
                    }
                }.start();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });

    }

    private static class DeleteEventFromDBThread extends Thread{
        private Event event;

        private DeleteEventFromDBThread(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            db.service().delete(event);
        }
    }



    private Event findEventByUpdateTime(List<Event> events, long time){
        for (Event event: events){
            if (event.getLastUpdateTime() == time){
                return event;
            }
        }
        throw new NullPointerException();
    }

    private static class GetAllEventsFromDBThread extends Thread{
        private ObservableList<Event> events;

        private GetAllEventsFromDBThread(ObservableList<Event> events) {
            this.events = events;
        }

        @Override
        public void run() {
            List<Event> eventList = db.service().getEvents();
            events.clear();
            events.addAll(eventList);
        }
    }

    private static class AddEventInDBThread extends Thread{
        private Event event;

        private AddEventInDBThread(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            db.service().insert(event);
        }
    }


    private static class ReplaceEventListInDBThread extends Thread{
        private List<Event> eventList;

        private ReplaceEventListInDBThread(List<Event> eventList) {
            this.eventList = eventList;
        }

        @Override
        public void run() {
            db.service().deleteAllEvents();
            db.service().insert(eventList);
        }
    }


    private static class GetUserFromDBThread extends Thread{
        private ObservableField<User> observableUser;

        GetUserFromDBThread(ObservableField<User> observableUser) {
            this.observableUser = observableUser;
        }

        @Override
        public void run() {
            List<User> userList = db.service().getUser();
            if ( !userList.isEmpty())
                observableUser.set(db.service().getUser().get(0));
            else observableUser.set(null);
        }
    }

    private static class InsertUserIntoDBThread extends Thread{

        private User user;

        InsertUserIntoDBThread(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            db.service().deleteUser();
            db.service().insert(user);
        }
    }

    private static class DeleteUserFromDBThread extends Thread{
        @Override
        public void run() {
            db.service().deleteUser();
        }
    }

    private static class UpdateUserInDBThread extends Thread{
        User user;

        public UpdateUserInDBThread(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            db.service().update(user);
        }
    }

    public void setObservableUser(User observableUser) {
        this.observableUser.set(observableUser);
    }

    public void setObservableEventList(ObservableList<Event> observableEventList) {
        this.observableEventList = observableEventList;
    }


    public Event getEvent(int id){
        return observableEventList.get(id);
    }


    public ObservableField<User> getObservableUser() {
        return observableUser;
    }

    public ObservableList<Event> getObservableEventList() {
        return observableEventList;
    }
}
