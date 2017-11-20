package com.example.orangeparadise.clientorderdemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.orangeparadise.clientException.NoSocketAvailableException;
import com.example.orangeparadise.clientException.OtherClientException;
import com.example.orangeparadise.clientorderannotates.ClientAnnotation;


@ClientAnnotation(id = 1, use = ClientAnnotation.usage.TEST)
public class MainActivity extends AppCompatActivity implements ServiceConnection{

    private static final String TAG = "MainACtivity";
    private ClientOrderService clientOrderService;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String type = bundle.getString(ClientOrderService.RESULT_TYPE);
            switch (type) {
                case ClientOrderService.AVAILABLE:
                    Toast.makeText(MainActivity.this,
                            bundle.getString(ClientOrderService.RESULT), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, ClientOrderService.class);
        intent.putExtra(ClientOrderService.OPERATION, ClientOrderService.CONNECT);
        //startService(intent);

        //Log.i(TAG, clientOrderService.toString());

        Button buttonSwitch = findViewById(R.id.buttonVP);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(getBaseContext(), MainActivityVertical.class);
                MainActivity.this.startActivity(intent);*/
                Log.i(TAG, "OnClick");
                Log.i(TAG, String.valueOf(clientOrderService.getItems().size()));
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                MainActivity.this.startActivity(intent);
                //Toast.makeText(MainActivity.this, String.valueOf(clientOrderService.getItems().size()), Toast.LENGTH_SHORT).show();
            }
        });

        Log.i(TAG, "ONCREATE DONE");

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);
                    clientOrderService.initConnection();
                } catch (NoSocketAvailableException e) {
                    e.printStackTrace();
                } catch (OtherClientException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    @Override
    protected void onResume() {
        registerReceiver(receiver, new IntentFilter(ClientOrderService.NOTIFICATION));
        Intent intent = new Intent(this, ClientOrderService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        //startService(intent);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        unbindService(this);
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        //Toast.makeText(this, "Service Connected", Toast.LENGTH_SHORT).show();
        //Log.i(TAG, "Service connected1");
        ClientOrderService.OrderBinder binder = (ClientOrderService.OrderBinder) iBinder;
        Log.i(TAG, "Service connected");
        clientOrderService = binder.getService();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientOrderService.initConnection();
                } catch (NoSocketAvailableException e) {
                    e.printStackTrace();
                } catch (OtherClientException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        clientOrderService = null;
    }


}
