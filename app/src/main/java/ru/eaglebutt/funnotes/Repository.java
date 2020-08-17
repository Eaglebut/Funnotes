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


    public synchronized void getUserFromDB(){
        new Thread(){
            @Override
            public void run() {
                observableUser.set(db.service().getUser().get(0));
            }
        }.start();
    }

    public synchronized void addUser(User user){
        new Thread(new RepositoryThread() {
            @Override
            void runFunction() {
                db.service().deleteUser();
                db.service().deleteAllEvents();
                observableUser.set(null);
                observableEventList.clear();
                Call<Void> putUser = apiService.putUser(user);
                putUser.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()){
                            new Thread(){
                                @Override
                                public void run() {
                                    getUserAndEvents(user.getEmail(), user.getPassword());
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
        }).start();
    }

    public synchronized void deleteUser(String email, String password){
        new Thread(new RepositoryThread() {
            @Override
            void runFunction() {
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
                                    observableUser.set(null);
                                    observableEventList.clear();
                                    observableString.set(null);
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
        }).start();
    }


    public synchronized void updateUser(String email, String password, User user){

        new Thread(new RepositoryThread() {
            @Override
            void runFunction() {
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
        }).start();
    }


    public synchronized void getUserAndEvents(String email, String password){
        new Thread(new RepositoryThread() {
            @Override
            void runFunction() {
                Call<AllUsersResponseData> responseDataCall = apiService.getAllUserData(email, password);

                responseDataCall.enqueue(new Callback<AllUsersResponseData>() {
                    @Override
                    public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {
                        isLoading.set(false);
                        new Thread(){
                            @Override
                            public void run() {
                                if (!response.isSuccessful()){
                                    return;
                                }
                                if(response.body() == null){
                                    return;
                                }
                                db.service().deleteUser();
                                db.service().deleteAllEvents();
                                User user = response.body().getUser();
                                user.setSynchronized(true);
                                List<Event> eventList = response.body().getEvents();
                                for (Event event: eventList){
                                    event.setStatus(Event.STATUSES.UPDATED);
                                    event.update();
                                }
                                db.service().insert(user);
                                db.service().insert(eventList);
                                loadDataFromDB();
                            }
                        }.start();
                    }

                    @Override
                    public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                        isLoading.set(false);
                        new Thread(){
                            @Override
                            public void run() {

                            }
                        }.start();
                    }
                });
            }
        }).start();
    }


    public synchronized void addEvent(String email, String password, Event event){
        new Thread(new RepositoryThread() {
            @Override
            void runFunction() {
                event.update();
                event.setCreated(System.currentTimeMillis());
                event.setStatus(Event.STATUSES.NEW);
                db.service().insert(event);
                loadDataFromDB();
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
                        new Thread(() -> {
                        }).start();
                    }
                });
            }
        }).start();
    }

    public synchronized void deleteEvent(String email, String password, int id){
        new Thread(new RepositoryThread()   {
            @Override
            void runFunction() {
                if (id == 0){
                    return;
                }
                Event event = db.service().findEventByLocalId(id);

                if (event == null){
                    isLoading.set(false);
                    return;
                }

                if (event.getStatus() == Event.STATUSES.NEW){
                    db.service().delete(event);
                    isLoading.set(false);
                    return;
                }
                else{
                    event.setStatus(Event.STATUSES.DELETED);
                    event.update();
                    db.service().update(event);
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
        }).start();

    }


    public synchronized void updateEvent(String email, String password, Event event){
        new Thread(new RepositoryThread() {
            @Override
            void runFunction() {
                event.setServerId(db.service().findEventByLocalId(event.getLocalId()).getServerId());
                event.setStatus(Event.STATUSES.UPDATED);
                event.update();
                db.service().update(event);
                loadDataFromDB();
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
        }).start();
    }

    public synchronized void synchronizeWithServer(){
        if (isLoading.get()){return;}
        loadDataFromDB();
        if(observableUser.get() == null)
            return;
        if (!observableUser.get().isSynchronized()) {
            updateUser(observableUser.get().getEmail(), observableUser.get().getPassword(), observableUser.get());
        }
        for (Event event: observableEventList) {
            if (event.getStatus() == Event.STATUSES.NEW) {
                deleteEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event.getLocalId());
                addEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event);
            } else if (event.getStatus() == Event.STATUSES.UPDATED) {
                updateEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event);
            } else if (event.getStatus() == Event.STATUSES.DELETED) {
                deleteEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event.getLocalId());
            }
        }
    }

    public synchronized void loadDataFromDB(){
        List<User> user = db.service().getUser();
        if (user == null || user.isEmpty()){
            return;
        }
        observableUser.set(user.get(0));
        observableEventList.clear();
        observableEventList.addAll(db.service().getEvents());
        updateObservableString();
    }


    public ObservableField<User> getObservableUser() {
        return observableUser;
    }

    public ObservableList<Event> getObservableEventList() {
        return observableEventList;
    }

    abstract class RepositoryThread implements Runnable{

        abstract void runFunction();

        @Override
        public void run() {
            if (isLoading.get())
                return;
            isLoading.set(true);
            synchronizeWithServer();
            loadDataFromDB();
            updateObservableString();
            runFunction();
        }
    }

}
