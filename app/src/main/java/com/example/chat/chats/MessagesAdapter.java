package com.example.chat.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messsageList;
    private FirebaseAuth firebaseAuth;


    public MessagesAdapter(Context context, List<MessageModel> messsageList) {
        this.context = context;
        this.messsageList = messsageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout,parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messsageList.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String fromUserId = message.getMessageFrom();

        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        String dateTime = sfd.format(new Date(message.getMessageTime()));
        String [] splitString = dateTime.split(" ");
        String messageTime = splitString[1];

        if(fromUserId.equals(currentUserId))
        {
            holder.llSent.setVisibility(View.VISIBLE);
            holder.llReceived.setVisibility(View.GONE);
            holder.tvSentMessage.setText(message.getMessage());
            holder.tvSentMessageTime.setText(messageTime);
        }
        else
        {
            holder.llReceived.setVisibility(View.VISIBLE);
            holder.llSent.setVisibility(View.GONE);
            holder.tvReceivedMessage.setText(message.getMessage());
            holder.tvReceivedMessageTime.setText(messageTime);
        }
    }

    @Override
    public int getItemCount() {
        return messsageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSent,llReceived;
        private TextView tvSentMessage,tvSentMessageTime,tvReceivedMessage,tvReceivedMessageTime;
        private ConstraintLayout clMessage;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            llSent = itemView.findViewById(R.id.llSent);
            llReceived = itemView.findViewById(R.id.llReceived);
            tvSentMessage = itemView.findViewById(R.id.tvSentMessage);
            tvSentMessageTime = itemView.findViewById(R.id.tvSentMessageTime);
            tvReceivedMessage = itemView.findViewById(R.id.tvRecivedMessage);
            tvReceivedMessageTime = itemView.findViewById(R.id.tvReceivedMessageTime);
            clMessage = itemView.findViewById(R.id.clMessage);
        }
    }
}
