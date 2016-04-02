package com.chernowii.rides;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.uber.sdk.android.rides.RequestButton;
import com.uber.sdk.android.rides.RideParameters;

public class ButtonUber extends AppCompatActivity {
    float pickupLocationLatitude = 0;
    float pickupLocationLongitude = 0;
    String pickupShortName = "";
    String pickupAddress = "";

    float dropoffLocationLatitude = 0;
    float dropoffLocationLongitude = 0;
    String dropoffShortName = "";
    String dropoffAddress = "";
    Boolean pickup_getlocation=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_uber);
        String clientId = getString(R.string.client_id);
        Intent intent = getIntent();
        pickup_getlocation=intent.getBooleanExtra("pickup-get-location",true);

        RequestButton uberButtonBlack = (RequestButton) findViewById(R.id.uber_button_black);
        if(!pickup_getlocation) {
            pickupLocationLatitude = intent.getFloatExtra("pickup_location_latitude",0);
            pickupLocationLongitude = intent.getFloatExtra("pickup_location_longitude",0);

            pickupShortName=intent.getStringExtra("pickup_short_name");
            pickupAddress=intent.getStringExtra("pickup_address");

            dropoffLocationLatitude = intent.getFloatExtra("dropoff_location_latitude",0);
            dropoffLocationLongitude = intent.getFloatExtra("dropoff_location_longitude",0);

            dropoffShortName=intent.getStringExtra("dropoff_short_name");
            dropoffAddress=intent.getStringExtra("dropoff_address");
            RideParameters rideParameters = new RideParameters.Builder()
                    .setPickupLocation(pickupLocationLatitude, pickupLocationLongitude, pickupShortName,pickupAddress)
                    .setDropoffLocation(dropoffLocationLatitude, dropoffLocationLongitude, dropoffShortName, dropoffAddress)
                    .build();

            if (uberButtonBlack != null) {
                uberButtonBlack.setRideParameters(rideParameters);
            }
        }
        if(pickup_getlocation) {
            dropoffLocationLatitude = intent.getFloatExtra("dropoff_location_latitude",0);
            dropoffLocationLongitude = intent.getFloatExtra("dropoff_location_longitude",0);

            dropoffShortName=intent.getStringExtra("dropoff_short_name");
            dropoffAddress=intent.getStringExtra("dropoff_address");
            RideParameters rideParameters = new RideParameters.Builder()
                    .setPickupToMyLocation()
                    .setDropoffLocation(dropoffLocationLatitude, dropoffLocationLongitude, dropoffShortName, dropoffAddress)
                    .build();

            if (uberButtonBlack != null) {
                uberButtonBlack.setRideParameters(rideParameters);
            }
        }
    }
}
