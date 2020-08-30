package ru.eaglebutt.funnotes.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentEditTaskBinding;
import ru.eaglebutt.funnotes.model.Event;
import ru.eaglebutt.funnotes.repositories.EventRepository;


public class EditTaskFragment extends Fragment {


    private static final String ID = "id";

    private int id;
    private EventRepository repository;
    private FragmentEditTaskBinding binding;
    private long completionTime = 0;

    private Button saveButton;
    private Button cancelButton;
    private Button dateButton;
    private Calendar dateAndTime;

    public EditTaskFragment() {
    }


    public static EditTaskFragment newInstance(int id) {
        EditTaskFragment fragment = new EditTaskFragment();
        Bundle args = new Bundle();
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt(ID);
        }
        repository = EventRepository.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
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
        binding.setId(id);
        binding.setRepository(repository);
        saveButton = binding.saveTaskButtonEditTask;
        cancelButton = binding.cancelButtonEditTask;
        dateButton = binding.pickDateButtonEditTask;

        completionTime = repository.getEventList().get(id).getStartTime();
        dateAndTime = Calendar.getInstance();
        dateAndTime.setTimeInMillis(completionTime * 1_000);
        setTimeButton();

        saveButton.setOnClickListener(v -> {
            Event event = repository.getEventList().get(id);
            event.setTitle(binding.taskTitleEditTask.getText().toString());
            event.setDescription(binding.taskDescriptionEditTask.getText().toString());
            event.setStartTime(completionTime);
            repository.updateEvent(event);
            getActivity().onBackPressed();
        });

        cancelButton.setOnClickListener(v -> getActivity().onBackPressed());

        dateButton.setOnClickListener(v -> {
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
        completionTime = dateAndTime.getTimeInMillis() / 1_000;
    }


}