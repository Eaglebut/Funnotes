package ru.eaglebutt.funnotes;

import android.content.Context;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.eaglebutt.funnotes.API.APIService;
import ru.eaglebutt.funnotes.API.APIServiceConstructor;
import ru.eaglebutt.funnotes.DB.MainDB;
import ru.eaglebutt.funnotes.Model.Event;
import ru.eaglebutt.funnotes.Model.User;

public class Repository {
    private static MainDB db = null;
    private static APIService apiService = null;
    private static Repository INSTANCE = null;

    private ObservableField<User> observableUser = new ObservableField<>();
    private ObservableList<Event> observableEventList = new ObservableArrayList<>();


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
        Call<User> userCall = apiService.getUser(email, password);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        User user = response.body();
                        user.setSynchronized(true);
                        new InsertUserIntoDBThread(user).start();
                        observableUser.set(user);
                        return;
                    }
                }
                new GetUserFromDBThread(observableUser).start();
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                new GetUserFromDBThread(observableUser).start();
            }
        });
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



    public void setObservableUser(ObservableField<User> observableUser) {
        this.observableUser = observableUser;
    }

    public void setObservableEventList(ObservableList<Event> observableEventList) {
        this.observableEventList = observableEventList;
    }

    public ObservableField<User> getObservableUser() {
        return observableUser;
    }

    public ObservableList<Event> getObservableEventList() {
        return observableEventList;
    }
}
