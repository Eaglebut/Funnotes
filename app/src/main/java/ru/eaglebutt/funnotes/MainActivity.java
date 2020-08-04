package ru.eaglebutt.funnotes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;

import ru.eaglebutt.funnotes.DB.Event;
import ru.eaglebutt.funnotes.DB.EventDB;
import ru.eaglebutt.funnotes.DB.EventList;

public class MainActivity extends AppCompatActivity {

    EventDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = EventDB.get(getApplicationContext());
        new myTask().execute();

    }


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
            ListView listView = findViewById(R.id.list_view);
            listView.setAdapter(adapter);
        }

    }

}