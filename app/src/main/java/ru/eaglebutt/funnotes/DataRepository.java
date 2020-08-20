package ru.eaglebutt.funnotes;


import android.content.Context;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.eaglebutt.funnotes.api.APIService;
import ru.eaglebutt.funnotes.api.APIServiceConstructor;
import ru.eaglebutt.funnotes.db.MainDB;
import ru.eaglebutt.funnotes.model.AllUsersResponseData;
import ru.eaglebutt.funnotes.model.Event;
import ru.eaglebutt.funnotes.model.User;

public class DataRepository {
    private MainDB db;
    private APIService apiService;
    private static DataRepository INSTANCE = null;


    private ObservableField<User> observableUser = new ObservableField<>();
    private List<Event> eventList = new ArrayList<>();
    private MutableLiveData<List<Event>> liveEventList = new MutableLiveData<>();
    private ObservableBoolean isLoading = new ObservableBoolean(false);
    private ObservableBoolean isSynchronized = new ObservableBoolean(false);


    private DataRepository(Context context) {
        db = MainDB.get(context);
        apiService = APIServiceConstructor.createService(APIService.class);
    }

    public ObservableBoolean getIsSynchronized() {
        return isSynchronized;
    }

    public void setIsSynchronized(ObservableBoolean isSynchronized) {
        this.isSynchronized = isSynchronized;
    }

    public ObservableBoolean getIsLoading() {
        return isLoading;
    }

    public void setIsLoading(ObservableBoolean isLoading) {
        this.isLoading = isLoading;
    }

    public static DataRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(context);
        }
        return INSTANCE;
    }

    public MutableLiveData<List<Event>> getLiveEventList() {
        return liveEventList;
    }

    private void updateLiveData() {
        if (observableUser.get() == null) {
            liveEventList.setValue(null);
            return;
        }
        liveEventList.postValue(eventList);
    }


    public void getUserFromDB() {
        new Thread() {
            @Override
            public void run() {
                try {
                    observableUser.set(db.service().getUser().get(0));
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void addUser(User user) {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                db.service().deleteUser();
                db.service().deleteAllEvents();
                observableUser.set(null);
                eventList.clear();
                Call<Void> putUser = apiService.putUser(user);
                putUser.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()) {
                            new Thread(){
                                @Override
                                public void run() {
                                    observableUser.set(user);
                                    getUserAndEvents();
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

    public void deleteUser() {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart() || observableUser.get() == null) {
                    return;
                }
                Call<Void> deleteUser = apiService.deleteUser(observableUser.get().getEmail(), observableUser.get().getPassword());
                deleteUser.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()) {
                            new Thread() {
                                @Override
                                public void run() {
                                    db.service().deleteUser();
                                    observableUser.set(null);
                                    eventList.clear();
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


    public void updateUser(User user) {

        new Thread() {
            @Override
            public void run() {
                if (!beforeStart() || observableUser.get() == null) {
                    return;
                }
                Call<Void> updateUser = apiService.updateUser(observableUser.get().getEmail(), observableUser.get().getPassword(), user);
                updateUser.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()) {
                            getUserAndEvents();
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


    public void getUserAndEvents() {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart() || observableUser.get() == null) {
                    return;
                }
                Log.d("THREADS", "getUserAndEvents: " + Thread.currentThread().getName());
                Call<AllUsersResponseData> responseDataCall = apiService.getAllUserData(observableUser.get().getEmail(), observableUser.get().getPassword());
                loadDataFromDB();
                responseDataCall.enqueue(new Callback<AllUsersResponseData>() {
                    @Override
                    public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {
                        isLoading.set(false);
                        new Thread() {
                            @Override
                            public void run() {
                                if (!response.isSuccessful()) {
                                    return;
                                }
                                if (response.body() == null) {
                                    return;
                                }
                                isSynchronized.set(true);
                                db.service().deleteUser();
                                db.service().deleteAllEvents();
                                User user = response.body().getUser();
                                user.setSynchronized(true);
                                List<Event> eventList = response.body().getEvents();
                                for (Event event : eventList) {
                                    event.setStatus(Event.STATUSES.SYNCHRONIZED);
                                    event.update();
                                    db.service().insert(event);
                                }
                                db.service().insert(user);
                                loadDataFromDB();
                            }
                        }.start();
                    }

                    @Override
                    public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                        isLoading.set(false);
                        isSynchronized.set(false);
                    }
                });
            }
        }.start();
    }


    public void addEventInThread(Event event) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                addEvent(event);
            }
        };
        thread.start();
    }

    public void addEvent(Event event) {
        Log.d("THREADS", "addEvent: " + Thread.currentThread().getName());
        if (event.getStatus() != Event.STATUSES.NEW) {
            event.update();
            event.setCreated(System.currentTimeMillis());
            event.setStatus(Event.STATUSES.NEW);
            db.service().insert(event);
            loadDataFromDB();
        }
        Call<Void> putEvent = apiService.putEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event);
        putEvent.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()) {
                    new Thread() {
                        @Override
                        public void run() {

                            isSynchronized.set(true);
                            getUserAndEvents();
                        }
                    }.start();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
                isSynchronized.set(false);

            }
        });
    }

    public void deleteEventInThread(int id) {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                deleteEvent(id);
            }
        }.start();
    }

    public void deleteEvent(int id) {
        if (id == 0) {
            return;
        }
        Event event = db.service().findEventByLocalId(id);

        if (event == null) {
            isLoading.set(false);
            return;
        }

        if (event.getStatus() == Event.STATUSES.NEW) {
            db.service().delete(event);
            isLoading.set(false);
            return;
        } else {
            event.setStatus(Event.STATUSES.DELETED);
            event.update();
            db.service().update(event);
        }

        Call<Void> deleteEvent = apiService.deleteEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event.getServerId());
        deleteEvent.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                getUserAndEvents();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
                isSynchronized.set(false);

            }
        });
    }


    public void updateEventInThread(Event event) {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                updateEvent(event);
            }
        }.start();
    }


    public void updateEvent(Event event) {
        event.setServerId(db.service().findEventByLocalId(event.getLocalId()).getServerId());
        event.setStatus(Event.STATUSES.UPDATED);
        event.update();
        db.service().update(event);
        loadDataFromDB();
        Call<Void> updateEventCall = apiService.putEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event);
        updateEventCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);

                if (response.isSuccessful()) {
                    getUserAndEvents();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.set(false);
                isSynchronized.set(false);

            }
        });
    }


    public void synchronizeWithServer() {
        if (observableUser.get() == null)
            return;
        List<Event> notUpdatedList = db.service().getNotUpdatedEvents();
        for (Event event : notUpdatedList) {
            if (event.getStatus() == Event.STATUSES.NEW) {
                addEvent(event);
            } else if (event.getStatus() == Event.STATUSES.DELETED) {
                deleteEvent(event.getLocalId());
            } else if (event.getStatus() == Event.STATUSES.UPDATED) {
                updateEvent(event);
            }
        }
    }

    public void loadDataFromDB() {
        List<User> user = db.service().getUser();
        if (user == null || user.isEmpty()) {
            return;
        }
        observableUser.set(user.get(0));
        eventList.clear();
        eventList.addAll(db.service().getEvents());
        updateLiveData();
    }


    public ObservableField<User> getObservableUser() {
        return observableUser;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    private boolean beforeStart() {
        if (isLoading.get())
            return false;
        if (!isSynchronized.get())
            synchronizeWithServer();
        isLoading.set(true);
        loadDataFromDB();
        updateLiveData();
        return true;
    }

}
