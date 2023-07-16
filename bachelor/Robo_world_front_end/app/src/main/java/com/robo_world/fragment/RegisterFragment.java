package com.robo_world.fragment;

import static com.robo_world.utility.Constants.USER_PATH;

import android.annotation.SuppressLint;
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
import com.robo_world.model.RegisterRequest;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Register fragment
 *
 * @author Blajan George
 */
public class RegisterFragment extends Fragment implements AsyncResponse {
    private View view;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        TextView loginHook = view.findViewById(R.id.loginHook);
        loginHook.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(this.getId(), LoginFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("register_to_login")
                    .commit();
        });

        Button registerButton = view.findViewById(R.id.signUpButton);
        registerButton.setOnClickListener(v -> {
            EditText emailEditText = view.findViewById(R.id.signUpEmail);
            EditText passwordEditText = view.findViewById(R.id.signUpPassword);
            EditText lastNameEditText = view.findViewById(R.id.signUpLastName);
            EditText firstNameEditText = view.findViewById(R.id.signUpFirstName);

            @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(this);

            requestTask
                    .execute(new Request
                            .Builder()
                            .post(RequestBody
                                    .create(new Gson()
                                                    .toJson(new RegisterRequest(
                                                            emailEditText.getText().toString(),
                                                            passwordEditText.getText().toString(),
                                                            firstNameEditText.getText().toString(),
                                                            lastNameEditText.getText().toString())),
                                            MediaType.parse("application/json; charset=utf-8")))
                            .url(USER_PATH)
                            .build()
                    );
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void failedLogin(View view,
                             String msg) {
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
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(this.getId(), LoginFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("register_to_login")
                    .commit();
        } else {
            Log.e(this.toString(), "Login request failed " + response.getBody());
            String msg = null;
            if (response.getStatus() == 409) {
                msg = "This email is already in use";
            }
            if (response.getStatus() == 400) {
                msg = new Gson().fromJson(response.getBody(), ErrorResponse.class).getError().getMessage();
            }
            failedLogin(view, msg);
        }
    }
}