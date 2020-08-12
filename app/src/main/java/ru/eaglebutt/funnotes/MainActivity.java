package ru.eaglebutt.funnotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import android.app.Activity;
import android.app.ActivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.eaglebutt.funnotes.API.APIServiceConstructor;
import ru.eaglebutt.funnotes.API.AllUsersResponseData;
import ru.eaglebutt.funnotes.DB.MainDB;
import ru.eaglebutt.funnotes.Model.Event;
import ru.eaglebutt.funnotes.Model.User;
import ru.eaglebutt.funnotes.API.APIService;
import ru.eaglebutt.funnotes.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Repository repository = Repository.getInstance(this);


        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.setRepository(repository);

        MainDB db = MainDB.get(getApplicationContext());
        //new myTask().execute();
        final Button addUserButton = findViewById(R.id.addUser);
        final Button getUserButton = findViewById(R.id.getUser);
        final Button updateUserButton = findViewById(R.id.updateUser);
        final Button deleteUserButton = findViewById(R.id.deleteUser);
        final Button addEventButton = findViewById(R.id.addEvent);
        final Button getEventButton = findViewById(R.id.getEvent);
        final Button updateEventButton = findViewById(R.id.updateEvent);
        final Button deleteEventButton = findViewById(R.id.deleteEvent);
        final Button getAllButton = findViewById(R.id.getAll);
        final EditText editText = findViewById(R.id.edit_text);

        User user = new User();
        user.setId(0);
        user.setEmail("test@test.test");
        user.setPassword("user");
        user.setName("Testuser");
        user.setSurname("Testsurname");

        Event event = new Event();
        event.setTitle("test");
        event.setDescription("description");
        event.setStartTime(System.currentTimeMillis());
        event.setEndTime(System.currentTimeMillis() + 500000);

        APIService service = APIServiceConstructor.createService(APIService.class);


        /*getAllButton.setOnClickListener(v -> {
            Call<AllUsersResponseData> getAllCall = service.getAllUserData(user.getEmail(), user.getPassword());
            getAllCall.enqueue(new Callback<AllUsersResponseData>() {
                @Override
                public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {
                    if (response.code() == 200){
                        if (response.body() != null){
                            textView.setText(response.body().toString());
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Пустое тело", Toast.LENGTH_LONG).show();
                        }
                    }
                    else if(response.code() == 403){
                        Toast.makeText(getApplicationContext(), "Запрещено", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();

                }
            });
        });
*/
        addUserButton.setOnClickListener(v -> {
            user.setName("Notchanged");
            repository.addUser(user);
        });

        getUserButton.setOnClickListener(v -> {
            repository.getUser(user.getEmail(), user.getPassword());
        });

        deleteUserButton.setOnClickListener(v -> {
            repository.deleteUser(user.getEmail(), user.getPassword());
        });

        updateUserButton.setOnClickListener(v -> {
            user.setName("Changed");
           repository.updateUser(user.getEmail(),user.getPassword(), user);
        });
/*
        addEventButton.setOnClickListener(v -> {
            event.setTitle("Test");
            event.setId(0);
            Call<Void> putEventCall = service.putEvent(user.getEmail(), user.getPassword(), event);
            putEventCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 200){
                        Toast.makeText(getApplicationContext(), "Успешно", Toast.LENGTH_LONG).show();

                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Запрещено", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
            });
        });

        getEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());
            Call<Event> getEventCall = service.getEvent(user.getEmail(), user.getPassword(), id);
            getEventCall.enqueue(new Callback<Event>() {
                @Override
                public void onResponse(Call<Event> call, Response<Event> response) {
                    if (response.code() == 200){
                        if (response.body() != null){
                            textView.setText(response.body().toString());
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Пустое тело", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Запрещено", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Event> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
            });
        });

        deleteEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());

            Call<Void> deleteEventCall = service.deleteEvent(user.getEmail(), user.getPassword(), id);
            deleteEventCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 200){
                        Toast.makeText(getApplicationContext(), "Успешно", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Запрещено", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
            });
        });

        updateEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());

            event.setTitle("Updated");
            event.setId(id);
            Call<Void> updateEventCall = service.putEvent(user.getEmail(), user.getPassword(), event);
            updateEventCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 200){
                        Toast.makeText(getApplicationContext(), "Успешно", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Запрещено", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
            });

        });

*/
    }

    static class getUserTask extends AsyncTask<User, Void, User> {

        TextView textView;



        @Override
        protected User doInBackground(User... users) {
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            textView.setText(user.toString());
        }
    }

/*
     class myTask extends AsyncTask<Void, Void, List<Event>>{

        @Override
        protected List<Event> doInBackground(Void... voids) {
            EventList eventList = db.eventList();
            return eventList.getEvents();
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                    android.R.layout.simple_list_item_1);
            for (Event event: events){
                adapter.add(event.toString());
            }
            //ListView listView = findViewById(R.id.list_view);
            //listView.setAdapter(adapter);
        }
    }

 */
}