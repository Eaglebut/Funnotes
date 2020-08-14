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
    private  MainDB db;
    private  APIService apiService;
    private static Repository INSTANCE = null;

    private ObservableField<User> observableUser = new ObservableField<>();
    private ObservableList<Event> observableEventList = new ObservableArrayList<>();
    private ObservableField<String> observableString = new ObservableField<>();


    public ObservableField<String> getObservableString() {
        return observableString;
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

    private synchronized void updateObservableString(){
        if (observableUser.get() == null){
            observableString.set("");
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(observableUser.get().toString())
                .append("\n");
        for (Event event: observableEventList) {
            builder.append(event.toString());
        }
        observableString.set(builder.toString());
    }

    public synchronized void addUser(User user){
        synchronizeWithServer();
        if (isLoading.get()){return;}
        isLoading.set(true);
        observableUser.set(null);
        updateObservableString();
        Call<Void> putUser = apiService.putUser(user);
        putUser.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()){
                    new Thread(){
                        @Override
                        public void run() {
                            user.setSynchronized(true);
                            db.service().deleteUser();
                            db.service().insert(user);
                            observableUser.set(user);
                            updateObservableString();
                        }
                    }.start();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public synchronized void deleteUser(String email, String password){
        synchronizeWithServer();
        if (isLoading.get()){return;}
        isLoading.set(true);
        Call<Void> deleteUser = apiService.deleteUser(email, password);
        deleteUser.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()){
                    new Thread(){
                        @Override
                        public void run() {
                            db.service().deleteUser();
                            db.service().deleteAllEvents();
                            observableUser.set(null);
                            observableEventList.clear();
                            updateObservableString();
                        }
                    }.start();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }


    public synchronized void updateUser(String email, String password, User user){
        synchronizeWithServer();
        if (isLoading.get()){return;}
        isLoading.set(true);
        Call<Void> updateUser = apiService.updateUser(email, password, user);
        updateUser.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()){
                    getUserAndEvents(email, password);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public synchronized void getUserAndEvents(String email, String password){
        synchronizeWithServer();
        if (isLoading.get()){return;}
        isLoading.set(true);
        Call<AllUsersResponseData> userCall = apiService.getAllUserData(email, password);
        userCall.enqueue(new Callback<AllUsersResponseData>() {
            @Override
            public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {
                isLoading.set(false);
                if (response.isSuccessful()){
                    new Thread(){
                        @Override
                        public void run() {
                            User user =  response.body().getUser();
                            db.service().deleteUser();
                            db.service().deleteAllEvents();
                            user.setSynchronized(true);
                            db.service().insert(user);
                            observableEventList.clear();
                            for (Event event: response.body().getEvents()){
                                event.setStatus(Event.STATUSES.SYNCHRONIZED);
                                event.update();
                                observableEventList.add(event);
                            }
                            db.service().insert(observableEventList);
                            observableEventList.clear();
                            observableEventList.addAll(db.service().getEvents());
                            updateObservableString();
                        }
                    }.start();
                }
            }
            @Override
            public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public synchronized void addEvent(String email, String password, Event event){
        new Thread(){
            @Override
            public void run() {
                synchronizeWithServer();
                if (isLoading.get()) {
                    return;
                }
                isLoading.set(true);
                event.update();
                event.setCreated(System.currentTimeMillis());
                event.setStatus(Event.STATUSES.NEW);
                db.service().insert(event);
                observableEventList.clear();
                observableEventList.addAll(db.service().getEvents());
                updateObservableString();
                Call<Void> putEvent = apiService.putEvent(email, password, event);
                putEvent.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()) {
                            new Thread() {
                                @Override
                                public void run() {
                                    getUserAndEvents(email, password);
                                }
                            }.start();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        isLoading.set(false);
                    }
                });
            }
        }.start();
    }

    public synchronized void deleteEvent(String email, String password, int id){
        synchronizeWithServer();
        if (isLoading.get()){return;}
        if (id == 0){
            return;
        }
        isLoading.set(true);
        Event event = null;
        for (int i = 0; i < observableEventList.size(); i++){
            if(observableEventList.get(i).getLocalId() == id){
                event = observableEventList.get(i);
                if (event.getStatus() == Event.STATUSES.NEW){
                    Event finalEvent = event;
                    new Thread(){
                        @Override
                        public void run() {
                            db.service().delete(finalEvent);
                        }
                    }.start();
                    observableEventList.remove(event);
                    updateObservableString();
                    isLoading.set(false);
                    return;
                }
                else {
                    event.setStatus(Event.STATUSES.DELETED);
                    event.update();
                    Event finalEvent = event;
                    new Thread(){
                        @Override
                        public void run() {
                            db.service().update(finalEvent);
                        }
                    }.start();
                    observableEventList.set(i, event);
                    updateObservableString();
                }
            }
        }
        if (event == null){
            isLoading.set(false);
            return;
        }
        Call<Void> deleteEvent = apiService.deleteEvent(email, password, event.getServerId());
        deleteEvent.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                getUserAndEvents(email, password);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }


    public synchronized void updateEvent(String email, String password, Event event){
        synchronizeWithServer();
        new Thread(){
            @Override
            public void run() {
                if (isLoading.get()){return;}
                isLoading.set(true);
                Event bufferEvent = db.service().findEventByLocalId(event.getLocalId());
                if (bufferEvent == null){
                    isLoading.set(false);
                    return;
                }
                event.setServerId(bufferEvent.getServerId());
                event.setStatus(Event.STATUSES.UPDATED);
                event.update();
                db.service().update(event);
                updateObservableString();
                Call<Void> updateEventCall = apiService.putEvent(email, password, event);
                updateEventCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()){
                            getUserAndEvents(email, password);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        isLoading.set(false);
                    }
                });
            }
        }.start();
    }

    public synchronized void synchronizeWithServer(){
        if (isLoading.get()){return;}
        new Thread(){
            @Override
            public void run() {
                loadDataFromDB();
                if(observableUser.get() == null)
                    return;
                if (!observableUser.get().isSynchronized()){
                    updateUser(observableUser.get().getEmail(), observableUser.get().getPassword(),observableUser.get());
                }
                Event buffer;
                for (Event event: observableEventList){
                    if (event.getStatus() == Event.STATUSES.NEW){
                        buffer = event;
                        deleteEvent(observableUser.get().getEmail(),observableUser.get().getPassword(), event.getLocalId());
                        addEvent(observableUser.get().getEmail(),observableUser.get().getPassword(), buffer);
                    }
                    else if(event.getStatus() == Event.STATUSES.UPDATED){
                        updateEvent(observableUser.get().getEmail(),observableUser.get().getPassword(), event);
                    }
                    else if (event.getStatus() == Event.STATUSES.DELETED){
                        deleteEvent(observableUser.get().getEmail(),observableUser.get().getPassword(), event.getLocalId());
                    }
                }
            }
        }.start();
    }

    public synchronized void loadDataFromDB(){
        new Thread(){
            @Override
            public void run() {
                List<User> user = db.service().getUser();
                if (user == null || user.isEmpty()){
                    return;
                }
                observableUser.set(user.get(0));
                observableEventList.clear();
                observableEventList.addAll(db.service().getEvents());
                updateObservableString();
            }
        }.start();
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
