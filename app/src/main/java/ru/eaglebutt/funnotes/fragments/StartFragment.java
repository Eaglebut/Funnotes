package ru.eaglebutt.funnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import ru.eaglebutt.funnotes.R;


public class StartFragment extends Fragment {

    NavController controller;

    public StartFragment() {
    }

    public static StartFragment newInstance() {
        return new StartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = NavHostFragment.findNavController(this);
        view.findViewById(R.id.go_to_entrance_button)
                .setOnClickListener(v -> controller.navigate(R.id.action_startFragment_to_entranceFragment));

        view.findViewById(R.id.go_to_registration_button)
                .setOnClickListener(v -> controller.navigate(R.id.action_startFragment_to_registrationFragment));
    }
}