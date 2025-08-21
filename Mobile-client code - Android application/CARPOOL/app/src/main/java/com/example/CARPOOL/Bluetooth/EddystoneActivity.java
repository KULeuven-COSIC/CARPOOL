package com.example.CARPOOL.Bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

/**
 * Activity to read bluetooth advertisements and send the read message back to calling activity
 *
 * We use the Eddystone URL format for bluetooth advertisements to make
 */
public class EddystoneActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    private static final String TAG = "EddystoneActivity";

    private BeaconManager beaconManager;
    private String device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eddystone);
        setTitle("Bluetooth beacon interaction");
        Intent intent = getIntent();
        device_id = intent.getStringExtra("device_id");

        setUp();
    }



    public void setUp() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG,"Start");
        Region region = new Region("allbeaconsregion",null,null,null);  // Identifiers can be added to narrow down the admitted beacons (don't know the fine details myself)
        beaconManager.startRangingBeacons(region);                                          // start scanning/reading
        beaconManager.addRangeNotifier(this);                                               // adds a notifier: does something when beacon is in range


    }

    /**
     * Called every second. All beacons found (and their packet content) are in the "beacons" collection
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.d(TAG, "Amount of beacons: " + beacons.size());
        for (Beacon beacon : beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // Inside a clause only for URL frames
                String hexString = beacon.getId1().toHexString();
                Log.d(TAG,"Hexstring: " + hexString);

                String rcvd_id = hexString.substring(4, 6);

                if (rcvd_id.equals(device_id)) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("Hexstring", hexString);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.bind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}