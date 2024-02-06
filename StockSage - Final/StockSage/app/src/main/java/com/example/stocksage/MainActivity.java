package com.example.stocksage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.content.SharedPreferences;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
public class MainActivity extends AppCompatActivity {
    private EditText editTextMessage;
    private ListView listViewMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messages;
    private static final String PREFS_NAME = "StockSagePrefs";
    private static final String MESSAGES_KEY = "Messages";
    @Override
    protected void onPause() {
        super.onPause();
        saveMessages();
    }

    private void saveMessages() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(messages);
        editor.putString(MESSAGES_KEY, jsonMessages);
        editor.apply();
    }

    private void loadMessages() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonMessages = prefs.getString(MESSAGES_KEY, null);
        if (jsonMessages != null) {
            Gson gson = new Gson();
            messages = gson.fromJson(jsonMessages, new TypeToken<ArrayList<Message>>(){}.getType());
        } else {
            messages = new ArrayList<>();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMessage = findViewById(R.id.editTextMessage);
        listViewMessages = findViewById(R.id.listViewMessages);
        Button buttonSend = findViewById(R.id.buttonSend);

        if (savedInstanceState != null && savedInstanceState.containsKey("messages")) {
            messages = savedInstanceState.getParcelableArrayList("messages");
        } else {
            loadMessages(); // Load messages from SharedPreferences or initialize new list
        }

        adapter = new MessageAdapter(this, messages);
        listViewMessages.setAdapter(adapter);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editTextMessage.getText().toString();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText);
                    editTextMessage.setText(""); // Clear the EditText after sending the message
                }
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("messages", messages);
    }
    private void sendMessage(String messageText) {
        Message userMessage = new Message(messageText, true);
        messages.add(userMessage);
        adapter.notifyDataSetChanged(); // Notify the adapter of the data change
        scrollToBottom();

        // Simulate a response
        receiveMessage("Downloading " + messageText + "...");

        DownloadStock.download(messageText, (result -> {
            runOnUiThread(() -> receiveMessage(result));
        }));
    }

    private void receiveMessage(String messageText) {
        Message replyMessage = new Message(messageText, false);
        messages.add(replyMessage);
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void scrollToBottom() {
        listViewMessages.post(new Runnable() {
            @Override
            public void run() {
                // scroll the list view to the last item
                listViewMessages.smoothScrollToPosition(adapter.getCount() - 1);
            }
        });
    }
}