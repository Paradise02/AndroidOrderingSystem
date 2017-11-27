package com.example.orangeparadise.clientorderdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;


/**
 * Created by minhu on 2017/11/27.
 */



public class FinalResult extends Activity{
    FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_result);
        addListenerOnButton();
    }

    private void addListenerOnButton() {

        final Context context=this;

        button = (FloatingActionButton) findViewById(R.id.back_to_menu);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivityVertical.class);
                startActivity(intent);
            }
        });

    };
}

