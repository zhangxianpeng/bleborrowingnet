/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.inmo.bleborrowingnet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class BluetoothPanUtils {

    private static BluetoothPanUtils instance = new BluetoothPanUtils();

    public static BluetoothPanUtils getInstance() {
        return instance;
    }

    private BluetoothDevice curDevice;

    public static boolean isBluetoothConnected() {
        int connectState = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(1);
        if(connectState == 2) {
            return true;
        }
        return false;
    }

    //判断蓝牙网络是否连接
    public static boolean isPanConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo btInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
        if(btInfo!=null) {
            return btInfo.isConnected();
        }
        return false;
    }

    public void getPanProfile() {
        boolean isGet = BluetoothAdapter.getDefaultAdapter().getProfileProxy(MyApplication.instance, mProfileServiceListener, 5);
        Log.i("BluetoothPanUtils", "isGet: " + isGet);
    }


    public void getConnectedDeviceAndConnectNetwork() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);
            Log.i("BluetoothPanUtils", "state: " + state);
            if(state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i("BluetoothPanUtils","BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i("BluetoothPanUtils","devices:"+devices.size());

                for(BluetoothDevice device : devices){
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if(isConnected){
                        Log.i("BluetoothPanUtils","connected:"+device.getName());
                        curDevice = device;
                        getPanProfile();
                        break;
//                        deviceList.add(device);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private AtomicReference<Object> mBluetoothPan = new AtomicReference<>();
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.i("BluetoothPanUtils", "mProfileServiceListener onServiceConnected");
            mBluetoothPan.set(proxy);
            connectInternet(curDevice);
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i("BluetoothPanUtils", "mProfileServiceListener onServiceDisconnected");
            mBluetoothPan.set(null);
        }
    };



    private BluetoothProfile.ServiceListener mDeviceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.i("BluetoothPanUtils", "mDeviceListener onServiceConnected");
            mBluetoothPan.set(proxy);
            List<BluetoothDevice> devices = proxy.getConnectedDevices();
            Log.i("BluetoothPanUtils", "connectdevices: " + devices);
            if(devices != null && devices.size() > 0) {
                for(BluetoothDevice device : devices) {
                    Log.i("BluetoothPanUtils", device.getName());
                }
                curDevice = devices.get(0);
                getPanProfile();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i("BluetoothPanUtils", "mDeviceListener onServiceDisconnected");
            mBluetoothPan.set(null);
        }
    };



    private void connectInternet(BluetoothDevice bluetoothDevice) {
        try{
            Object bluetoothPan = mBluetoothPan.get();
            Class bluetoothPanClass = Class.forName("android.bluetooth.BluetoothPan");
            if(bluetoothPan != null) {
                Method methodGetConnectedDevices = bluetoothPanClass.getMethod("getConnectedDevices");
                Method methodDisconnect = bluetoothPanClass.getMethod("disconnect", BluetoothDevice.class);
                List<BluetoothDevice> sinks = (List<BluetoothDevice>)methodGetConnectedDevices.invoke(bluetoothPan);
                if(sinks != null) {
                    for(BluetoothDevice sink : sinks) {
                        methodDisconnect.invoke(bluetoothPan, sink);
                    }
                }
                //连接需要的设备
                Method methodConnect = bluetoothPanClass.getMethod("connect", BluetoothDevice.class);
                methodConnect.invoke(bluetoothPan, bluetoothDevice);
            }
        }catch (Exception e) {

        }
    }

    public void disConnectInternet() {
        try{
            Object bluetoothPan = mBluetoothPan.get();
            Class bluetoothPanClass = Class.forName("android.bluetooth.BluetoothPan");
            if(bluetoothPan != null) {
                Method methodGetConnectedDevices = bluetoothPanClass.getMethod("getConnectedDevices");
                Method methodDisconnect = bluetoothPanClass.getMethod("disconnect", BluetoothDevice.class);
                List<BluetoothDevice> sinks = (List<BluetoothDevice>)methodGetConnectedDevices.invoke(bluetoothPan);
                if(sinks != null) {
                    for(BluetoothDevice sink : sinks) {
                        methodDisconnect.invoke(bluetoothPan, sink);
                    }
                }
//                //连接需要的设备
//                Method methodConnect = bluetoothPanClass.getMethod("connect", BluetoothDevice.class);
//                methodConnect.invoke(bluetoothPan, bluetoothDevice);
            }
        }catch (Exception e) {

        }
    }
}
