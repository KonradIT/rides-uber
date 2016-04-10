package com.chernowii.rides;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.neno0o.ubersdk.Endpoints.Models.Prices.Price;
import com.neno0o.ubersdk.Endpoints.Models.Prices.Prices;
import com.neno0o.ubersdk.Uber;
import com.squareup.okhttp.Response;
import com.uber.sdk.android.rides.RequestButton;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.UberButton;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;


/**
 * Created by Konrad Iturbe on 03/30/16.
 */
public class UberWearListener extends WearableListenerService {
    String pid = "";
    float pickupLocationLatitude = 0;
    float pickupLocationLongitude = 0;
    String pickupShortName = "";
    String pickupAddress = "";

    float dropoffLocationLatitude = 0;
    float dropoffLocationLongitude = 0;
    String dropoffShortName = "";
    String dropoffAddress = "";
    Boolean pickup_custom=true;
    GoogleApiClient mGoogleApiClient;
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);


        if (messageEvent.getPath().equals("/confirm-ride")) {

            Intent startUberButton = new Intent(this, ButtonUber.class);
            if (pickup_custom) {
                startUberButton.putExtra("pickup-get-location", false);

                startUberButton.putExtra("pickup_location_latitude", pickupLocationLatitude);
                startUberButton.putExtra("pickup_location_longitude", pickupLocationLongitude);
                startUberButton.putExtra("pickup_short_name", pickupShortName);
                startUberButton.putExtra("pickup_address", pickupAddress);
            }
            if (!pickup_custom) {
                startUberButton.putExtra("pickup-get-location", true);
            }

            startUberButton.putExtra("dropoff_location_latitude", dropoffLocationLatitude);
            startUberButton.putExtra("dropoff_location_longitude", dropoffLocationLongitude);
            startUberButton.putExtra("dropoff_short_name", dropoffShortName);
            startUberButton.putExtra("dropoff_address", dropoffAddress);
            startUberButton.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startUberButton);
        }
        if (messageEvent.getPath().equals("/pickup-raw")) {
            pickup_custom = true;
            Geocoder geocoder = new Geocoder(UberWearListener.this, Locale.US);
            List<Address> listOfAddress;
            try {
                listOfAddress = geocoder.getFromLocationName(messageEvent.getData().toString(), 1);
                if (listOfAddress != null && !listOfAddress.isEmpty()) {
                    Address address = listOfAddress.get(0);

                    pickupLocationLatitude = (float) address.getLatitude();
                    pickupLocationLongitude = (float) address.getLongitude();
                    pickupAddress = String.valueOf(messageEvent.getData());
                    pickupShortName = address.getLocality(); //using this for now.

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (messageEvent.getPath().equals("/pickup-my-location")) {
            pickup_custom = false;

        }
        if (messageEvent.getPath().equals("/dropoff-raw")) {
            Geocoder geocoder = new Geocoder(UberWearListener.this, Locale.US);
            List<Address> listOfAddress;
            try {
                listOfAddress = geocoder.getFromLocationName(messageEvent.getData().toString(), 1);
                if (listOfAddress != null && !listOfAddress.isEmpty()) {
                    Address address = listOfAddress.get(0);

                    dropoffLocationLatitude = (float) address.getLatitude();
                    dropoffLocationLongitude = (float) address.getLongitude();
                    dropoffAddress = String.valueOf(messageEvent.getData());
                    dropoffShortName = address.getLocality(); //using this for now.

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (messageEvent.getPath().equals("/price-estimate")) {
            /*
        }
            Uber.getInstance().getUberAPIService().getPriceEstimates(37.7786,
                    122.3893,
                    37.7944,
                    122.3958,
                    new Callback<Prices>() {
                        @Override
                        public void success(Prices prices, retrofit.client.Response response) {
                            List<Price> PriceList = prices.getPrices();
                            Toast.makeText(getApplicationContext(),"Price: " + PriceList.get(0),Toast.LENGTH_SHORT).show();
                            */

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            sendMessage();
                        }
                        @Override
                        public void onConnectionSuspended(int cause) {

                        }
                    }).build();
            mGoogleApiClient.connect();

        }
    }
    private void getNodes() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        HashSet<String> results = new HashSet<String>();
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            results.add(node.getId());
                        }
                        sendMessageApi(results);
                    }
                }
        );
    }

    private void sendMessageApi(Collection<String> nodes) {
        for (String node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, "/price", "REKT".getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {


                            } else {

                                try {
                                    finalize();
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }

                        }
                    }
            );
        }
    }

    private void sendMessage() {
        getNodes();
    }


    }

