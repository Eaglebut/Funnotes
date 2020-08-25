package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentRegistrationBinding;
import ru.eaglebutt.funnotes.model.User;
import ru.eaglebutt.funnotes.repositories.UserRepository;

public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private UserRepository repository;
    private Button goNextButton;
    private EditText repeatPasswordEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private NavController controller;


    public RegistrationFragment() {

    }


    public static RegistrationFragment newInstance() {
        return new RegistrationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp();
        User user = new User();

        goNextButton.setOnClickListener(v -> {
            if (!emailEditText.getText().toString().isEmpty()
                    && !passwordEditText.getText().toString().isEmpty()
                    && !repeatPasswordEditText.getText().toString().isEmpty())
                if (passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())) {
                    user.setEmail(emailEditText.getText().toString());
                    user.setPassword(passwordEditText.getText().toString());
                    repository.getObservableUser().set(user);
                    controller.navigate(R.id.action_registrationFragment_to_secondRegistrationFragment);
                } else {
                    Snackbar.make(view, "Пароли должны совпадать", Snackbar.LENGTH_LONG).show();
                }
        });
    }

    private void setUp() {
        goNextButton = binding.goToSecondRegistrationButton;
        repeatPasswordEditText = binding.repeatPasswordEditTextRegistration;
        repository = UserRepository.getInstance(getContext());
        controller = NavHostFragment.findNavController(RegistrationFragment.this);
        emailEditText = binding.emailEditTextRegistration;
        passwordEditText = binding.passwordEditTextRegistration;
    }

}