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
import ru.eaglebutt.funnotes.databinding.FragmentEditProfileBinding;
import ru.eaglebutt.funnotes.model.User;
import ru.eaglebutt.funnotes.repositories.UserRepository;


public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private UserRepository repository;
    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText emailEditText;
    private Button saveButton;
    private Button editPasswordButton;
    private Button cancelButton;
    private NavController controller;

    public EditProfileFragment() {
    }

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp();
    }

    private void setUp() {
        nameEditText = binding.nameEditTextEditProfile;
        surnameEditText = binding.surnameEditTextEditProfile;
        emailEditText = binding.emailEditTextEditProfile;
        saveButton = binding.saveButtonEditProfile;
        cancelButton = binding.cancelButtonEditProfile;
        editPasswordButton = binding.changePasswordButtonEditProfile;
        repository = UserRepository.getInstance(getContext());
        binding.setRepository(repository);
        controller = NavHostFragment.findNavController(this);

        saveButton.setOnClickListener(v -> {
            if (nameEditText.getText().toString().isEmpty() || surnameEditText.getText().toString().isEmpty() || emailEditText.getText().toString().isEmpty()) {
                Snackbar.make(getView(), R.string.missing_value_string, Snackbar.LENGTH_LONG).show();
            } else {
                User user = repository.getObservableUser().get();
                user.setName(nameEditText.getText().toString());
                user.setSurname(surnameEditText.getText().toString());
                user.setEmail(emailEditText.getText().toString());
                repository.updateUser(user);
                getActivity().onBackPressed();
            }
        });

        cancelButton.setOnClickListener(v -> getActivity().onBackPressed());

        editPasswordButton.setOnClickListener(v -> controller.navigate(R.id.action_editProfileFragment_to_editPasswordFragment));

    }


}