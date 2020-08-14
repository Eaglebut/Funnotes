package ru.eaglebutt.funnotes;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import ru.eaglebutt.funnotes.API.APIService;
import ru.eaglebutt.funnotes.API.APIServiceConstructor;
import ru.eaglebutt.funnotes.DB.MainDB;
import ru.eaglebutt.funnotes.Model.Event;
import ru.eaglebutt.funnotes.Model.User;
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

        repository.loadDataFromDB();

        getAllButton.setOnClickListener(v -> {
           repository.getUserAndEvents(user.getEmail(), user.getPassword());
        });

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

        addEventButton.setOnClickListener(v -> {
            event.setTitle("Test");
            event.setId(0);
            repository.addEvent(user.getEmail(), user.getPassword(), event);
        });

        getEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());
            repository.getEvent(user.getEmail(), user.getPassword(), id);
        });

        deleteEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());
            repository.deleteEvent(user.getEmail(), user.getPassword(), id);
        });

        updateEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());
            event.setTitle("Updated");
            event.setId(id);
            repository.updateEvent(user.getEmail(), user.getPassword(), event);
        });


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