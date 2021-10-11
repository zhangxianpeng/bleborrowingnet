package com.inmoglass.pantest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "pantest";
    Button connect, disConnect;
    TextView state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        state = findViewById(R.id.state);
        connect = findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPan();
            }
        });
        disConnect = findViewById(R.id.disconnect);
        disConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothPanUtils.getInstance().disConnectInternet();
            }
        });
        getPanState();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    private void checkPan() {
        boolean isConnected = getPanState();
        Log.i(TAG, "PAN isConnected: " + isConnected);
        if(!isConnected) {
            BluetoothPanUtils.getInstance().getConnectedDeviceAndConnectNetwork();
//            handler.sendEmptyMessageDelayed(1, 1000);
        }
    }

    private boolean getPanState() {
        boolean isConnected = BluetoothPanUtils.isPanConnected(this);
        updateState(isConnected);
        return isConnected;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            getPanState();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void updateState(boolean isConnected) {
        state.setText(isConnected ? "随身网络已连接" : "随身网络未连接");
    }

    private ConnectivityManager connectivityManager;
    private NetworkInfo info;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.i(TAG, "ConnectivityManager.CONNECTIVITY_ACTION");
                info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
                if(info!=null) {
                    Log.i(TAG, "TYPE_BLUETOOTH: " + info.isConnected());
                    updateState(info.isConnected());
                }
            }
        }
    };
}