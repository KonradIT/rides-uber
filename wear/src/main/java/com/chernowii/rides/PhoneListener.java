package com.chernowii.rides;

import android.content.Intent;
import android.widget.TextView;

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

            Intent i = new Intent("PRICERECEIVED");
            i.putExtra("price_of_ride",messageEvent.getData());

            sendBroadcast(i);
        }
    }
}
