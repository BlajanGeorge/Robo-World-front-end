package com.robo_world.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.robo_world.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Home fragment
 *
 * @author Blajan George
 */
public class HomeFragment extends Fragment {
    private View view;
    private boolean connected = false;
    private OutputStream bluetoothOutputStream;
    private ImageView botConnectionStatusImage;
    private Button connectButton;
    private BluetoothSocket bluetoothSocket;
    private TextView selectedBotName;
    private SharedPreferences sharedPreferences;
    private static final String DISCONNECTED_MESSAGE = "Connection to bot was interrupted";
    private boolean commandIssued = false;
    private boolean lightsOn = false;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @SuppressLint({"DiscouragedPrivateApi", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.home_item);
        navigationView.setOnNavigationItemSelectedListener(navListener);

        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);

        String botName = sharedPreferences.getString("selected_bot_name", null);

        connectButton = view.findViewById(R.id.connectButton);
        ImageButton parkingButton = view.findViewById(R.id.parkButton);
        ImageButton lightButton = view.findViewById(R.id.lightButton);
        ImageButton soundButton = view.findViewById(R.id.soundButton);
        ImageButton moveRightButton = view.findViewById(R.id.moveRightButton);
        ImageButton moveLeftButton = view.findViewById(R.id.moveLeftButton);
        ImageButton moveUpButton = view.findViewById(R.id.moveUpButton);
        ImageButton moveDownButton = view.findViewById(R.id.moveDownButton);

        botConnectionStatusImage = view.findViewById(R.id.botConnectionStatusImage);
        selectedBotName = view.findViewById(R.id.selectedBotName);

        botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);

        if (botName != null) {
            selectedBotName.setText(botName);
        }

        connectButton.setOnClickListener(v -> {
            if (connected) {
                if (!commandIssued) {
                    disconnect(bluetoothSocket);
                } else {
                    showSelectBotSnackBarError("Please finish your button actions before disconnecting");
                }
            } else {
                connect(botName);
            }
        });

        parkingButton.setOnClickListener(v ->
        {
            if (!connected) {
                showSelectBotSnackBarError("Please connect to a bot first");
            } else {
                try {
                    bluetoothOutputStream.write('X');
                } catch (IOException e) {
                    Log.e(this.toString(), DISCONNECTED_MESSAGE);
                    showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                    connected = false;
                    lightsOn = false;
                    botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                    connectButton.setText("Connect");
                }
            }
        });

        lightButton.setOnClickListener(v ->
        {
            if (!connected) {
                showSelectBotSnackBarError("Please connect to a bot first");
            } else {
                try {
                    if (lightsOn) {
                        lightsOn = false;
                        bluetoothOutputStream.write('w');
                    } else {
                        lightsOn = true;
                        bluetoothOutputStream.write('W');
                    }
                } catch (IOException e) {
                    Log.e(this.toString(), DISCONNECTED_MESSAGE);
                    showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                    connected = false;
                    lightsOn = false;
                    botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                    connectButton.setText("Connect");
                }
            }
        });

        soundButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (!connected) {
                        showSelectBotSnackBarError("Please connect to a bot first");
                    } else {
                        try {
                            bluetoothOutputStream.write('V');
                            commandIssued = true;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    if (connected) {
                        try {
                            bluetoothOutputStream.write('v');
                            commandIssued = false;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
            }
            return true;
        });

        moveDownButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (!connected) {
                        showSelectBotSnackBarError("Please connect to a bot first");
                    } else {
                        try {
                            bluetoothOutputStream.write('B');
                            commandIssued = true;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    if (connected) {
                        try {
                            bluetoothOutputStream.write('S');
                            commandIssued = false;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
            }
            return true;
        });

        moveUpButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (!connected) {
                        showSelectBotSnackBarError("Please connect to a bot first");
                    } else {
                        try {
                            bluetoothOutputStream.write('F');
                            commandIssued = true;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    if (connected) {
                        try {
                            bluetoothOutputStream.write('S');
                            commandIssued = false;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
            }
            return true;
        });

        moveLeftButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (!connected) {
                        showSelectBotSnackBarError("Please connect to a bot first");
                    } else {
                        try {
                            bluetoothOutputStream.write('L');
                            commandIssued = true;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    if (connected) {
                        try {
                            bluetoothOutputStream.write('S');
                            commandIssued = false;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
            }
            return true;
        });

        moveRightButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (!connected) {
                        showSelectBotSnackBarError("Please connect to a bot first");
                    } else {
                        try {
                            bluetoothOutputStream.write('R');
                            commandIssued = true;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    if (connected) {
                        try {
                            bluetoothOutputStream.write('S');
                            commandIssued = false;
                        } catch (IOException e) {
                            Log.e(this.toString(), DISCONNECTED_MESSAGE);
                            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
                            connected = false;
                            lightsOn = false;
                            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                            connectButton.setText("Connect");
                        }
                    }
                }
                break;
            }
            return true;
        });
    }

    private void disconnect(BluetoothSocket socket) {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                bluetoothOutputStream.write('w');
                socket.close();
                botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
                connectButton.setText("Connect");
                showSuccessConnectionSnackBar("Connection terminated");
                connected = false;
                lightsOn = false;
            }
        } catch (IOException e) {
            showSelectBotSnackBarError(DISCONNECTED_MESSAGE);
            connected = false;
            lightsOn = false;
            botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_red);
            connectButton.setText("Connect");
        }
    }

    private void connect(String botName) {
        if (botName == null) {
            showSelectBotSnackBarError("Please select a bot first");
        } else {
            Optional<BluetoothSocket> maybeBluetoothSocket = connectToBot(botName);
            if (maybeBluetoothSocket.isEmpty()) {
                showSelectBotSnackBarError("Failed to obtain bluetooth connection");
            } else {
                BluetoothSocket bluetoothSocket = maybeBluetoothSocket.get();
                try {
                    bluetoothOutputStream = bluetoothSocket.getOutputStream();
                    connected = true;
                    botConnectionStatusImage.setBackgroundResource(R.drawable.ic_bot_24_green);
                    showSuccessConnectionSnackBar("Connection established");
                    connectButton.setText("Disconnect");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private Optional<BluetoothSocket> connectToBot(String botName) {
        BluetoothManager bluetoothManager = view.getContext().getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            Optional<BluetoothDevice> maybeBotDevice = pairedDevices.stream().filter(device -> device.getName().equals(botName)).findAny();
            if (maybeBotDevice.isEmpty()) {
                showSelectBotSnackBarError("Bot not found through paired devices, please try to pair your bot");
                return Optional.empty();
            } else {
                BluetoothDevice botDevice = maybeBotDevice.get();
                try {
                    bluetoothSocket = botDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (IOException e) {
                    Log.e(this.toString(), "Socket's create() method failed", e);
                    showSelectBotSnackBarError("Socket creation failed");
                    return Optional.empty();
                }
                try {
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();
                    return Optional.of(bluetoothSocket);
                } catch (IOException connectException) {
                    try {
                        bluetoothSocket.close();
                        Log.e(this.toString(), "Socket's connect() method failed", connectException);
                        showSelectBotSnackBarError("Socket connection failed");
                        return Optional.empty();
                    } catch (IOException closeException) {
                        Log.e(this.toString(), "Could not close the client socket", closeException);
                        return Optional.empty();
                    }
                }
            }
        } else {
            showSelectBotSnackBarError("No paired devices found, please try to pair your bot");
            return Optional.empty();
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

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        if (itemId == R.id.bots) {
            disconnect(bluetoothSocket);
            getParentFragmentManager().beginTransaction().replace(this.getId(), BotsFragment.class, null).setReorderingAllowed(true).addToBackStack("home_to_bots").commit();
        }

        if (itemId == R.id.friends) {
            disconnect(bluetoothSocket);
            getParentFragmentManager().beginTransaction().replace(this.getId(), FriendsFragment.class, null).setReorderingAllowed(true).addToBackStack("home_to_friends").commit();
        }

        if (itemId == R.id.profile) {
            disconnect(bluetoothSocket);
            getParentFragmentManager().beginTransaction().replace(this.getId(), ProfileFragment.class, null).setReorderingAllowed(true).addToBackStack("home_to_profile").commit();
        }

        if (itemId == R.id.chat) {
            disconnect(bluetoothSocket);
            getParentFragmentManager().beginTransaction().replace(this.getId(), ChatFragment.class, null).setReorderingAllowed(true).addToBackStack("home_to_chat").commit();
        }

        return true;
    };

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setSelectedItemId(R.id.home_item);
        sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
        String botName = sharedPreferences.getString("selected_bot_name", null);
        if (!selectedBotName.getText().equals(botName)) {
            selectedBotName.setText(botName);
            LinearLayout botStateLayout = view.findViewById(R.id.bot_status_layout);
            botStateLayout.invalidate();
        }
    }
}
