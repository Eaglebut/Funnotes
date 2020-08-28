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
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentSecondRegistrationBinding;
import ru.eaglebutt.funnotes.model.User;
import ru.eaglebutt.funnotes.repositories.UserRepository;

public class SecondRegistrationFragment extends Fragment {


    private Button registerButton;
    private EditText nameEditText;
    private EditText surnameEditText;
    FragmentSecondRegistrationBinding binding;
    NavController controller;
    UserRepository repository;
    LiveData<User> liveUser;

    public SecondRegistrationFragment() {
    }

    public static SecondRegistrationFragment newInstance() {
        return new SecondRegistrationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSecondRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp();

        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String surname = surnameEditText.getText().toString();
            if (name.isEmpty() || surname.isEmpty()) {
                Snackbar.make(getView(), R.string.missing_value_string, Snackbar.LENGTH_LONG).show();
            } else {
                User user = repository.getObservableUser().get();
                if (user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
                    Snackbar.make(getView(), "Ошибка", Snackbar.LENGTH_LONG).show();
                } else {
                    user.setName(name);
                    user.setSurname(surname);
                    liveUser = repository.getLiveUser();
                    liveUser.observe(getViewLifecycleOwner(), user1 -> {
                        if (user1 != null) {
                            controller.navigate(R.id.action_secondRegistrationFragment_to_entranceFragment);
                        }
                    });
                    repository.addUser(user);
                }
            }


        });
    }


    private void setUp() {
        repository = UserRepository.getInstance(getContext());
        registerButton = binding.registerButton;
        nameEditText = binding.nameEditText;
        surnameEditText = binding.surnameEditText;
        controller = NavHostFragment.findNavController(SecondRegistrationFragment.this);

    }
}