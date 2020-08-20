package ru.eaglebutt.funnotes.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ru.eaglebutt.funnotes.DataRepository;
import ru.eaglebutt.funnotes.model.Event;

public class EventListViewModel extends AndroidViewModel {

    private LiveData<List<Event>> eventLiveData;
    private DataRepository repository;

    public EventListViewModel(@NonNull Application application) {
        super(application);
        repository = DataRepository.getInstance(application);
        eventLiveData = repository.getLiveEventList();
    }


    public LiveData<List<Event>> getEventLiveData() {
        return eventLiveData;
    }


}
