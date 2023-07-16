package com.robo_world.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.robo_world.R;
import com.robo_world.fragment.LoginFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new LoginFragment())
                .commit();
    }
}
