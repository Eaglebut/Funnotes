package ru.eaglebutt.funnotes.repositories;

import android.content.Context;
import android.os.AsyncTask;

import androidx.databinding.ObservableField;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.eaglebutt.funnotes.api.APIService;
import ru.eaglebutt.funnotes.api.APIServiceConstructor;
import ru.eaglebutt.funnotes.db.MainDB;
import ru.eaglebutt.funnotes.model.User;

public class UserRepository {
    private static UserRepository INSTANCE = null;
    private MainDB db;
    private APIService apiService;
    private ObservableField<User> observableUser = new ObservableField<>();

    private UserRepository(Context context) {
        db = MainDB.get(context);
        apiService = APIServiceConstructor.createService(APIService.class);
    }

    public static UserRepository getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new UserRepository(context);
        return INSTANCE;
    }

    public ObservableField<User> getObservableUser() {
        return observableUser;
    }

    public synchronized void updateUserInDB(User user) {
        new UpdateUserInDBTask().execute(user);
    }

    public synchronized void putUserIntoDB(User user) {
        new PutUserIntoDBTask().execute(user);
    }

    public synchronized void getUserFromDB() {
        new GetUserFromDBTask().execute();
    }

    public synchronized void addUser(User user) {
        new AddUserTask().execute(user);
    }

    public synchronized void deleteUser() {
        new DeleteUserTask().execute();
    }

    public synchronized void updateUser(User user) {
        new UpdateUserInDBTask().execute(user);
    }

    public synchronized void getUser() {
        new GetUserTask().execute();
    }

    private class UpdateUserInDBTask extends AsyncTask<User, Void, Void> {

        @Override
        protected Void doInBackground(User... users) {
            db.userDAO().deleteUser();
            db.userDAO().insert(users[0]);
            observableUser.set(db.userDAO().getUser());
            return null;
        }
    }

    private class PutUserIntoDBTask extends AsyncTask<User, Void, Void> {

        @Override
        protected Void doInBackground(User... users) {
            db.userDAO().deleteUser();
            db.eventDAO().deleteAllEvents();
            db.userDAO().insert(users[0]);
            observableUser.set(db.userDAO().getUser());
            return null;
        }
    }

    private class GetUserFromDBTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                observableUser.set(db.userDAO().getUser());
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class AddUserTask extends AsyncTask<User, Void, Void> {
        @Override
        protected Void doInBackground(User... users) {
            User user = users[0];
            db.userDAO().deleteUser();
            db.eventDAO().deleteAllEvents();
            observableUser.set(null);
            Call<Void> putUser = apiService.putUser(user);
            putUser.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        new OnAddUserResponseTask().execute(user);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
            return null;
        }
    }

    private class OnAddUserResponseTask extends AsyncTask<User, Void, Void> {
        @Override
        protected Void doInBackground(User... users) {
            db.userDAO().insert(users[0]);
            observableUser.set(db.userDAO().getUser());
            return null;
        }
    }

    private class DeleteUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (observableUser.get() == null) {
                return null;
            }
            Call<Void> deleteUser = apiService.deleteUser(observableUser.get().getEmail(), observableUser.get().getPassword());
            deleteUser.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        new OnResponseDeleteUserTask().execute();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
            return null;
        }
    }

    private class OnResponseDeleteUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            db.userDAO().deleteUser();
            observableUser.set(null);
            return null;
        }
    }

    private class UpdateUserTask extends AsyncTask<User, Void, Void> {

        @Override
        protected Void doInBackground(User... users) {

            if (observableUser.get() == null) {
                return null;
            }
            User user = users[0];
            Call<Void> updateUser = apiService.updateUser(observableUser.get().getEmail(), observableUser.get().getPassword(), user);
            updateUser.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if (response.isSuccessful()) {
                        new OnAddUserResponseTask().execute(users);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });
            return null;
        }
    }

    private class OnResponseUpdateUserTask extends AsyncTask<User, Void, Void> {

        @Override
        protected Void doInBackground(User... users) {

            return null;
        }
    }

    private class GetUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (observableUser.get() == null) {
                return null;
            }
            Call<User> getUserCall = apiService.getUser(observableUser.get().getEmail(), observableUser.get().getPassword());
            getUserCall.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (!response.isSuccessful())
                        return;
                    if (response.body() != null) {
                        new OnResponseGetUserTask().execute(response.body());
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                }
            });
            return null;
        }
    }

    private class OnResponseGetUserTask extends AsyncTask<User, Void, Void> {
        @Override
        protected Void doInBackground(User... users) {
            User user = users[0];
            db.userDAO().deleteUser();
            db.userDAO().insert(user);
            observableUser.set(db.userDAO().getUser());
            return null;
        }
    }


}
