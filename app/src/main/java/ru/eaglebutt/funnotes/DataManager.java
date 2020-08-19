package ru.eaglebutt.funnotes;


import android.content.Context;
import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

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

public class DataManager {
    private MainDB db;
    private APIService apiService;
    private static DataManager INSTANCE = null;
    private ObservableField<User> observableUser = new ObservableField<>();
    private ObservableList<Event> observableEventList = new ObservableArrayList<>();
    private ObservableField<String> observableString = new ObservableField<>();
    private ObservableBoolean isLoading = new ObservableBoolean(false);
    private ObservableBoolean isSynchronized = new ObservableBoolean(false);

    public ObservableField<String> getObservableString() {
        return observableString;
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

    private DataManager(Context context) {
        db = MainDB.get(context);
        apiService = APIServiceConstructor.createService(APIService.class);
    }

    public static DataManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DataManager(context);
        }
        return INSTANCE;
    }

    private void updateObservableString() {
        if (observableUser.get() == null) {
            observableString.set("");
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(observableUser.get().toString())
                .append("\n");
        for (Event event : observableEventList) {
            builder.append(event.toString());
            builder.append("\n");
        }
        observableString.set(builder.toString());
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
                observableEventList.clear();
                Call<Void> putUser = apiService.putUser(user);
                putUser.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()) {
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
        }.start();
    }

    public void deleteUser(String email, String password) {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                Call<Void> deleteUser = apiService.deleteUser(email, password);
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
        }.start();
    }


    public void updateUser(String email, String password, User user) {

        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                Call<Void> updateUser = apiService.updateUser(email, password, user);
                updateUser.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        isLoading.set(false);
                        if (response.isSuccessful()) {
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


    public void getUserAndEvents(String email, String password) {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                Log.d("THREADS", "getUserAndEvents: " + Thread.currentThread().getName());
                Call<AllUsersResponseData> responseDataCall = apiService.getAllUserData(email, password);

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


    public void addEventInThread(String email, String password, Event event) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                addEvent(email, password, event);
            }
        };
        thread.start();
    }

    public void addEvent(String email, String password, Event event) {
        Log.d("THREADS", "addEvent: " + Thread.currentThread().getName());
        if (event.getStatus() != Event.STATUSES.NEW) {
            event.update();
            event.setCreated(System.currentTimeMillis());
            event.setStatus(Event.STATUSES.NEW);
            db.service().insert(event);
            loadDataFromDB();
        }
        Call<Void> putEvent = apiService.putEvent(email, password, event);
        putEvent.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.set(false);
                if (response.isSuccessful()) {
                    new Thread() {
                        @Override
                        public void run() {

                            isSynchronized.set(true);
                            getUserAndEvents(email, password);
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

    public void deleteEventInThread(String email, String password, int id) {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                deleteEvent(email, password, id);
            }
        }.start();
    }

    public void deleteEvent(String email, String password, int id) {
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
                isSynchronized.set(false);

            }
        });
    }


    public void updateEventInThread(String email, String password, Event event) {
        new Thread() {
            @Override
            public void run() {
                if (!beforeStart()) {
                    return;
                }
                updateEvent(email, password, event);
            }
        }.start();
    }


    public void updateEvent(String email, String password, Event event) {
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

                if (response.isSuccessful()) {
                    getUserAndEvents(email, password);
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
                addEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event);
            } else if (event.getStatus() == Event.STATUSES.DELETED) {
                deleteEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event.getLocalId());
            } else if (event.getStatus() == Event.STATUSES.UPDATED) {
                updateEvent(observableUser.get().getEmail(), observableUser.get().getPassword(), event);
            }
        }
    }

    public void loadDataFromDB() {
        List<User> user = db.service().getUser();
        if (user == null || user.isEmpty()) {
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

    private boolean beforeStart() {
        if (isLoading.get())
            return false;
        if (!isSynchronized.get())
            synchronizeWithServer();
        isLoading.set(true);
        loadDataFromDB();
        updateObservableString();
        return true;
    }

}