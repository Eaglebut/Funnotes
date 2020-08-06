package ru.eaglebutt.funnotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.eaglebutt.funnotes.API.APIConfig;
import ru.eaglebutt.funnotes.API.APIServiceConstructor;
import ru.eaglebutt.funnotes.API.AllUsersResponseData;
import ru.eaglebutt.funnotes.API.GetAllUserService;
import ru.eaglebutt.funnotes.DB.Event;
import ru.eaglebutt.funnotes.DB.EventDB;
import ru.eaglebutt.funnotes.DB.EventList;

public class MainActivity extends AppCompatActivity {

    EventDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //db = EventDB.get(getApplicationContext());
        //new myTask().execute();
        final TextView textView = findViewById(R.id.text_data);


        GetAllUserService service = APIServiceConstructor.createService(GetAllUserService.class);

        Call<AllUsersResponseData> dataCall = service.getAllUserData(APIConfig.email, APIConfig.password);

        dataCall.enqueue(new Callback<AllUsersResponseData>() {
            @Override
            public void onResponse(Call<AllUsersResponseData> call, Response<AllUsersResponseData> response) {
                if (response.body() != null){
                    textView.setText(response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<AllUsersResponseData> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                Log.d("Error API", t.getMessage() );
            }
        });
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
            //ListView listView = findViewById(R.id.list_view);
            //listView.setAdapter(adapter);
        }
    }
}