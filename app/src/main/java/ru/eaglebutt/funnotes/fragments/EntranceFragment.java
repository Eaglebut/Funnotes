package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.model.User;
import ru.eaglebutt.funnotes.repositories.UserRepository;


public class EntranceFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button logInButton;
    private Toolbar toolbar;
    private UserRepository repository;
    private NavController controller;
    private LiveData<User> liveUser;

    public EntranceFragment() {
    }

    public static EntranceFragment newInstance() {
        return new EntranceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
        logInButton.setOnClickListener(v -> {
            User user = new User();
            user.setEmail(emailEditText.getText().toString());
            user.setPassword(passwordEditText.getText().toString());
            if (user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
                Snackbar.make(view, R.string.missing_value_string, Snackbar.LENGTH_SHORT).show();
            } else {
                repository.logInUser(user);
            }
        });
    }

    private void setUp(View view) {

        repository = UserRepository.getInstance(view.getContext());
        liveUser = repository.getLiveUser();
        controller = NavHostFragment.findNavController(this);
        emailEditText = view.findViewById(R.id.email_edit_text_entrance);
        passwordEditText = view.findViewById(R.id.password_edit_text_entrance);
        logInButton = view.findViewById(R.id.log_in_button);
        toolbar = view.getRootView().findViewById(R.id.app_toolbar);
        liveUser.removeObservers(getViewLifecycleOwner());
        liveUser.observe(getViewLifecycleOwner(), user1 -> {
            if (user1 != null)
                controller.navigate(R.id.action_entranceFragment_to_todayFragment);
        });
    }
}