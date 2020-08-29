package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentEventListBinding;


public class TodayFragment extends EventListFragment<FragmentEventListBinding> {


    public static TodayFragment newInstance() {
        return new TodayFragment();
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
        toolbar.setTitle("Мой день");
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DATE_FIELD);
        String strDate = dateFormat.format(date);
        toolbar.setTitle("Мой день");
        toolbar.setSubtitle(strDate);
    }

    @Override
    protected void setUpRefreshLayout(View view) {
        refreshLayout = view.findViewById(R.id.today_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> eventRepository.getTodayTasks());
    }

    @Override
    protected void getTasks() {
        eventRepository.getTodayTasks();
    }
}