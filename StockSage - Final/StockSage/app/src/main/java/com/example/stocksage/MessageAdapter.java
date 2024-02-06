package com.example.stocksage;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Context context, List<Message> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_message, parent, false);
        }

        TextView textViewMessage = convertView.findViewById(R.id.textViewMessage);
        LinearLayout messageContainer = convertView.findViewById(R.id.messageContainer);

        textViewMessage.setText(message.getText());

        if (message.isUser()) {
            textViewMessage.setBackgroundResource(R.drawable.user_message_background);
            messageContainer.setGravity(Gravity.END); // Align user bubble to the right
        } else {
            textViewMessage.setBackgroundResource(R.drawable.receiver_message_background);
            messageContainer.setGravity(Gravity.START); // Align receiver bubble to the left
        }

        return convertView;
    }
}
