package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.adapters.EventsAdapter;
import ru.eaglebutt.funnotes.repositories.EventRepository;
import ru.eaglebutt.funnotes.repositories.UserRepository;
import ru.eaglebutt.funnotes.view_models.EventListViewModel;


public abstract class EventListFragment<Binding extends ViewDataBinding> extends Fragment {
    protected EventRepository eventRepository;
    protected UserRepository userRepository;
    protected SwipeRefreshLayout refreshLayout;
    protected Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private EventsAdapter mAdapter;
    private EventListViewModel viewModel;
    protected Binding binding;
    private NavController controller;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    public abstract Binding onCreateBinding(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState);

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        binding = onCreateBinding(inflater, container, savedInstanceState);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUser(view);
        setUpRefreshLayout(view);
        getTasks();
        setUpRecyclerView(view);
        setUpViewModel();
        setUpToolbar(view);
    }

    private void checkUser(View view) {
        eventRepository = EventRepository.getInstance(view.getContext());
        userRepository = UserRepository.getInstance(view.getContext());
        userRepository.getUserFromDB();
        BottomNavigationView bottomNavigationView = view.getRootView().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);
        controller = NavHostFragment.findNavController(this);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (userRepository.getObservableUser().get() == null) {

            controller.navigate(R.id.action_to_startFragment);
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
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
        recyclerView = view.findViewById(R.id.today_recycler_view);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new EventsAdapter(eventRepository.getEventList(), controller);
        recyclerView.setAdapter(mAdapter);
    }

    protected abstract void setUpToolbar(View view);

    protected abstract void setUpRefreshLayout(View view);

    protected abstract void getTasks();


}