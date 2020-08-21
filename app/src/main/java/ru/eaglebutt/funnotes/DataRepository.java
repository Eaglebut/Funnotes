package ru.eaglebutt.funnotes;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private static int TODAY_DATA = 0;
    private static int ALL_DATA = 1;
    private int type = 1;

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
                if (beforeStart()) {
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
                if (beforeStart() || observableUser.get() == null) {
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
                if (beforeStart() || observableUser.get() == null) {
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
        new GetUserAndEvents().execute();
    }

    public void addEventInThread(Event event) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (beforeStart()) {
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
            new LoadAllDataFromDB().execute();
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
                if (beforeStart()) {
                    return;
                }
                deleteEvent(id);
            }
        }.start();
    }

    public void updateEventInThread(Event event) {
        new Thread() {
            @Override
            public void run() {
                if (beforeStart()) {
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
        new LoadAllDataFromDB().execute();
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

    private Date atEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private Date atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
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

    private boolean beforeStart() {
        if (isLoading.get())
            return true;
        if (!isSynchronized.get())
            synchronizeWithServer();
        isLoading.set(true);
        if (type == ALL_DATA)
            new LoadAllDataFromDB().execute();
        else
            new LoadTodayDataFromDB().execute();
        return false;
    }

    public void getTodayTasks() {
        type = TODAY_DATA;
        new GetTodayTasks().execute();
    }

    public ObservableField<User> getObservableUser() {
        return observableUser;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    private class GetUserAndEvents extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (beforeStart() || observableUser.get() == null) {
                return null;
            }
            Call<AllUsersResponseData> responseDataCall = apiService.getAllUserData(observableUser.get().getEmail(), observableUser.get().getPassword());
            responseDataCall.enqueue(new Callback<AllUsersResponseData>() {
                @Override
                public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {
                    isLoading.set(false);
                    if (!response.isSuccessful()) {
                        return;
                    }
                    if (response.body() == null) {
                        return;
                    }
                    isSynchronized.set(true);
                    new OnResponseGetUserAndEvents().execute(response.body());
                }

                @Override
                public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                    isLoading.set(false);
                    isSynchronized.set(false);
                }
            });
            return null;
        }
    }

    class OnResponseGetUserAndEvents extends AsyncTask<AllUsersResponseData, Void, Void> {

        @Override
        protected Void doInBackground(AllUsersResponseData... allUsersResponseData) {
            db.service().deleteUser();
            db.service().deleteAllEvents();
            User user = allUsersResponseData[0].getUser();
            user.setSynchronized(true);
            List<Event> eventList = allUsersResponseData[0].getEvents();
            for (Event event : eventList) {
                event.setStatus(Event.STATUSES.SYNCHRONIZED);
                event.update();
                db.service().insert(event);
            }
            db.service().insert(user);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (type == ALL_DATA)
                new LoadAllDataFromDB().execute();
            else
                new LoadTodayDataFromDB().execute();
        }
    }

    public class LoadAllDataFromDB extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            List<User> user = db.service().getUser();
            if (user == null || user.isEmpty()) {
                return null;
            }
            observableUser.set(user.get(0));
            eventList.clear();
            eventList.addAll(db.service().getEvents());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateLiveData();
        }
    }

    public class LoadTodayDataFromDB extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            List<User> user = db.service().getUser();
            if (user == null || user.isEmpty()) {
                return null;
            }
            observableUser.set(user.get(0));
            eventList.clear();
            Date date = new Date(System.currentTimeMillis());
            long startTime = atStartOfDay(date).getTime();
            long endTime = atEndOfDay(date).getTime();
            eventList.addAll(db.service().getEventsBetweenTime(startTime / 1000, endTime / 1000));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateLiveData();
        }
    }

    private class GetTodayTasks extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            new GetUserAndEvents().doInBackground();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadTodayDataFromDB().execute();
        }
    }

}
