package com.example.orangeparadise.clientorderdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orangeparadise.clientException.NoSocketAvailableException;
import com.example.orangeparadise.clientException.OtherClientException;
import com.example.orangeparadise.clientUtility.OrderItem;
import com.gigamole.navigationtabbar.ntb.NavigationTabBar;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivityVertical extends Activity implements ServiceConnection{

    private static final String TAG = "MainActivity";
    private ClientOrderService clientOrderService;

    private FloatingActionButton FABOrder;
    private FloatingActionButton FABRefresh;

    private TextView txtTotal;

    private URLLock urlLock;


    List<OrderItem> items = new ArrayList<>();
    List<Integer> orderNumbers = new ArrayList<>();

    private int getTotalOrderNum(){
        int sum = 0;
        for (int num: orderNumbers){
            sum += num;
        }
        return sum;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String type = bundle.getString(ClientOrderService.RESULT_TYPE);
            switch (type) {
                case ClientOrderService.AVAILABLE:
                    Log.i(TAG, bundle.getString(ClientOrderService.RESULT) + " items fetched");
                    items = clientOrderService.getItems();
                    orderNumbers = clientOrderService.orderNums;
                    initUI();
                    txtTotal.setVisibility(View.GONE);
                    if (getTotalOrderNum() > 0){
                        txtTotal.setText(getTotalOrderNum()>10? "10+":String.valueOf(getTotalOrderNum()));
                        Log.i(TAG, "visible");
                        txtTotal.setVisibility(View.VISIBLE);
                    }
                    break;
                case ClientOrderService.FAILED:
                    Toast.makeText(MainActivityVertical.this, "Failed to load data, please" +
                            " refresh pages", Toast.LENGTH_LONG).show();
                    break;
                case ClientOrderService.QUERY:
                    Log.i(TAG, "query done");
                    String result = bundle.getString(ClientOrderService.RESULT);
                    if (result == ClientOrderService.NOTAVA){
                        Toast.makeText(MainActivityVertical.this, "Sorry, currently we can not get any of your order item prepared", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivityVertical.this, "Order checked, redirect to confirmation", Toast.LENGTH_LONG).show();
                        MainActivityVertical.this.startActivity(new Intent(MainActivityVertical.this, ConfirmActivity.class));
                    }
                    break;
                case ClientOrderService.TRACK:
                    Log.i(TAG, "track done");
                    Toast.makeText(MainActivityVertical.this, "Your order is being packed.", Toast.LENGTH_LONG).show();
                    //clientOrderService.currentState = ClientOrderService.OrderState.Watching;
                    clientOrderService.beginNewOrder();
                    txtTotal.setVisibility(View.GONE);
                    //MainActivityVertical.this.startActivity(new Intent(MainActivityVertical.this, MainActivityVertical.class));
                    MainActivityVertical.this.startActivity(new Intent(MainActivityVertical.this, FinalResult.class));

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        items.add(new OrderItem("Default Item", 0.0, "default Url",
                "default description"));
        orderNumbers.add(0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_ntb);
        initUI();
        //imageDownloadTask.execute();
        urlLock = new URLLock();
        urlLock.locked = false;
        FABOrder = (FloatingActionButton) findViewById(R.id.FABOrder);
        FABRefresh = (FloatingActionButton) findViewById(R.id.FABRefresh);

        txtTotal = (TextView) findViewById(R.id.txt_vp_item_total);
        txtTotal.setVisibility(View.GONE);

        FABOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Order");
                //clientOrderService.orderItemRequest();
                if (clientOrderService.orderEmpty() && clientOrderService.currentState == ClientOrderService.OrderState.Watching){
                    Toast.makeText(MainActivityVertical.this,
                            "Your bag is empty", Toast.LENGTH_LONG).show();
                } else {
                    if (clientOrderService.currentState == ClientOrderService.OrderState.Watching ||
                            clientOrderService.currentState == ClientOrderService.OrderState.Ordering)
                        startActivity(new Intent(MainActivityVertical.this, OrderActivity.class));
                    else if (clientOrderService.currentState == ClientOrderService.OrderState.Confirming) {
                        startActivity(new Intent(MainActivityVertical.this, ConfirmActivity.class));
                    } else if (clientOrderService.currentState == ClientOrderService.OrderState.Preparing) {
                        Toast.makeText(getBaseContext(), "We are preparing your order now, thank you for patience", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        FABRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Refresh");
                //clientOrderService.fetchItemRequest();
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
        });
        //new ClientJsonReader().test();
    }

    private void initUI() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_vertical_ntb);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                final View view = LayoutInflater.from(
                        getBaseContext()).inflate(R.layout.item_vp, null, false);

                final TextView txtName = (TextView) view.findViewById(R.id.txt_vp_item_name);
                txtName.setText(items.get(position).getItemName());

                final TextView txtDescrip = (TextView) view.findViewById(R.id.txt_vp_item_descip);
                txtDescrip.setText(items.get(position).getItemDesciption());

                final TextView txtInfo = (TextView) view.findViewById(R.id.txt_vp_item_info);
                txtInfo.setText("$"+ String.valueOf(items.get(position).getItemPrice()));

                txtInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Info#" + position + " clicked");
                        Toast.makeText(MainActivityVertical.this, "Info#" + position + " clicked",
                                Toast.LENGTH_LONG).show();
                    }
                });

                final TextView txtPage = (TextView) view.findViewById(R.id.txt_vp_item_page);
                txtPage.setText(String.format("Page #%d", position));

                final ImageView imgSrc = view.findViewById(R.id.img_item_url);

                final ImageButton imgbtnPlus = (ImageButton) view.findViewById(R.id.imgbtn_item_plus);
                final ImageButton imgbtnMinus = (ImageButton) view.findViewById(R.id.imgbtn_item_minus);

                final TextView txtAmt = (TextView) view.findViewById(R.id.txt_vp_item_num);
                imgbtnPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        orderNumbers.set(position, orderNumbers.get(position) + 1);
                        txtAmt.setText(String.valueOf(orderNumbers.get(position)));
                        txtTotal.setVisibility(View.GONE);
                        if (getTotalOrderNum() > 0){
                            txtTotal.setText(getTotalOrderNum()>10? "10+":String.valueOf(getTotalOrderNum()));
                            txtTotal.setVisibility(View.VISIBLE);
                        }
                    }
                });

                imgbtnMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        orderNumbers.set(position,
                                orderNumbers.get(position) - 1 >= 0? orderNumbers.get(position)-1:orderNumbers.get(position));
                        txtAmt.setText(String.valueOf(orderNumbers.get(position)));
                        txtTotal.setVisibility(View.GONE);
                        if (getTotalOrderNum() > 0){
                            txtTotal.setText(getTotalOrderNum()>10? "10+":String.valueOf(getTotalOrderNum()));
                            txtTotal.setVisibility(View.VISIBLE);
                        }
                    }
                });

                //Log.i(TAG, String.valueOf(orderNumbers.get(position)));
                //txtAmt.setText("hhhh");
                txtAmt.setText(String.valueOf(orderNumbers.get(position)));

                container.addView(view);

                DownloadImageTask downloadImageTask = new
                        DownloadImageTask(imgSrc, items.get(position).getItemUrl(),
                        items.get(position).getItemName());
                startAsyncTasks(downloadImageTask);
                return view;
            }
        });

        final String[] colors = getResources().getStringArray(R.array.vertical_ntb);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_vertical);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();

        Iterator<OrderItem> it = items.iterator();
        int index = 0;
        while (it.hasNext()){
            models.add(
                    new NavigationTabBar.Model.Builder(
                            getResources().getDrawable(R.drawable.ic_third),
                            Color.parseColor(colors[index]))
                            .selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                            .build()
                    );
            it.next();
        }

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 4);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        clientOrderService = ((ClientOrderService.OrderBinder) iBinder).getService();
        Log.i(TAG, "Service connected");

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

    @Override
    protected void onResume() {
        registerReceiver(receiver, new IntentFilter(ClientOrderService.NOTIFICATION));
        Intent intent = new Intent(this, ClientOrderService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        unbindService(this);
        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void startAsyncTasks(AsyncTask asyncTask){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask.execute();
    }

    private class URLLock{
        boolean locked = false;
    }

    private class DownloadImageTask extends AsyncTask<Object, Object, Drawable>{
        ImageView imageView;
        String url;
        String itemName;

        DownloadImageTask(ImageView imageView, String url, String itemName){
            this.imageView = imageView;
            this.url = url;
            this.itemName = itemName;
            Log.i(TAG, "download url: " + this.url);
        }

        @Override
        protected Drawable doInBackground(Object... objects) {
            Drawable drawable = null;
            synchronized (urlLock) {
                //Log.i(TAG, urlLock.toString());
                //Log.i(TAG, Thread.currentThread().toString());
                while (urlLock.locked) {
                    try {
                        urlLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                urlLock.locked = true;
                try {
                    URL urlConnection = new URL(url);
                    Log.i(TAG, url);
                    InputStream is = (InputStream) urlConnection.getContent();
                    drawable = Drawable.createFromStream(is, itemName);
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                urlLock.locked = false;
                urlLock.notify();
                return drawable;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (drawable != null){
                drawable = new ScaleDrawable(drawable, 0,
                        100, 100).getDrawable();
                imageView.setImageDrawable(drawable);
                Log.d(TAG, "image made for "+url);
            }
        }
    }
}
