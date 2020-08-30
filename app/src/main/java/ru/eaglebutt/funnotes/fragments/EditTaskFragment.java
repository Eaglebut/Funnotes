package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.eaglebutt.funnotes.databinding.FragmentEditTaskBinding;
import ru.eaglebutt.funnotes.model.Event;
import ru.eaglebutt.funnotes.repositories.EventRepository;


public class EditTaskFragment extends Fragment {


    private static final String ID = "id";

    private int id;
    private EventRepository repository;
    private FragmentEditTaskBinding binding;

    private Button saveButton;
    private Button cancelButton;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setId(id);
        binding.setRepository(repository);
        saveButton = binding.saveTaskButtonEditTask;
        cancelButton = binding.cancelButtonEditTask;

        saveButton.setOnClickListener(v -> {
            Event event = repository.getEventList().get(id);
            event.setTitle(binding.taskTitleEditTask.getText().toString());
            event.setDescription(binding.taskDescriptionEditTask.getText().toString());
            repository.updateEvent(event);
            getActivity().onBackPressed();
        });

        cancelButton.setOnClickListener(v -> getActivity().onBackPressed());

    }
}