package ru.eaglebutt.funnotes.fragments;

import android.view.View;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.eaglebutt.funnotes.R;


public class TodayFragment extends EventListFragment {


    public static TodayFragment newInstance() {
        return new TodayFragment();
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