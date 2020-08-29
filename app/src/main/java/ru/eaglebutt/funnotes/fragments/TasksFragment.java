package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentEventListBinding;


public class TasksFragment extends EventListFragment<FragmentEventListBinding> {

    public TasksFragment() {
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }


    @NonNull
    @Override
    public FragmentEventListBinding onCreateBinding(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding;
    }

    @Override
    protected void setUpToolbar(View view) {
        toolbar = view.getRootView().findViewById(R.id.app_toolbar);
        toolbar.setTitle("Задачи");
        toolbar.setSubtitle(null);
    }

    @Override
    protected void setUpRefreshLayout(View view) {
        refreshLayout = view.findViewById(R.id.today_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> eventRepository.getAllEvents());
    }

    @Override
    protected void getTasks() {
        eventRepository.getAllEvents();
    }
}