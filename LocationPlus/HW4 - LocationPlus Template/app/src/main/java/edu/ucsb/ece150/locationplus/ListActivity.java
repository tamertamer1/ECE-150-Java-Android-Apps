package edu.ucsb.ece150.locationplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.app.AlertDialog;
import android.widget.TextView;

public class ListActivity extends AppCompatActivity {
    private ArrayList<Satellite> satelliteList;
    private ListView listView;
    private TextView tvTotalSatellites;
    private TextView tvSatellitesUsedInFix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        tvTotalSatellites = findViewById(R.id.tvTotalSatellites);
        tvSatellitesUsedInFix = findViewById(R.id.tvSatellitesUsedInFix);
        satelliteList = (ArrayList<Satellite>) getIntent().getSerializableExtra("SatelliteList");
        listView = findViewById(R.id.satelliteList);

        ArrayAdapter<Satellite> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                satelliteList
        );

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this::onSatelliteClick);
        int totalSatellites = satelliteList.size();
        int satellitesUsedInFix = calculateSatellitesUsedInFix(satelliteList); // You need to implement this method

        tvTotalSatellites.setText("Number of Satellites: " + totalSatellites);
        tvSatellitesUsedInFix.setText("Number Used In Fix: " + satellitesUsedInFix);
    }

    private void onSatelliteClick(AdapterView<?> parent, View view, int position, long id) {
        Satellite satellite = satelliteList.get(position);
        showSatelliteDetails(satellite);
    }
    private int calculateSatellitesUsedInFix(ArrayList<Satellite> satellites) {
        int count = 0;
        for (Satellite satellite : satellites) {
            if (satellite.isUsedInFix()) {
                count++;
            }
        }
        return count;
    }
    private void showSatelliteDetails(Satellite satellite) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Satellite " + satellite.getSatelliteNumber())
                .setMessage("Azimuth: " + satellite.getAzimuth() + "°\n" +
                        "Elevation: " + satellite.getElevation() + "°\n" +
                        "Carrier Frequency: " + satellite.getCarrierFrequency() + " Hz\n" +
                        "C/N0: " + satellite.getCarrierNoiseDensity() + " dB Hz\n" +
                        "Constellation: " + satellite.getConstellationName() + "\n" +
                        "SVID: " + satellite.getSvid())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


