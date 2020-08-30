package ru.eaglebutt.funnotes.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateUtils;
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

import java.util.Calendar;

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
    private Calendar dateAndTime;

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

    private TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        setTimeButton();
    };
    private DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
        dateAndTime.set(Calendar.YEAR, year);
        dateAndTime.set(Calendar.MONTH, month);
        dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        setTime();
    };

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

        dateButton.setOnClickListener(v -> {
            dateAndTime = Calendar.getInstance();
            setDate();
        });
    }

    public void setDate() {
        DatePickerDialog dialog = new DatePickerDialog(getContext(), dateSetListener,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH));
        dialog.show();
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(R.color.colorPrimaryText, null));
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(getResources().getColor(R.color.colorPrimaryText, null));

    }

    public void setTime() {
        TimePickerDialog dialog = new TimePickerDialog(getContext(), timeSetListener,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true);
        dialog.show();
        dialog.getButton(TimePickerDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(R.color.colorPrimaryText, null));
        dialog.getButton(TimePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(getResources().getColor(R.color.colorPrimaryText, null));

    }

    private void setTimeButton() {
        dateButton.setText(DateUtils.formatDateTime(getContext(),
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
        completionTime = dateAndTime.getTimeInMillis() / 1000;
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