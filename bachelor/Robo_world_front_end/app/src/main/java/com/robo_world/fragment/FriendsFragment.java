package com.robo_world.fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.robo_world.utility.Constants.SERVER_ADDRESS;
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
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robo_world.R;
import com.robo_world.http.HttpRequestTask;
import com.robo_world.model.ErrorResponse;
import com.robo_world.model.FriendRequest;
import com.robo_world.model.FriendRequestDecision;
import com.robo_world.model.GetFriendRequestResponse;
import com.robo_world.model.GetFriendResponse;
import com.robo_world.model.Message;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

/**
 * Friends fragment
 *
 * @author Blajan George
 */
public class FriendsFragment extends Fragment {
    private View view;
    private LinearLayout listLayout;
    private SharedPreferences sharedPreferences;
    private StompClient stompClient;

    public FriendsFragment() {
        super(R.layout.fragment_friends);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        this.listLayout = view.findViewById(R.id.friendsListLayout);
        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);

        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.friends);
        navigationView.setOnNavigationItemSelectedListener(navListener);

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + SERVER_ADDRESS + "/chat");
        stompClient.connect();

        TabLayout friendsTab = view.findViewById(R.id.tabLayout);
        friendsTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("Friends")) {
                    ImageButton addFriendButton = view.findViewById(R.id.addFriendButton);
                    addFriendButton.setVisibility(View.VISIBLE);

                    listenerOnAddButton(addFriendButton);
                    populateFriendsList();
                }

                if (tab.getText().equals("Friend requests")) {
                    ImageButton addFriendButton = view.findViewById(R.id.addFriendButton);
                    addFriendButton.setVisibility(View.INVISIBLE);

                    populateFriendRequestsList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //ignore
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getText().equals("Friends")) {
                    ImageButton addFriendButton = view.findViewById(R.id.addFriendButton);
                    addFriendButton.setVisibility(View.VISIBLE);

                    listenerOnAddButton(addFriendButton);
                    populateFriendsList();
                }

                if (tab.getText().equals("Friend requests")) {
                    ImageButton addFriendButton = view.findViewById(R.id.addFriendButton);
                    addFriendButton.setVisibility(View.INVISIBLE);

                    populateFriendRequestsList();
                }
            }
        });

        TabLayout.Tab firstTab = friendsTab.getTabAt(0);
        firstTab.select();
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.friends);
    }

    private void populateFriendRequestsList() {
        listLayout.removeAllViews();


        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", 0);

        @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(response -> {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<GetFriendRequestResponse>>() {
            }.getType();
            List<GetFriendRequestResponse> friendRequests = gson.fromJson(response.getBody(), listType);

            for (GetFriendRequestResponse requestResponse : friendRequests) {
                ImageView friendIcon = new ImageView(view.getContext());
                friendIcon.setBackgroundResource(R.drawable.ic_friend);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50, 20, 0, 0);
                lp.height = 100;
                lp.width = 100;
                friendIcon.setLayoutParams(lp);

                LinearLayout friendLayout = new LinearLayout(view.getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150);
                layoutParams.setMargins(50, 50, 50, 50);
                friendLayout.setLayoutParams(layoutParams);
                friendLayout.setOrientation(LinearLayout.HORIZONTAL);
                friendLayout.setBackground(getResources().getDrawable(R.drawable.selected_robot_state_background));
                friendLayout.addView(friendIcon);

                LinearLayout textLayout = new LinearLayout(view.getContext());
                LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(200, ViewGroup.LayoutParams.MATCH_PARENT);
                textLp.setMargins(50, 0, 50, 0);
                textLayout.setLayoutParams(textLp);
                textLayout.setOrientation(LinearLayout.VERTICAL);
                TextView botName = new TextView(view.getContext());
                botName.setText(requestResponse.getLastName() + " " + requestResponse.getFirstName() + " #" + requestResponse.getRequesterId());
                botName.setTextSize(16);
                botName.setTypeface(null, Typeface.BOLD_ITALIC);
                textLayout.addView(botName);
                textLayout.setGravity(Gravity.CENTER);

                friendLayout.addView(textLayout);
                listLayout.addView(friendLayout);

                ImageButton acceptButton = new ImageButton(view.getContext());
                acceptButton.setBackground(getResources().getDrawable(R.drawable.ic_accept));
                LinearLayout.LayoutParams acceptLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                acceptLp.setMargins(20, 20, 0, 0);
                acceptButton.setLayoutParams(acceptLp);
                friendLayout.addView(acceptButton);

                acceptButton.setOnClickListener(v -> {
                    @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask acceptRequestTask = new HttpRequestTask(response1 -> {
                        if (response1.isSuccessful()) {
                            showSnackBarSuccess("Friend request accepted");
                            getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                        }
                    });

                    acceptRequestTask
                            .execute(new Request
                                    .Builder()
                                    .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                    .post(RequestBody
                                            .create(new Gson()
                                                            .toJson(new FriendRequestDecision(
                                                                    requestResponse.getRequestId(),
                                                                    true)),
                                                    MediaType.parse("application/json; charset=utf-8")))
                                    .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/friend-requests/decision")
                                    .build()
                            );
                });

                ImageButton rejectButton = new ImageButton(view.getContext());
                rejectButton.setBackground(getResources().getDrawable(R.drawable.ic_reject));
                LinearLayout.LayoutParams rejectLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rejectLp.setMargins(20, 20, 0, 0);
                rejectButton.setLayoutParams(rejectLp);
                friendLayout.addView(rejectButton);

                rejectButton.setOnClickListener(v -> {
                    @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask acceptRequestTask = new HttpRequestTask(response1 -> {
                        if (response1.isSuccessful()) {
                            showSnackBarSuccess("Friend request rejected");
                            getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                        }
                    });

                    acceptRequestTask
                            .execute(new Request
                                    .Builder()
                                    .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                    .post(RequestBody
                                            .create(new Gson()
                                                            .toJson(new FriendRequestDecision(
                                                                    requestResponse.getRequestId(),
                                                                    false)),
                                                    MediaType.parse("application/json; charset=utf-8")))
                                    .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/friend-requests/decision")
                                    .build()
                            );
                });
            }
        });

        requestTask
                .execute(new Request
                        .Builder()
                        .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                        .url(USER_PATH + "/" + userId + "/friend-requests")
                        .build()
                );
    }

    private void populateFriendsList() {
        listLayout.removeAllViews();

        @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(response -> {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<GetFriendResponse>>() {
            }.getType();
            List<GetFriendResponse> friends = gson.fromJson(response.getBody(), listType);

            for (GetFriendResponse friend : friends) {
                ImageView friendIcon = new ImageView(view.getContext());
                friendIcon.setBackgroundResource(R.drawable.ic_friend);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50, 20, 0, 0);
                lp.height = 100;
                lp.width = 100;
                friendIcon.setLayoutParams(lp);

                LinearLayout friendLayout = new LinearLayout(view.getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150);
                layoutParams.setMargins(50, 50, 50, 50);
                friendLayout.setLayoutParams(layoutParams);
                friendLayout.setOrientation(LinearLayout.HORIZONTAL);
                friendLayout.setBackground(getResources().getDrawable(R.drawable.selected_robot_state_background));
                friendLayout.addView(friendIcon);

                LinearLayout textLayout = new LinearLayout(view.getContext());
                LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(200, ViewGroup.LayoutParams.MATCH_PARENT);
                textLp.setMargins(50, 0, 50, 0);
                textLayout.setLayoutParams(textLp);
                textLayout.setOrientation(LinearLayout.VERTICAL);
                TextView botName = new TextView(view.getContext());
                botName.setText(friend.getLastName() + " " + friend.getFirstName() + " #" + friend.getId());
                botName.setTextSize(16);
                botName.setTypeface(null, Typeface.BOLD_ITALIC);
                textLayout.addView(botName);
                textLayout.setGravity(Gravity.CENTER);

                friendLayout.addView(textLayout);
                listLayout.addView(friendLayout);

                ImageButton messageButton = new ImageButton(view.getContext());
                messageButton.setBackground(getResources().getDrawable(R.drawable.ic_message));
                LinearLayout.LayoutParams messageLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                messageLp.setMargins(20, 30, 0, 0);
                messageButton.setLayoutParams(messageLp);
                friendLayout.addView(messageButton);

                ImageButton deleteButton = new ImageButton(view.getContext());
                deleteButton.setBackground(getResources().getDrawable(R.drawable.ic_delete_24));
                LinearLayout.LayoutParams deleteLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                deleteLp.setMargins(20, 20, 0, 0);
                deleteButton.setLayoutParams(deleteLp);

                messageButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    FrameLayout container = new FrameLayout(view.getContext());
                    EditText text = new EditText(view.getContext());
                    LinearLayout.LayoutParams sendMessageLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    sendMessageLp.setMargins(50, 20, 50, 20);
                    text.setLayoutParams(sendMessageLp);
                    container.addView(text);
                    text.setSingleLine(false);
                    text.setTextColor(Color.GRAY);
                    text.setHintTextColor(Color.GRAY);
                    text.setHint("Your message...");
                    text.setMaxLines(5);
                    text.getBackground()
                            .mutate()
                            .setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                    builder.setView(container);

                    builder.setPositiveButton("Send", ((dialog, which) -> {
                                int userId = sharedPreferences.getInt("id", 0);

                                if (userId == 0) {
                                    return;
                                }

                                if (text.getText().toString().isEmpty()) {
                                    showSnackBarError("Empty message");
                                } else {
                                    stompClient.send("/app/chat", new Gson()
                                            .toJson(
                                                    new Message(userId, friend.getId(), text.getText().toString(), Instant.now().toString()))).subscribe();

                                    getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                                }
                            }))
                            .setNegativeButton("Cancel", ((dialog, which) -> {
                                getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                            }));

                    builder.create().show();
                });

                deleteButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    builder.setMessage("Please confirm removing " + friend.getLastName() + " from friends")
                            .setPositiveButton("Confirm", (dialog, id) -> {

                                @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask removeFriendTask = new HttpRequestTask(response1 -> {
                                    if (response1.isSuccessful()) {
                                        showSnackBarSuccess("Friend removed");
                                        getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                                    } else {
                                        showSnackBarError("Friend deletion failed");
                                    }
                                });

                                removeFriendTask
                                        .execute(new Request
                                                .Builder()
                                                .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                                .delete()
                                                .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/friends/" + friend.getId())
                                                .build()
                                        );
                            })
                            .setNegativeButton("Cancel", (dialog, id) -> {
                                // User cancelled the dialog
                            });
                    builder.create().show();
                });

                friendLayout.addView(deleteButton);
            }

        });

        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", 0);

        requestTask
                .execute(new Request
                        .Builder()
                        .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                        .url(USER_PATH + "/" + userId + "/friends")
                        .build()
                );
    }

    private void listenerOnAddButton(ImageButton addButton) {
        addButton.setOnClickListener(v -> {
            EditText idText = new EditText(view.getContext());
            idText.setInputType(InputType.TYPE_CLASS_TEXT);
            idText.setHint("User id");
            idText.setHintTextColor(Color.GRAY);
            idText.setTextColor(Color.GRAY);
            idText.setGravity(Gravity.CENTER_HORIZONTAL);
            idText
                    .getBackground()
                    .mutate()
                    .setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder
                    .setMessage("Send friend request")
                    .setView(idText)
                    .setPositiveButton("Confirm", (dialog, id) -> {

                        @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask createBotTask = new HttpRequestTask(response1 -> {
                            if (response1.isSuccessful()) {
                                showSnackBarSuccess("Friend request sent");
                                getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                            } else {
                                ErrorResponse errorResponse = new Gson().fromJson(response1.getBody(), ErrorResponse.class);
                                if (errorResponse.getError().getType().equals("HttpMessageNotReadableException")) {
                                    showSnackBarError("Invalid input");
                                } else {
                                    showSnackBarError(errorResponse.getError().getMessage());
                                }
                                getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                            }
                        });

                        createBotTask
                                .execute(new Request
                                        .Builder()
                                        .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                                        .post(RequestBody
                                                .create(new Gson()
                                                                .toJson(new FriendRequest(
                                                                        idText.getText().toString())),
                                                        MediaType.parse("application/json; charset=utf-8")))
                                        .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/friends")
                                        .build()
                                );
                    })
                    .setNegativeButton("Cancel", ((dialog, which) -> {
                        getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).commit();
                    }));

            builder.create().show();
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        if (itemId == R.id.home_item) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), HomeFragment.class, null).addToBackStack("friends_to_home").setReorderingAllowed(true).commit();
        }

        if (itemId == R.id.chat) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), ChatFragment.class, null).addToBackStack("friends_to_chat").setReorderingAllowed(true).commit();
        }

        if (itemId == R.id.profile) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), ProfileFragment.class, null).addToBackStack("friends_to_profile").setReorderingAllowed(true).commit();
        }

        if (itemId == R.id.bots) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).addToBackStack("friends_to_bots").setReorderingAllowed(true).commit();
        }

        return true;
    };

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showSnackBarError(String msg) {
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
    private void showSnackBarSuccess(String msg) {
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
