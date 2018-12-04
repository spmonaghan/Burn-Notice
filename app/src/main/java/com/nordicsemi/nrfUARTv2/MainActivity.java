
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.nordicsemi.nrfUARTv2;

import java.text.DateFormat;
import java.util.Date;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;


    // AG: Initializing variables
    private int num_notifs = 1;
    private int instant_irradiance;
    private int cumul_irradiance;
    private int user_MED = 0;
    private int exposure_percentage;
    private int last_exposure = 0;
    private int threshold = 80;
    private String str_user_MED;
    private boolean demo_mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.action_bluetooth);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();

        // AG: Create Notification Channel for Android 8+
        Notifications.createNotificationChannel(getApplicationContext());

        // AG: Recalls exposure
        exposure_percentage = BurnNoticeSharedPrefs.getExposure(this);
        updateProgress();

//        checkIfDemo();

        // AG: Removed Send function
        // Handle Send button
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            	EditText editText = (EditText) findViewById(R.id.sendText);
//            	String message = editText.getText().toString();
//            	byte[] value;
//				try {
//					//send data to service
//					value = message.getBytes("UTF-8");
//					mService.writeRXCharacteristic(value);
//					//Update the log with time stamp
//					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
//               	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//               	 	edtMessage.setText("");
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//            }
//        });
     
        // Set initial UI state

        // AG: Change color scheme
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#81D4FA")));
        getActionBar().setIcon(R.drawable.ic_ffef00_sunshine);
    }

//    private void checkIfDemo() {
//
//        Log.d(TAG, "Demo mode value: " + demo_mode);
//
//        final Button test_notif_button = (Button) findViewById(R.id.test_notif_button);
//        final Button test_increment_button = (Button) findViewById(R.id.test_increment_button);
//        final Button reset_button = (Button) findViewById(R.id.reset_button);
//        TextView deviceLabel = (TextView)findViewById(R.id.deviceLabel);
//        TextView deviceName = (TextView)findViewById(R.id.deviceName);
//        TextView current_exposure = (TextView)findViewById(R.id.current_exposure);
//
//        if (!demo_mode) {   // Set up dev options
//
//            // AG: Created Notification button to test functionality
//            test_notif_button.setVisibility(View.VISIBLE);
//            test_notif_button.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Notifications.notifyExposure(getApplicationContext(), "", num_notifs);
//                    num_notifs++;
//                }
//            });
//
//            // AG: Created Increment button to artificially increment
//            test_increment_button.setVisibility(View.VISIBLE);
//            test_increment_button.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    last_exposure = exposure_percentage;
//                    exposure_percentage = exposure_percentage + 10;
//                    updateProgress();
//                    checkThreshold();
//                }
//            });
//
//            // AG: Created Reset button to artificially increment
//            reset_button.setVisibility(View.VISIBLE);
//            reset_button.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    exposure_percentage = 0;
//                    updateProgress();
//                }
//            });
//
//            // AG: Created text to show developer status
//
//            deviceLabel.setVisibility(View.VISIBLE);
//            deviceName.setVisibility(View.VISIBLE);
//            current_exposure.setVisibility(View.VISIBLE);
//            current_exposure.setText("Current Exposure: " + instant_irradiance);
//        }
//        else if (demo_mode)
//        {
//            test_notif_button.setVisibility(View.INVISIBLE);
//            test_increment_button.setVisibility(View.INVISIBLE);
//            reset_button.setVisibility(View.INVISIBLE);
//            deviceLabel.setVisibility(View.INVISIBLE);
//            deviceName.setVisibility(View.INVISIBLE);
//            current_exposure.setVisibility(View.INVISIBLE);
//        }
//
//    }

    // AG: Added menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // AG: onClick, navigates from main page to settings page
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_bluetooth) {
            // Handle Disconnect & Connect button
//            btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (!mBtAdapter.isEnabled()) {
//                        Log.i(TAG, "Bluetooth not enabled yet");
//                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//                    }
//                    else {
//                        if (btnConnectDisconnect.getText().equals(R.string.action_bluetooth)){

                            //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

            Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
//                        } else {
//                            //Disconnect button pressed
//                            if (mDevice!=null)
//                            {
//                                mService.disconnect();
//
//                            }
//                        }
//                    }
//                }
//            });
        }
        else if (id == R.id.action_demo) {
            demo_mode = !demo_mode;
            onResume();
        }

        return super.onOptionsItemSelected(item);
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_CONNECT_MSG");
//                             btnConnectDisconnect.setText("Disconnect");
                             edtMessage.setEnabled(true);
                             btnSend.setEnabled(true);
                             // AG: Replaced mDevice.getName()
                             ((TextView) findViewById(R.id.deviceName)).setText("Connected");
//                             listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
//                        	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
//                             btnConnectDisconnect.setText("Connect");
                             edtMessage.setEnabled(false);
                             btnSend.setEnabled(false);
                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
//                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
              
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                         	String text = new String(txValue, "UTF-8");
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

                                // AG: if before value < thresh; new value > thresh, then notify
                                last_exposure = exposure_percentage;

                         	    exposure_percentage = calculateExposure(text);

                         	    // AG: Stores exposure for next time app is opened
                         	    BurnNoticeSharedPrefs.setExposure(getApplicationContext(), exposure_percentage);

                         	    updateProgress();

                                checkThreshold();

                         	    // AG: Removing continuous printing to main screen
//                        	 	listAdapter.add("["+currentDateTimeString+"] RX: "+text);
//                        	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                             e.printStackTrace();
                         }
                     }
                 });
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
            
            
        }
    };

    private void checkThreshold() {
        // AG: If beyond threshold exposure, send notification
        if (last_exposure < threshold && exposure_percentage >= threshold) {
            Notifications.notifyExposure(getApplicationContext(), "", 0);
        }
    }

    int calculateExposure(String text) {

        int[] mask = new int[10];

        for (int i = 0; i < text.length(); i++)
        {
            int ASCII = Integer.valueOf(text.charAt(i));

            if (ASCII == 45 || (ASCII >= 48 && ASCII <= 57)) {
                mask[i] = 1;
            }
            else {
                mask[i] = 0;
            }
        }

        StringBuilder modified_text = new StringBuilder();

        for (int j = 0; j <= text.length(); j++)
        {
            if (mask[j] == 1)
            {
                modified_text.append(text.charAt(j));
            }
        }

        text = modified_text.toString();

        int incoming_data = Integer.parseInt(text); // AG: convert string to double
        if (incoming_data < 0) { // AG: if negative value, assume negligible and approximately 0
            incoming_data = 0;
        }
        else {
            incoming_data = incoming_data + 13;
        }

        instant_irradiance = incoming_data / 195; // AG: Experimentally derived scalar
                cumul_irradiance = cumul_irradiance + instant_irradiance;
        Log.d(TAG, "Cumulative Exposure: " + String.valueOf(cumul_irradiance));
        user_MED = getMED();

        if (exposure_percentage >=100)
        {
            return  exposure_percentage = 100;
        }
        else{
            exposure_percentage = (int) ((double) cumul_irradiance / (double) user_MED * 100);
            return exposure_percentage;
        }

    }

    private int getMED() {
        str_user_MED = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.skin_type_key), "15");
        user_MED = Integer.valueOf(str_user_MED);

//        switch (pref_string){
//            case "Type I: Always burns, doesn\'t tan":
//                user_MED = 15;
//                break;
//            case "Type II: Burns easily":
//                user_MED = 25;
//                break;
//            case "Type III: Tans after initial burn":
//                user_MED = 30;
//                break;
//            case "Type IV: Burns minimally, tans easily":
//                user_MED = 40;
//                break;
//            case "Type V: Rarely burns, tans well":
//                user_MED = 60;
//                break;
//            case "Type VI: Never burns, always tan":
//                user_MED = 90;
//                break;
//        }

        Log.d(TAG, "User MED is now: " + user_MED);
        return user_MED;
    }

    // AG: Creating method to continuously update progress bar

    private void updateProgress() {
//
////        Thread progressThread = new Thread(new Runnable() {
//
////            public void run() {
////                while (true) {
//            // AG: Update progress bar
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView progressPercent = findViewById(R.id.progressPercent);

        // AG: Change % value of progress TextView
        progressBar.setProgress(exposure_percentage);
        Log.d(TAG, "Exposure Percentage: " + String.valueOf(exposure_percentage));
        progressPercent.setText(String.valueOf(progressBar.getProgress()) + " %");
//                }
//            });
//                    try {
//                        Thread.sleep(1000); // AG: Time between updates (1 second)
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

//        progressThread.start();
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

//        checkIfDemo();
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);
                            

            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }
}
