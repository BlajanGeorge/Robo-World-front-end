package com.robo_world.fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.robo_world.utility.Constants.LOGIN_PATH;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.robo_world.R;
import com.robo_world.http.AsyncResponse;
import com.robo_world.http.HttpRequestTask;
import com.robo_world.http.HttpResponse;
import com.robo_world.model.ErrorResponse;
import com.robo_world.model.LoginRequest;
import com.robo_world.model.LoginResponse;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Login fragment
 *
 * @author Blajan George
 */
public class LoginFragment extends Fragment implements AsyncResponse {
    private View view;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        TextView signUpHook = view.findViewById(R.id.signUpHook);
        signUpHook.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(this.getId(), RegisterFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("login_to_register")
                    .commit();
        });

        Button loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            EditText emailEditText = view.findViewById(R.id.loginEmail);
            EditText passwordEditText = view.findViewById(R.id.loginPassword);

            @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(this);

            requestTask
                    .execute(new Request
                            .Builder()
                            .post(RequestBody
                                    .create(new Gson()
                                                    .toJson(new LoginRequest(
                                                            emailEditText.getText().toString(),
                                                            passwordEditText.getText().toString())),
                                            MediaType.parse("application/json; charset=utf-8")))
                            .url(LOGIN_PATH)
                            .build()
                    );
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void failedLogin(View view, String msg) {
        SpannableStringBuilder builder = new SpannableStringBuilder("    " + msg);
        builder.setSpan(new ImageSpan(view.getContext(), R.drawable.snack_bar_err), 0, 1, 0);

        Snackbar snackbar = Snackbar.make(view, builder, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();

        FrameLayout.LayoutParams snackBackLayoutParams = (FrameLayout.LayoutParams) snackBarView.getLayoutParams();
        snackBackLayoutParams.setMargins(25, 25, 25, 25);

        snackBarView.setLayoutParams(snackBackLayoutParams);
        snackBarView.setBackground(getResources().getDrawable(R.drawable.snackbar_shape));

        snackbar.show();
    }

    @Override
    public void processFinish(HttpResponse response) {
        if (response.isSuccessful()) {
            Log.i(this.toString(), "Login request was successful " + response.getBody());
            SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
            LoginResponse loginResponse = new Gson().fromJson(response.getBody(), LoginResponse.class);

            if (loginResponse.getRole().equals("CUSTOMER")) {
                editor.putString("selected_bot_name", loginResponse.getBotName());
            }
            editor.putString("role", loginResponse.getRole());
            editor.putInt("id", loginResponse.getId());
            editor.putString("access_token", loginResponse.getToken());
            editor.putString("refresh_token", loginResponse.getRefreshToken());
            editor.apply();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(this.getId(), HomeFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("login_to_home")
                    .commit();
        } else {
            Log.e(this.toString(), "Login request failed " + response.getBody());
            if (response.getStatus() == 403) {
                failedLogin(view, "Wrong password");
            } else {
                failedLogin(view, new Gson().fromJson(response.getBody(), ErrorResponse.class).getError().getMessage());
            }
        }
    }
}