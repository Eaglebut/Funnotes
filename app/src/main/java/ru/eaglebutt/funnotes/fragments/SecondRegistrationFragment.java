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
import androidx.navigation.fragment.NavHostFragment;

import ru.eaglebutt.funnotes.R;

public class SecondRegistrationFragment extends Fragment {


    private Button registerButton;
    private EditText nameEditText;
    private EditText surnameEditText;


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
        return inflater.inflate(R.layout.fragment_second_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.register_button).setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondRegistrationFragment.this)
                    .navigate(R.id.action_secondRegistrationFragment_to_entranceFragment);
        });
    }

    private void setUp() {

    }
}