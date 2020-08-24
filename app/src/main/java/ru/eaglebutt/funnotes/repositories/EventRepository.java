package ru.eaglebutt.funnotes.repositories;


import android.content.Context;
import android.os.AsyncTask;

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

public class EventRepository {
    private MainDB db;
    private APIService apiService;
    private static EventRepository INSTANCE = null;
    private UserRepository userRepository;

    private List<Event> eventList = new ArrayList<>();
    private MutableLiveData<List<Event>> liveEventList = new MutableLiveData<>();
    private static int TODAY_DATA = 0;
    private static int ALL_DATA = 1;
    private int type = 1;
    private boolean isSynchronize = false;

    private EventRepository(Context context) {
        db = MainDB.get(context);
        apiService = APIServiceConstructor.createService(APIService.class);
        userRepository = UserRepository.getInstance(context);
    }

    public static EventRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new EventRepository(context);
        }
        return INSTANCE;
    }

    public MutableLiveData<List<Event>> getLiveEventList() {
        return liveEventList;
    }

    private void updateLiveData() {
        liveEventList.postValue(eventList);
    }

    public void getAllEvents() {
        type = ALL_DATA;
        new GetUserAndEvents().execute();
    }

    public synchronized void addEvent(Event event) {
            new AddEventTask().execute(event);
    }

    public synchronized void deleteEvent(int id) {
        new DeleteEventTask().execute(id);
    }

    public synchronized void updateEvent(Event event) {
        new UpdateEventTask().execute(event);
    }

    public synchronized void synchronizeWithServer() {
        if (isSynchronize)
            return;
        List<Event> notUpdatedList = db.eventDAO().getNotUpdatedEvents();
        for (Event event : notUpdatedList) {
            isSynchronize = true;
            if (event.getStatus() == Event.STATUSES.NEW) {
                new AddEventTask().doInBackground(event);
            } else if (event.getStatus() == Event.STATUSES.DELETED) {
                new DeleteEventTask().doInBackground(event.getLocalId());
            } else if (event.getStatus() == Event.STATUSES.UPDATED) {
                new UpdateEventTask().doInBackground(event);
            }
        }
    }



    private synchronized boolean beforeStart() {
        if (isSynchronize)
            return false;
        /*if (!db.eventDAO().getNotUpdatedEvents().isEmpty())
            synchronizeWithServer();
         */
        if (type == ALL_DATA) {
            new LoadAllEventsFromDB().execute();
        } else {
            new LoadTodayEventsFromDB().execute();
        }
        return false;
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

    private abstract class EventAsyncTask<T> extends AsyncTask<T, Void, Void> {
        protected AsyncTask<Void, Void, Void> onResponseUpdateListTask;
        private AsyncTask<Void, Void, Void> updateListTask;

        public EventAsyncTask() {
            if (type == ALL_DATA) {
                onResponseUpdateListTask = new GetUserAndEvents();
                updateListTask = new LoadAllEventsFromDB();
            } else if (type == TODAY_DATA) {
                onResponseUpdateListTask = new GetTodayTasks();
                updateListTask = new LoadTodayEventsFromDB();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateListTask.execute();
        }

    }

    private class AddEventTask extends EventAsyncTask<Event> {

        @Override
        protected Void doInBackground(Event... events) {
            if (beforeStart()) {
                return null;
            }
            Event event = events[0];
            if (event.getStatus() != Event.STATUSES.NEW) {
                event.update();
                event.setCreated(System.currentTimeMillis());
                event.setStatus(Event.STATUSES.NEW);
                db.eventDAO().insert(event);
            }
            User user = userRepository.getObservableUser().get();
            Call<Void> putEvent = apiService.putEvent(user.getEmail(), user.getPassword(), event);
            putEvent.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (!isSynchronize)
                            onResponseUpdateListTask.execute();
                        isSynchronize = false;
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
            return null;
        }
    }

    private class DeleteEventTask extends EventAsyncTask<Integer> {

        @Override
        protected Void doInBackground(Integer... integers) {
            if (beforeStart()) {
                return null;
            }
            int id = integers[0];
            if (id == 0) {
                return null;
            }
            Event event = db.eventDAO().findEventByLocalId(id);

            if (event == null) {
                return null;
            }

            if (event.getStatus() == Event.STATUSES.NEW) {
                db.eventDAO().delete(event);
                return null;
            } else {
                event.setStatus(Event.STATUSES.DELETED);
                event.update();
                db.eventDAO().update(event);
            }
            User user = userRepository.getObservableUser().get();
            Call<Void> deleteEvent = apiService.deleteEvent(user.getEmail(), user.getPassword(), event.getServerId());
            deleteEvent.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!isSynchronize)
                        onResponseUpdateListTask.execute();
                    isSynchronize = false;
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
            return null;
        }
    }

    private class UpdateEventTask extends EventAsyncTask<Event> {

        @Override
        protected Void doInBackground(Event... events) {
            if (beforeStart()) {
                return null;
            }
            Event event = events[0];
            event.setServerId(db.eventDAO().findEventByLocalId(event.getLocalId()).getServerId());
            event.setStatus(Event.STATUSES.UPDATED);
            event.update();
            db.eventDAO().update(event);
            User user = userRepository.getObservableUser().get();
            Call<Void> updateEventCall = apiService.putEvent(user.getEmail(), user.getPassword(), event);
            updateEventCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (!isSynchronize)
                            onResponseUpdateListTask.execute();
                        isSynchronize = false;
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
            return null;
        }
    }

    public void getTodayTasks() {
        type = TODAY_DATA;
        new GetTodayTasks().execute();
    }


    public List<Event> getEventList() {
        return eventList;
    }

    private class GetUserAndEvents extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            userRepository.getUser();
            User user = userRepository.getObservableUser().get();
            if (user == null) {
                return null;
            }
            eventList = db.eventDAO().getEvents();
            if (!db.eventDAO().getNotUpdatedEvents().isEmpty())
                synchronizeWithServer();
            Call<AllUsersResponseData> responseDataCall = apiService.getAllUserData(user.getEmail(), user.getPassword());
            responseDataCall.enqueue(new Callback<AllUsersResponseData>() {
                @Override
                public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {

                    if (!response.isSuccessful()) {
                        return;
                    }
                    if (response.body() == null) {
                        return;
                    }
                    new OnResponseGetUserAndEvents().execute(response.body());
                }

                @Override
                public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                }
            });
            return null;
        }
    }

    class OnResponseGetUserAndEvents extends AsyncTask<AllUsersResponseData, Void, Void> {

        @Override
        protected Void doInBackground(AllUsersResponseData... allUsersResponseData) {
            db.userDAO().deleteUser();
            db.eventDAO().deleteAllEvents();
            User user = allUsersResponseData[0].getUser();
            user.setSynchronized(true);
            List<Event> eventList = allUsersResponseData[0].getEvents();
            for (Event event : eventList) {
                event.setStatus(Event.STATUSES.SYNCHRONIZED);
                event.update();
                db.eventDAO().insert(event);
            }
            db.userDAO().insert(user);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (type == ALL_DATA)
                new LoadAllEventsFromDB().execute();
            else
                new LoadTodayEventsFromDB().execute();
        }
    }

    public class LoadAllEventsFromDB extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            eventList.clear();
            eventList.addAll(db.eventDAO().getEvents());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateLiveData();
        }
    }

    public class LoadTodayEventsFromDB extends AsyncTask<Void, Void, Void> {

        public LoadTodayEventsFromDB() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            eventList.clear();
            Date date = new Date(System.currentTimeMillis());
            long startTime = atStartOfDay(date).getTime();
            long endTime = atEndOfDay(date).getTime();
            eventList.addAll(db.eventDAO().getEventsBetweenTime(startTime / 1000, endTime / 1000));
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
            new LoadTodayEventsFromDB().execute();
        }
    }

}
