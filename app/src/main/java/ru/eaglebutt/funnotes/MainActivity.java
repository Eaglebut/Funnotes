package ru.eaglebutt.funnotes;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private DataManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        manager = DataManager.getInstance(this.getApplicationContext());
        manager.getUserFromDB();
        /*User user = new User();
        user.setEmail("test@test.test");
        user.setPassword("test");
        user.setName("Adwdw");
        user.setSurname("Adawadw");
        manager.addUser(user);*/
    }
}