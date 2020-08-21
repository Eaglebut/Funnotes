package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.eaglebutt.funnotes.DataRepository;
import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.adapters.EventsAdapter;
import ru.eaglebutt.funnotes.view_models.EventListViewModel;


public class TodayFragment extends Fragment {

    private DataRepository repository;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private EventsAdapter mAdapter;
    private EventListViewModel viewModel;
    private SwipeRefreshLayout refreshLayout;
    private Toolbar toolbar;

    public static TodayFragment newInstance(String param1, String param2) {
        return new TodayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUser(view);
        setUpRefreshLayout(view);
        repository.getUserAndEvents();
        setUpRecyclerView(view);
        setUpViewModel();
        setUpToolbar(view);
    }


    private void setUpToolbar(View view) {
        toolbar = view.getRootView().findViewById(R.id.app_toolbar);
        toolbar.setTitle("Мой день");
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DATE_FIELD);
        String strDate = dateFormat.format(date);
        toolbar.setTitle("Мой день");
        toolbar.setSubtitle(strDate);
    }

    private void setUpViewModel() {
        viewModel = new ViewModelProvider(this,
                ViewModelProvider
                        .AndroidViewModelFactory
                        .getInstance(getActivity()
                                .getApplication()))
                .get(EventListViewModel.class);

        viewModel.getEventLiveData().observe(getViewLifecycleOwner(), events -> {
            mAdapter.setData(events);
            refreshLayout.setRefreshing(false);
        });
    }

    private void setUpRecyclerView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.today_recycler_view);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new EventsAdapter(repository.getEventList());
        recyclerView.setAdapter(mAdapter);
    }

    private void setUpRefreshLayout(View view) {
        refreshLayout = view.findViewById(R.id.today_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> repository.getTodayTasks());
    }

    private void checkUser(View view) {
        repository = DataRepository.getInstance(view.getContext());
        BottomNavigationView bottomNavigationView = view.getRootView().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (repository.getObservableUser().get() == null) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_todayFragment_to_startFragment);
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
    }
}