package com.chernowii.rides;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Konrad Iturbe on 04/03/16.
 */
public class PhoneListener extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if(messageEvent.getPath().equals("/price")){

            Toast.makeText(getApplicationContext(),messageEvent.getData().toString(),Toast.LENGTH_SHORT).show();
        }
    }
}
