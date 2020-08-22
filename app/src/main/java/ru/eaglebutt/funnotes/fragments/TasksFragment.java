package ru.eaglebutt.funnotes.fragments;

import android.view.View;

import ru.eaglebutt.funnotes.R;


public class TasksFragment extends EventListFragment {

    public TasksFragment() {
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
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