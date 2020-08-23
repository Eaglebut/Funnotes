package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.model.Event;
import ru.eaglebutt.funnotes.repositories.EventRepository;


public class NewTaskFragment extends Fragment {

    long completionTime = 0;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button dateButton;
    private CheckBox reminderCheckBox;
    private Button addButton;
    private Button cancelButton;
    private EventRepository eventRepository;
    private Toolbar toolbar;

    public NewTaskFragment() {
    }

    public static NewTaskFragment newInstance() {
        return new NewTaskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findAllViews(view);
        addButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            if (!title.isEmpty() && !description.isEmpty()) {
                Event event = new Event();
                event.setTitle(title);
                event.setDescription(description);
                event.setStartTime(completionTime);
                event.setEndTime(0);
                eventRepository.addEvent(event);
                getActivity().onBackPressed();
            }
        });

        cancelButton.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });
    }


    private void findAllViews(View view) {
        toolbar = view.getRootView().findViewById(R.id.app_toolbar);
        eventRepository = EventRepository.getInstance(view.getContext());
        titleEditText = view.findViewById(R.id.task_title_new_task);
        descriptionEditText = view.findViewById(R.id.task_description_new_task);
        dateButton = view.findViewById(R.id.pick_date_button_new_task);
        reminderCheckBox = view.findViewById(R.id.remind_check_box_new_task);
        addButton = view.findViewById(R.id.add_task_button_new_task);
        cancelButton = view.findViewById(R.id.cancel_button_new_task);
    }

}