package com.chernowii.rides;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class RidesOnWear extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    //Buttons in the app
    private ImageButton Pickup_Custom;
    private ImageButton Pickup_My_Location;
    private ImageButton Choose_Dropoff_Location;
    private ImageButton Confirm_Ride;

    //Other
    private static final int SPEECH_REQUEST_CODE = 0;

    private static final int SPEECH_REQUEST_CODE_2 = 2;

    //Google Play Services wear

    Node wearNode;
    GoogleApiClient wearGoogleApiClient;
    private boolean mResolvingError=false;

    Boolean pickup_ready=false;
    Boolean dropoff_ready=false;

    public TextView pickupText;
    public TextView dropoffText;
    public TextView priceText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides_on_wear);
        wearGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        registerReceiver(getPrice, new IntentFilter("PRICERECEIVED"));

        pickupText= (TextView)findViewById(R.id.pickup_text);
        dropoffText=(TextView)findViewById(R.id.dropoff_text);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                Pickup_Custom = (ImageButton) stub.findViewById(R.id.custom_pickup);
                Pickup_My_Location = (ImageButton) stub.findViewById(R.id.mylocation_pickup);
                Choose_Dropoff_Location = (ImageButton) stub.findViewById(R.id.choose_dropoff);
                Confirm_Ride = (ImageButton) stub.findViewById(R.id.confirm_ride);

                Pickup_Custom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        startActivityForResult(intent, SPEECH_REQUEST_CODE);

                    }
                });

                Pickup_My_Location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       sendtophone("/pickup-my-location","null");
                        pickup_ready=true;
                        pickupText= (TextView)findViewById(R.id.pickup_text);
                        dropoffText=(TextView)findViewById(R.id.dropoff_text);
                        pickupText.setText("My Location");
                    }
                });

                Choose_Dropoff_Location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        startActivityForResult(intent, SPEECH_REQUEST_CODE_2);

                    }
                });

                Confirm_Ride.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(pickup_ready && dropoff_ready) {
                            sendtophone("/confirm-ride", "null");
                            Intent intent = new Intent(RidesOnWear.this, ConfirmationActivity.class);
                            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Pickup/dropoff location missing!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            final String spokenText = results.get(0);
            new AlertDialog.Builder(RidesOnWear.this)
                    .setTitle("Pickup Address confirmation")
                    .setMessage(spokenText)
                    .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            pickupText= (TextView)findViewById(R.id.pickup_text);
                            dropoffText=(TextView)findViewById(R.id.dropoff_text);
                            sendtophone("/pickup-raw",spokenText);
                            pickup_ready=true;
                            pickupText.setText(spokenText);
                        }
                    })
                    .setNegativeButton("repeat", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            startActivityForResult(intent, SPEECH_REQUEST_CODE);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if (requestCode == SPEECH_REQUEST_CODE_2 && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            final String spokenText = results.get(0);
            new AlertDialog.Builder(RidesOnWear.this)
                    .setTitle("Dropoff Address confirmation")
                    .setMessage(spokenText)
                    .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            pickupText= (TextView)findViewById(R.id.pickup_text);
                            dropoffText=(TextView)findViewById(R.id.dropoff_text);
                            sendtophone("/dropoff-raw",spokenText);
                            dropoff_ready=true;
                            dropoffText.setText(spokenText);
                            if(pickup_ready && dropoff_ready){
                                sendtophone("/price-estimate","null");
                            }
                        }
                    })
                    .setNegativeButton("repeat", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            startActivityForResult(intent, SPEECH_REQUEST_CODE_2);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void sendtophone(String path, String data) {
        if (wearNode != null && wearGoogleApiClient!=null && wearGoogleApiClient.isConnected()) {

            Wearable.MessageApi.sendMessage(
                    wearGoogleApiClient, wearNode.getId(), path, data.getBytes()).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e("TAG", "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }else{
            Toast.makeText(getApplicationContext(),
                    "No connection to phone", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),
                    "Connect watch to phone!", Toast.LENGTH_SHORT).show();

        }

    }
    private BroadcastReceiver getPrice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            priceText= (TextView)findViewById(R.id.price_text);
            priceText.setText("Est. Price: "+ intent.getExtras().getString("price_of_ride"));

        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            wearGoogleApiClient.connect();
        }
    }

    /*
    * Resolve the node = the connected device to send the message to
    */
    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(wearGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    wearNode = node;
                }
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(),                     "Error, not connected to phone!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(),                     "Error, not connected to phone!", Toast.LENGTH_SHORT).show();
    }
}
