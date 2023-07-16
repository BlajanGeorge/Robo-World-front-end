package com.robo_world.fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.robo_world.utility.Constants.USER_PATH;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.robo_world.R;
import com.robo_world.http.HttpRequestTask;
import com.robo_world.model.ChangePasswordRequest;
import com.robo_world.model.ErrorResponse;
import com.robo_world.model.GetUserResponse;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Profile fragment
 *
 * @author Blajan George
 */
public class ProfileFragment extends Fragment {
    private View view;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.profile);
        navigationView.setOnNavigationItemSelectedListener(navListener);

        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);

        @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"}) HttpRequestTask getUserInfoTask = new HttpRequestTask(resp -> {
            if (resp.isSuccessful()) {
                GetUserResponse userResponse = new Gson().fromJson(resp.getBody(), GetUserResponse.class);

                LinearLayout emailLayout = view.findViewById(R.id.emailLayout);

                TextView email = new TextView(view.getContext());
                email.setText(userResponse.getEmail());
                email.setTextSize(16);
                email.setTypeface(null, Typeface.BOLD_ITALIC);
                emailLayout.addView(email);
                emailLayout.setGravity(Gravity.CENTER);

                LinearLayout firstNameLayout = view.findViewById(R.id.firstNameLayout);

                TextView firstName = new TextView(view.getContext());
                firstName.setText(userResponse.getFirstName());
                firstName.setTextSize(16);
                firstName.setTypeface(null, Typeface.BOLD_ITALIC);
                firstNameLayout.addView(firstName);
                firstNameLayout.setGravity(Gravity.CENTER);

                LinearLayout lastNameLayout = view.findViewById(R.id.lastNameLayout);

                TextView lastName = new TextView(view.getContext());
                lastName.setText(userResponse.getLastName());
                lastName.setTextSize(16);
                lastName.setTypeface(null, Typeface.BOLD_ITALIC);
                lastNameLayout.addView(lastName);
                lastNameLayout.setGravity(Gravity.CENTER);

                LinearLayout botsNumberLayout = view.findViewById(R.id.botsNumberLayout);

                TextView botsNumber = new TextView(view.getContext());
                botsNumber.setText(userResponse.getBotsNumber().toString());
                botsNumber.setTextSize(16);
                botsNumber.setTypeface(null, Typeface.BOLD_ITALIC);
                botsNumberLayout.addView(botsNumber);
                botsNumberLayout.setGravity(Gravity.CENTER);

                LinearLayout friendsNumberLayout = view.findViewById(R.id.friendsNumberLayout);

                TextView friendsNumber = new TextView(view.getContext());
                friendsNumber.setText(userResponse.getFriendsNumber().toString());
                friendsNumber.setTextSize(16);
                friendsNumber.setTypeface(null, Typeface.BOLD_ITALIC);
                friendsNumberLayout.addView(friendsNumber);
                friendsNumberLayout.setGravity(Gravity.CENTER);
            }
        });

        getUserInfoTask
                .execute(new Request
                        .Builder()
                        .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                        .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0))
                        .build()
                );

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().beginTransaction().replace(this.getId(), LoginFragment.class, null).setReorderingAllowed(true).commit();
        });

        Button changePassButton = view.findViewById(R.id.resetButton);
        changePassButton.setOnClickListener(v -> {
            EditText currentPass = view.findViewById(R.id.currentPassword);
            EditText newPass = view.findViewById(R.id.newPassword);

            @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"}) HttpRequestTask changePassTask = new HttpRequestTask(resp -> {
                if (resp.isSuccessful()) {
                    showSuccessConnectionSnackBar("Password changed");
                } else {
                    ErrorResponse response = new Gson().fromJson(resp.getBody(), ErrorResponse.class);
                    showSelectBotSnackBarError(response.getError().getMessage());
                }
            });

            changePassTask
                    .execute(new Request
                            .Builder()
                            .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                            .post(RequestBody
                                    .create(new Gson()
                                                    .toJson(new ChangePasswordRequest(
                                                            currentPass.getText().toString(),
                                                            newPass.getText().toString())),
                                            MediaType.parse("application/json; charset=utf-8")))
                            .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/change-password")
                            .build()
                    );
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.profile);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        if (itemId == R.id.bots) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).setReorderingAllowed(true).addToBackStack("profile_to_bots").commit();
        }

        if (itemId == R.id.friends) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).addToBackStack("profile_to_friends").commit();
        }

        if (itemId == R.id.home_item) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), HomeFragment.class, null).setReorderingAllowed(true).addToBackStack("profile_to_home").commit();
        }

        if (itemId == R.id.chat) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), ChatFragment.class, null).setReorderingAllowed(true).addToBackStack("profile_to_chat").commit();
        }

        return true;
    };

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showSelectBotSnackBarError(String msg) {
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showSuccessConnectionSnackBar(String msg) {
        SpannableStringBuilder builder = new SpannableStringBuilder("    " + msg);
        builder.setSpan(new ImageSpan(view.getContext(), R.drawable.snack_bar_info), 0, 1, 0);

        Snackbar snackbar = Snackbar.make(view, builder, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();

        FrameLayout.LayoutParams snackBackLayoutParams = (FrameLayout.LayoutParams) snackBarView.getLayoutParams();
        snackBackLayoutParams.setMargins(25, 25, 25, 25);

        snackBarView.setLayoutParams(snackBackLayoutParams);
        snackBarView.setBackground(getResources().getDrawable(R.drawable.snack_bar_info_shape));

        snackbar.show();
    }
}
