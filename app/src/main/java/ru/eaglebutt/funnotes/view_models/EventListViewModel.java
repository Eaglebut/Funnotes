package ru.eaglebutt.funnotes.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ru.eaglebutt.funnotes.model.Event;
import ru.eaglebutt.funnotes.repositories.EventRepository;

public class EventListViewModel extends AndroidViewModel {

    private LiveData<List<Event>> eventLiveData;
    private EventRepository repository;

    public EventListViewModel(@NonNull Application application) {
        super(application);
        repository = EventRepository.getInstance(application);
        eventLiveData = repository.getLiveEventList();
    }

    public LiveData<List<Event>> getEventLiveData() {
        return eventLiveData;
    }

}
