package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import ru.eaglebutt.funnotes.R;

public class RegistrationFragment extends Fragment {


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
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.go_to_second_registration_button).setOnClickListener(v -> {
            NavHostFragment.findNavController(RegistrationFragment.this)
                    .navigate(R.id.action_registrationFragment_to_secondRegistrationFragment);
        });
    }
}