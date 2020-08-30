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

import com.google.android.material.snackbar.Snackbar;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.databinding.FragmentEditPasswordBinding;
import ru.eaglebutt.funnotes.model.User;
import ru.eaglebutt.funnotes.repositories.UserRepository;


public class EditPasswordFragment extends Fragment {

    private FragmentEditPasswordBinding binding;
    private UserRepository repository;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText repeatNewPasswordEditText;
    private Button saveButton;
    private Button cancelButton;


    public EditPasswordFragment() {
    }


    public static EditPasswordFragment newInstance() {
        return new EditPasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp();

    }

    private void setUp() {
        repository = UserRepository.getInstance(getContext());
        oldPasswordEditText = binding.oldPasswordEditTextEditPassword;
        newPasswordEditText = binding.newPasswordEditTextEditPassword;
        repeatNewPasswordEditText = binding.repeatNewPasswordEditTextEditPassword;
        saveButton = binding.saveButtonEditPassword;
        cancelButton = binding.cancelButtonEditPassword;

        saveButton.setOnClickListener(v -> {
            String oldPassword = oldPasswordEditText.getText().toString();
            String newPassword = newPasswordEditText.getText().toString();
            String repeatNewPassword = repeatNewPasswordEditText.getText().toString();
            User user = repository.getObservableUser().get();

            if (newPassword.isEmpty() || oldPassword.isEmpty() || repeatNewPassword.isEmpty()) {
                Snackbar.make(getView(), R.string.missing_value_string, Snackbar.LENGTH_LONG).show();
            } else if (!oldPassword.equals(user.getPassword())) {
                Snackbar.make(getView(), R.string.wrong_password_string, Snackbar.LENGTH_LONG).show();
            } else if (!newPassword.equals(repeatNewPassword)) {
                Snackbar.make(getView(), R.string.passwords_not_match_string, Snackbar.LENGTH_LONG).show();
            } else {
                user.setPassword(newPassword);
                repository.updateUser(user);
                getActivity().onBackPressed();
            }
        });

        cancelButton.setOnClickListener(v -> getActivity().onBackPressed());


    }
}