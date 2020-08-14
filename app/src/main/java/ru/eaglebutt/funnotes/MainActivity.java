package ru.eaglebutt.funnotes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

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


        repository.loadDataFromDB();

        getAllButton.setOnClickListener(v -> {
           repository.getUserAndEvents(user.getEmail(), user.getPassword());
        });

        addUserButton.setOnClickListener(v -> {
            user.setName("Notchanged");
            repository.addUser(user);
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
            event.setLocalId(0);
            repository.addEvent(user.getEmail(), user.getPassword(), event);
        });


        deleteEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());
            repository.deleteEvent(user.getEmail(), user.getPassword(), id);
        });

        updateEventButton.setOnClickListener(v -> {
            int id = Integer.parseInt(editText.getText().toString().isEmpty() ? "0" : editText.getText().toString());
            event.setTitle("Updated");
            event.setLocalId(id);
            repository.updateEvent(user.getEmail(), user.getPassword(), event);
        });
    }
}