package com.robo_world.fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.robo_world.utility.Constants.USER_PATH;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.kimkevin.cachepot.CachePot;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robo_world.R;
import com.robo_world.http.HttpRequestTask;
import com.robo_world.model.GetConversationsResponse;
import com.robo_world.model.Message;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.Request;

/**
 * Chat fragment
 *
 * @author Blajan George
 */
public class ChatFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private View view;

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.chat);
        navigationView.setOnNavigationItemSelectedListener(navListener);

        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);

        @SuppressLint("UseCompatLoadingForDrawables") HttpRequestTask requestTask = new HttpRequestTask(response -> {
            LinearLayout conversationLayout = view.findViewById(R.id.conversation_layout);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<GetConversationsResponse>>() {
            }.getType();
            List<GetConversationsResponse> conversations = gson.fromJson(response.getBody(), listType);

            for (GetConversationsResponse conversation : conversations) {
                LinearLayout convInfoLayout = new LinearLayout(view.getContext());
                LinearLayout.LayoutParams convInfoLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300);
                convInfoLayoutLp.setMargins(50, 60, 50, 50);
                convInfoLayout.setLayoutParams(convInfoLayoutLp);
                convInfoLayout.setOrientation(LinearLayout.VERTICAL);
                convInfoLayout.setBackground(getResources().getDrawable(R.drawable.selected_robot_state_background));

                conversationLayout.addView(convInfoLayout);

                TextView userView = new TextView(view.getContext());
                LinearLayout.LayoutParams userViewLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 75);
                userViewLp.setMargins(30, 30, 10, 20);
                userView.setMaxLines(1);
                userView.setEllipsize(TextUtils.TruncateAt.END);
                userView.setText(conversation.getUserResponse().getLastName() + " " + conversation.getUserResponse().getFirstName() + " #" + conversation.getUserResponse().getId());
                userView.setTextSize(16);
                userView.setLayoutParams(userViewLp);
                userView.setTypeface(null, Typeface.BOLD_ITALIC);

                convInfoLayout.addView(userView);

                TextView messageView = new TextView(view.getContext());
                LinearLayout.LayoutParams textViewLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 85);
                textViewLp.setMargins(50, 0, 50, 0);

                Message lastMessage = conversation.getMessages().stream().max(Comparator.comparing(Message::getTimestamp)).get();
                Date lastMessageDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(lastMessage.getTimestamp());

                messageView.setText(lastMessage.getContent());
                messageView.setTextSize(20);
                messageView.setLayoutParams(textViewLp);
                messageView.setMaxLines(1);
                messageView.setEllipsize(TextUtils.TruncateAt.END);
                messageView.setTypeface(null, Typeface.BOLD_ITALIC);

                convInfoLayout.addView(messageView);

                TextView dateView = new TextView(view.getContext());
                LinearLayout.LayoutParams dateViewLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
                if (System.currentTimeMillis() - lastMessageDate.getTime() > 86400000) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(lastMessageDate);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    String stringMonth;
                    String stringDay;
                    if (month < 10) {
                        stringMonth = "0" + month;
                    } else {
                        stringMonth = String.valueOf(month);
                    }
                    if (day < 10) {
                        stringDay = "0" + day;
                    } else {
                        stringDay = String.valueOf(day);
                    }
                    dateView.setText(stringDay + "/" + stringMonth + "/" + calendar.get(Calendar.YEAR));
                    dateViewLp.setMargins(480, 0, 10, 20);
                } else {
                    int hour = lastMessageDate.getHours();
                    int minutes = lastMessageDate.getMinutes();
                    String stringHour;
                    String stringMinutes;
                    if (hour < 10) {
                        stringHour = "0" + hour;
                    } else {
                        stringHour = String.valueOf(hour);
                    }
                    if (minutes < 10) {
                        stringMinutes = "0" + minutes;
                    } else {
                        stringMinutes = String.valueOf(minutes);
                    }
                    dateView.setText(stringHour + ":" + stringMinutes);
                    dateViewLp.setMargins(620, 0, 10, 20);
                }
                dateView.setTextSize(16);
                dateView.setLayoutParams(dateViewLp);
                dateView.setTypeface(null, Typeface.BOLD_ITALIC);

                convInfoLayout.addView(dateView);

                conversationLayout.setOnClickListener(v -> {
                    CachePot.getInstance().push(conversation);

                    getParentFragmentManager().beginTransaction().replace(this.getId(), new ConversationFragment(), null).setReorderingAllowed(true).addToBackStack("chat_to_conversation").commit();
                });
            }

        });

        requestTask
                .execute(new Request
                        .Builder()
                        .addHeader("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                        .url(USER_PATH + "/" + sharedPreferences.getInt("id", 0) + "/conversations")
                        .build()
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.chat);
    }


    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        if (itemId == R.id.home_item) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), HomeFragment.class, null).addToBackStack("chat_to_home").setReorderingAllowed(true).commit();
        }

        if (itemId == R.id.friends) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).addToBackStack("chat_to_friends").setReorderingAllowed(true).commit();
        }

        if (itemId == R.id.profile) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), ProfileFragment.class, null).addToBackStack("chat_to_profile").setReorderingAllowed(true).commit();
        }

        if (itemId == R.id.bots) {
            getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).addToBackStack("chat_to_bots").setReorderingAllowed(true).commit();
        }

        return true;
    };
}
