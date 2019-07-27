package com.example.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.databinding.DataBindingUtil;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    Vector<AccessPoint> accessPoints;
    LinearLayoutManager linearLayoutManager;
    AccessPointAdapter accessPointAdapter;
    WifiManager wifiManager;
    List<ScanResult> scanResult;
    ActivityMainBinding binding;

    private String res = "";

    public static final int MULTIPLE_PERMISSIONS = 10;
    public static final int LOAD_SUCCESS = 101;

    String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 23 && !checkPermissions()) finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.accessPointRecyclerView.setLayoutManager(linearLayoutManager);

        accessPoints = new Vector<>();
        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mWifiScanReceiver, filter);
            wifiManager.startScan();
        }

        Button getbtn = (Button)findViewById(R.id.getjsonbtn);
        getbtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                getJSON();
            }
        });

    }

    private Handler mHandler = new Handler(){
        @NonNull
        @Override
        public void handleMessage(Message msg) {
                if(msg.what == LOAD_SUCCESS)    res = (String)msg.obj;
            }
        };

    public void getJSON() {

        Thread thread = new Thread(new Runnable() {

            public void run() {
                String SEARCH_URL = "https://pos.api.here.com/positioning/v1/locate?";
                String APP_ID_TAG = "app_id=";
                String APP_ID = "g9cu0MS7SE5blxTfApTn";
                String APP_CODE_TAG = "&app_code=";
                String APP_CODE = "FUYirG2FANQ0q9h2b1vtwg";
                String REQUEST_URL = SEARCH_URL + APP_ID_TAG + APP_ID  + APP_CODE_TAG+ APP_CODE;

                String result;
                try {
                    URL url = new URL(REQUEST_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(6000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    String str = setHTTPBody();

                    OutputStream os = conn.getOutputStream();
                    os.write(str.getBytes("utf-8"));
                    os.flush();

                    int responseStatusCode = conn.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = conn.getInputStream();
                    } else {
                        inputStream = conn.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    conn.disconnect();

                    result = sb.toString().trim();

                } catch (Exception e) {
                    result = e.toString();
                }

                Message message = mHandler.obtainMessage(LOAD_SUCCESS,result);
                mHandler.sendMessage(message);
            }

        });



        while (true) {
            try{
                thread.start();
                thread.sleep(2000);
                sendJSON();
            } catch(Exception e){
                Log.d("error2",e.toString());
                break;
            }
        }

    }

    public void sendJSON(){

        Thread thread2 = new Thread(new Runnable() {

            public void run() {
                String REQUEST_URL = "http://54.167.123.220:4000";

                String result;
                try {
                    URL url = new URL(REQUEST_URL);
                    HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();

                    conn2.setConnectTimeout(2000);
                    conn2.setReadTimeout(2000);
                    conn2.setRequestMethod("POST");
                    conn2.setRequestProperty("Content-Type","application/json");
                    conn2.setRequestProperty("Accept", "text/html");
                    conn2.setDoOutput(true);
                    conn2.setDoInput(true);
                    conn2.connect();

                    OutputStream os = conn2.getOutputStream();
                    os.write(res.getBytes("utf-8"));
                    os.flush();

                    int responseStatusCode = conn2.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = conn2.getInputStream();
                    } else {
                        inputStream = conn2.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    conn2.disconnect();

                    result = sb.toString().trim();

                } catch (Exception e) {
                    result = e.toString();
                    Log.i("error",result);
                }
            }

        });
        thread2.start();
    }

    private String setHTTPBody(){

        JSONArray arr = new JSONArray();

        for (int i =0; i < accessPoints.size(); i++){
            try {
                JSONObject obj = new JSONObject();
                obj.put("mac", accessPoints.get(i).getBssid());
                obj.put("powrx", Integer.valueOf(accessPoints.get(i).getRssi()));
                arr.put(obj);
            }
            catch(Exception e){
                Log.i("error",e.toString());
            }
        }
        JSONObject jsonobject = new JSONObject();
        try {
            jsonobject.put("wlan", arr);
        }
        catch(Exception e){
            Log.i("error",e.toString());
        }
        return jsonobject.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiScanReceiver);
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(MainActivity.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    requestCode = MULTIPLE_PERMISSIONS;
                //Log.d("permission", "granted");
            }
        }
    }

    private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    getWIFIScanResult();
                    wifiManager.startScan();
                }
                else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    context.sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
                }
            }
        }
    };

    public void getWIFIScanResult() {
        scanResult = wifiManager.getScanResults();

        if (accessPoints.size() != 0){
            accessPoints.clear();
        }

        for (int i = 0; i < scanResult.size(); i++) {
            ScanResult result = scanResult.get(i);
            if (result.frequency < 3000)
                accessPoints.add(new AccessPoint(result.SSID, result.BSSID, String.valueOf(result.level)));
        }
        accessPointAdapter = new AccessPointAdapter(accessPoints, MainActivity.this);
        binding.accessPointRecyclerView.setAdapter(accessPointAdapter);
        accessPointAdapter.notifyDataSetChanged();
    }
}
