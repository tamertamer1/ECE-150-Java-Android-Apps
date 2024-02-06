package edu.ucsb.ece150.gauchopay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;
import java.util.Arrays;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CardListActivity extends AppCompatActivity {

    private static final int RC_HANDLE_INTERNET_PERMISSION = 2;

    private ArrayList<String> cardArray;
    private ArrayAdapter adapter;

    private ListView cardList;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Launch the asynchronous process to grab the web API
                    new ReadWebServer(getApplicationContext()).execute("");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadCardListFromPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        // Ensure that we have Internet permissions
        int internetPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if(internetPermissionGranted != PackageManager.PERMISSION_GRANTED) {
            final String[] permission = new String[] {Manifest.permission.INTERNET};
            ActivityCompat.requestPermissions(this, permission, RC_HANDLE_INTERNET_PERMISSION);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (cardArray == null || cardArray.isEmpty()) {
            if (savedInstanceState != null) {
                cardArray = savedInstanceState.getStringArrayList("SavedCardList");
            }
            if (cardArray == null) cardArray = new ArrayList<>();
        }

        cardList = findViewById(R.id.cardList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cardArray);
        cardList.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddCardActivity = new Intent(getApplicationContext(), AddCardActivity.class);
                startActivityForResult(toAddCardActivity, 1);
            }
        });

        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = (int) id;

                // If "lastAmount > 0" the last API call is a valid request (that the user must
                // respond to.
                if (ReadWebServer.getLastAmount() != 0) {
                    // [TODO] Send the card information back to the web API. Reference the
                    // WriteWebServer constructor to know what information must be passed.
                    // Get the card number from the cardArray based on the position in the array.
                    new WriteWebServer(getApplicationContext(),cardArray.get(position).split(" ")[1]).execute();
                    // Reset the stored information from the last API call
                    ReadWebServer.resetLastAmount();
                }
            }
        });

        // Start the timer to poll the webserver every 5000 ms
        timer.schedule(task, 0, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] This is a placeholder. Modify the card information in the cardArray ArrayList
        // accordingly.

        // This is how you tell the adapter to update the ListView to reflect the current state
        // of your ArrayList (which holds all of the card information).
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            final String CardNum = data.getStringExtra("CardNum");
            if (CardNum != null) {
                String formattedString = "Card " + CardNum;
                cardArray.add(formattedString);
            }
            saveCardListToPreferences();
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,cardArray);
            cardList=findViewById(R.id.cardList);
            cardList.setAdapter(adapter);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("SavedCardList", cardArray);
    }

    private void saveCardListToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("CardPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Convert the ArrayList into a single string for easy storage
        String cardsString = TextUtils.join(",", cardArray);
        editor.putString("cardArray", cardsString);
        editor.apply();
    }

    private void loadCardListFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("CardPrefs", MODE_PRIVATE);
        String cardsString = sharedPreferences.getString("cardArray", "");
        if (!cardsString.equals("")) {
            String[] cards = cardsString.split(",");
            cardArray = new ArrayList<>(Arrays.asList(cards));
        }
    }

}

