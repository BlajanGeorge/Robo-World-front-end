package com.robo_world.fragment;

import static android.content.Context.MODE_PRIVATE;

import static com.robo_world.utility.Constants.SERVER_ADDRESS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.github.kimkevin.cachepot.CachePot;
import com.google.gson.Gson;
import com.robo_world.R;
import com.robo_world.model.GetConversationsResponse;
import com.robo_world.model.Message;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class ConversationFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private Unregistrar unregistrar;
    private LinearLayout messages;
    private View view;
    private StompClient mStompClient;
    private ScrollView scrollView;
    private int initialChildCount = 0;
    private Activity myActivity;

    public ConversationFragment() {
        super(R.layout.fragment_conversation);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        ConstraintLayout constraintLayout = view.findViewById(R.id.messagesDisplay);
        TextView usernameView = view.findViewById(R.id.userNameView);
        scrollView = view.findViewById(R.id.messagesScroll);
        scrollView.fullScroll(View.FOCUS_DOWN);
        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);

        GetConversationsResponse conversation = CachePot.getInstance().pop(GetConversationsResponse.class);
        int userId = sharedPreferences.getInt("id", 0);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertDpToPixels(550));
        lp.leftToLeft = this.getId();
        lp.rightToRight = this.getId();
        lp.topToBottom = usernameView.getId();

        constraintLayout.setLayoutParams(lp);

        unregistrar = KeyboardVisibilityEvent.registerEventListener(
                getActivity(),
                isOpen -> {
                    if (isOpen) {
                        lp.height = convertDpToPixels(300);
                        constraintLayout.setLayoutParams(lp);
                    } else {
                        lp.height = convertDpToPixels(550);
                        constraintLayout.setLayoutParams(lp);
                    }
                });

        messages = view.findViewById(R.id.messages);

        messages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (((ViewGroup) v).getChildCount() != initialChildCount) {
                initialChildCount = ((ViewGroup) v).getChildCount();
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        if (conversation != null) {
            usernameView.setText(conversation.getUserResponse().getFirstName() + " " + conversation.getUserResponse().getLastName() + " #" + conversation.getUserResponse().getId());
            for (Message message : conversation.getMessages()) {
                try {
                    messages.addView(createMessage(userId, message));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        scrollView.scrollTo(0, scrollView.getBottom());

        ImageButton sendButton = view.findViewById(R.id.sendMessageButton);
        EditText messageView = view.findViewById(R.id.sendMessageText);

        sendButton.setOnClickListener(v -> {
            String messageText = messageView.getText().toString();

            if (!messageText.isEmpty()) {
                Message message = new Message(userId, conversation.getUserResponse().getId(), messageText, Instant.now().toString());
                mStompClient.send("/app/chat", new Gson().toJson(message)).subscribe();
                messageView.setText("");
                try {
                    messages.addView(createMessage(userId, message));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                messages.invalidate();
            }

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregistrar.unregister();
    }

    private int convertDpToPixels(float dp) {
        return Math.round(dp * ((float) getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @SuppressLint("CheckResult")
    private void createWebSocketClient() {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + SERVER_ADDRESS + "/chat");
        int userId = sharedPreferences.getInt("id", 0);

        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {

                case OPENED:
                    if (userId != 0) {
                        mStompClient.topic("/topic/user/" + userId).subscribe(msg -> {
                            myActivity.runOnUiThread(() -> {
                                try {
                                    messages.addView(createMessage(userId, new Gson().fromJson(msg.getPayload(), Message.class)));
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                messages.invalidate();
                            });
                        });
                    }
                    break;
                case ERROR:
                case CLOSED:
                    break;
            }
        });

        mStompClient.connect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mStompClient != null && mStompClient.isConnected()) {
            mStompClient.disconnect();
        }
        mStompClient = null;
        unregistrar.unregister();
    }

    @Override
    public void onResume() {
        super.onResume();
        myActivity = getActivity();
        createWebSocketClient();
    }

    private LinearLayout createMessage(int userId, Message message) throws ParseException {
        LinearLayout msgLayout = new LinearLayout(view.getContext());
        LinearLayout.LayoutParams msgLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView msgView = new TextView(view.getContext());
        LinearLayout.LayoutParams msgViewLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgView.setLayoutParams(msgViewLp);
        msgView.setText(message.getContent());
        msgView.setPadding(30, 15, 15, 20);
        msgView.setMaxWidth(convertDpToPixels(285));

        if (userId == message.getSubject()) {
            msgLayoutLp.setMargins(100, 20, 50, 15);
            msgLayoutLp.gravity = Gravity.END;
        } else {
            if (userId != 0) {
                msgLayoutLp.setMargins(50, 20, 100, 15);
            }
        }

        msgLayout.setLayoutParams(msgLayoutLp);
        msgLayout.setBackground(getResources().getDrawable(R.drawable.bots_background));
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);

        msgLayout.addView(msgView);

        @SuppressLint("SimpleDateFormat") Date messageDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(message.getTimestamp());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(messageDate);

        int hour = messageDate.getHours();
        int minutes = messageDate.getMinutes();
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

        TextView hourText = new TextView(view.getContext());
        LinearLayout.LayoutParams hourTextLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        hourTextLp.setMargins(0, 0, 15, 10);
        hourText.setLayoutParams(hourTextLp);
        hourText.setText(stringHour + ":" + stringMinutes);
        hourText.setTextSize(8);
        hourText.setGravity(Gravity.BOTTOM);
        msgLayout.addView(hourText);

        return msgLayout;
    }
}
