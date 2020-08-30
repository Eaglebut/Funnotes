package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentProfileBinding;
import ru.eaglebutt.funnotes.repositories.UserRepository;


public class ProfileFragment extends Fragment {

    UserRepository repository;
    FragmentProfileBinding binding;
    Toolbar toolbar;
    NavController controller;

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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = UserRepository.getInstance(view.getContext());
        binding.setRepository(repository);
        repository.getUserFromDB();
        repository.getUser();
        controller = NavHostFragment.findNavController(this);
        checkUser(view);
        setUpToolbar(view);

        Button exitButton = view.findViewById(R.id.exit_button);
        exitButton.setOnClickListener(v -> {
            repository.logOut();
            checkUser(view);

        });

        binding.editProfileButton.setOnClickListener(v -> {
            controller.navigate(R.id.action_profileFragment_to_editProfileFragment);
        });
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
            controller.navigate(R.id.action_to_startFragment);
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
    }

    protected void setUpToolbar(View view) {
        toolbar = view.getRootView().findViewById(R.id.app_toolbar);
        toolbar.setTitle("Профиль");
        toolbar.setSubtitle(null);
    }
}