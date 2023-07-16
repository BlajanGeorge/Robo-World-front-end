package com.robo_world.fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.robo_world.utility.Constants.USER_PATH;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robo_world.R;
import com.robo_world.http.AsyncResponse;
import com.robo_world.http.HttpRequestTask;
import com.robo_world.http.HttpResponse;
import com.robo_world.model.GetBotsResponse;
import com.robo_world.model.BotRequest;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Bots fragment
 *
 * @author Blajan George
 */
public class BotsFragment extends Fragment implements AsyncResponse {
    private View view;
    private LinearLayout botsListLayout;
    private SharedPreferences sharedPreferences;

    public BotsFragment() {
        super(R.layout.fragment_bots);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.bots);
        navigationView.setOnNavigationItemSelectedListener(navListener);

        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", 0);
        botsListLayout = view.findViewById(R.id.botsListLayout);

        @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(this);

        requestTask
                .execute(new Request
                        .Builder()
                        .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                        .url(USER_PATH + "/" + userId + "/bots")
                        .build()
                );

        ImageButton addBot = view.findViewById(R.id.addBotButton);
        addBot.setOnClickListener(v -> {
            EditText nameText = new EditText(view.getContext());
            nameText.setInputType(InputType.TYPE_CLASS_TEXT);
            nameText.setHint("Name");
            nameText.setHintTextColor(Color.GRAY);
            nameText.setTextColor(Color.GRAY);
            nameText.setGravity(Gravity.CENTER_HORIZONTAL);
            nameText
                    .getBackground()
                    .mutate()
                    .setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder
                    .setMessage("Create bot")
                    .setView(nameText)
                    .setPositiveButton("Confirm", (dialog, id) -> {
                        @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask createBotTask = new HttpRequestTask(response1 -> {
                            if (response1.isSuccessful()) {
                                showSuccessConnectionSnackBar("Bot created");
                                getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).setReorderingAllowed(true).commit();
                            } else {
                                showSelectBotSnackBarError("Bot creation failed");
                            }
                        });

                        createBotTask
                                .execute(new Request
                                        .Builder()
                                        .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                        .post(RequestBody
                                                .create(new Gson()
                                                                .toJson(new BotRequest(
                                                                        nameText.getText().toString())),
                                                        MediaType.parse("application/json; charset=utf-8")))
                                        .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/bots")
                                        .build()
                                );
                    })
                    .setNegativeButton("Cancel", ((dialog, which) -> {
                    }));

            builder.create().show();
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        if (itemId == R.id.home_item) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), HomeFragment.class, null).setReorderingAllowed(true).addToBackStack("bots_to_home").commit();
        }

        if (itemId == R.id.friends) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).addToBackStack("bots_to_friends").commit();
        }

        if (itemId == R.id.profile) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), ProfileFragment.class, null).setReorderingAllowed(true).addToBackStack("bots_to_profile").commit();
        }

        if (itemId == R.id.chat) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), ChatFragment.class, null).setReorderingAllowed(true).addToBackStack("bots_to_chat").commit();
        }

        return true;
    };

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.bots);
    }

    @Override
    public void processFinish(HttpResponse response) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<GetBotsResponse>>() {
        }.getType();
        List<GetBotsResponse> bots = gson.fromJson(response.getBody(), listType);

        for (GetBotsResponse bot : bots) {
            ImageView botIcon = new ImageView(view.getContext());
            botIcon.setBackgroundResource(R.drawable.ic_bot_24_white);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50, 20, 0, 0);
            lp.height = 100;
            lp.width = 100;
            botIcon.setLayoutParams(lp);

            LinearLayout botLayout = new LinearLayout(view.getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150);
            layoutParams.setMargins(50, 50, 50, 50);
            botLayout.setLayoutParams(layoutParams);
            botLayout.setOrientation(LinearLayout.HORIZONTAL);
            botLayout.setBackground(getResources().getDrawable(R.drawable.selected_robot_state_background));
            botLayout.addView(botIcon);

            LinearLayout textLayout = new LinearLayout(view.getContext());
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(150, ViewGroup.LayoutParams.MATCH_PARENT);
            textLp.setMargins(50, 0, 50, 0);
            textLayout.setLayoutParams(textLp);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            TextView botName = new TextView(view.getContext());
            botName.setText(bot.getName());
            botName.setTextSize(16);
            botName.setTypeface(null, Typeface.BOLD_ITALIC);
            textLayout.addView(botName);
            textLayout.setGravity(Gravity.CENTER);

            botLayout.addView(textLayout);

            if (!bot.isSelected()) {
                ImageButton selectButton = new ImageButton(view.getContext());
                selectButton.setBackground(getResources().getDrawable(R.drawable.ic_select));
                LinearLayout.LayoutParams selectLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                selectLp.setMargins(20, 20, 0, 0);
                selectButton.setLayoutParams(selectLp);
                botLayout.addView(selectButton);

                selectButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    builder.setMessage("Please confirm bot selection")
                            .setPositiveButton("Confirm", (dialog, id) -> {

                                @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(response1 -> {
                                    if (response1.isSuccessful()) {
                                        showSuccessConnectionSnackBar("Bot selected");
                                        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("selected_bot_name", bot.getName());
                                        editor.apply();
                                        getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).setReorderingAllowed(true).commit();
                                    } else {
                                        showSelectBotSnackBarError("Bot selection failed");
                                    }
                                });

                                requestTask
                                        .execute(new Request
                                                .Builder()
                                                .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                                .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/bots/" + bot.getId() + "/select")
                                                .build()
                                        );
                            })
                            .setNegativeButton("Cancel", (dialog, id) -> {
                                // User cancelled the dialog
                            });

                    builder.create().show();
                });
            } else {
                textLp.setMargins(50, 0, 170, 0);
                textLayout.setLayoutParams(textLp);
            }

            ImageButton editButton = new ImageButton(view.getContext());
            editButton.setBackground(getResources().getDrawable(R.drawable.ic_edit));
            LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            editLp.setMargins(20, 20, 0, 0);
            editButton.setLayoutParams(editLp);
            botLayout.addView(editButton);

            editButton.setOnClickListener(v -> {
                EditText nameText = new EditText(view.getContext());
                nameText.setInputType(InputType.TYPE_CLASS_TEXT);
                nameText.setText(bot.getName());
                nameText.setTextColor(Color.GRAY);
                nameText.setGravity(Gravity.CENTER_HORIZONTAL);
                nameText
                        .getBackground()
                        .mutate()
                        .setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                builder
                        .setMessage("Update bot")
                        .setView(nameText)
                        .setPositiveButton("Confirm", (dialog, id) -> {
                            @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(response1 -> {
                                if (response1.isSuccessful()) {
                                    showSuccessConnectionSnackBar("Bot updated");
                                    if (bot.isSelected()) {
                                        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("selected_bot_name", nameText.getText().toString());
                                        editor.apply();
                                    }
                                    getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).setReorderingAllowed(true).commit();
                                } else {
                                    showSelectBotSnackBarError("Bot update failed");
                                }
                            });

                            requestTask
                                    .execute(new Request
                                            .Builder()
                                            .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                            .put(RequestBody
                                                    .create(new Gson()
                                                                    .toJson(new BotRequest(
                                                                            nameText.getText().toString())),
                                                            MediaType.parse("application/json; charset=utf-8")))
                                            .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/bots/" + bot.getId())
                                            .build()
                                    );
                        })
                        .setNegativeButton("Cancel", ((dialog, which) -> {
                        }));

                builder.create().show();
            });

            ImageButton deleteButton = new ImageButton(view.getContext());
            deleteButton.setBackground(getResources().getDrawable(R.drawable.ic_delete_24));
            LinearLayout.LayoutParams deleteLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            deleteLp.setMargins(20, 20, 0, 0);
            deleteButton.setLayoutParams(deleteLp);
            botLayout.addView(deleteButton);

            deleteButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                builder.setMessage("Please confirm bot deletion")
                        .setPositiveButton("Confirm", (dialog, id) -> {

                            @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(response1 -> {
                                if (response1.isSuccessful()) {
                                    showSuccessConnectionSnackBar("Bot deleted");
                                    if (bot.isSelected()) {
                                        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("selected_bot_name", null);
                                        editor.apply();
                                    }
                                    getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).setReorderingAllowed(true).commit();
                                } else {
                                    showSelectBotSnackBarError("Bot deletion failed");
                                }
                            });

                            requestTask
                                    .execute(new Request
                                            .Builder()
                                            .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                            .delete()
                                            .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/bots/" + bot.getId())
                                            .build()
                                    );
                        })
                        .setNegativeButton("Cancel", (dialog, id) -> {
                            // User cancelled the dialog
                        });
                builder.create().show();
            });

            botsListLayout.addView(botLayout);
        }
    }

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
