package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentProfileBinding;
import ru.eaglebutt.funnotes.repositories.UserRepository;


public class ProfileFragment extends Fragment {

    UserRepository repository;
    FragmentProfileBinding binding;
    Toolbar toolbar;

    public ProfileFragment() {
    }


    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = UserRepository.getInstance(view.getContext());
        binding.setRepository(repository);
        repository.getUserFromDB();
        repository.getUser();
        checkUser(view);
        setUpToolbar(view);

    }

    private void checkUser(View view) {
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

    protected void setUpToolbar(View view) {
        toolbar = view.getRootView().findViewById(R.id.app_toolbar);
        toolbar.setTitle("Профиль");
        toolbar.setSubtitle(null);
    }
}