package com.example.chat.chats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.common.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.chatListViewHolder> {
    private Context context;
    private List<ChatModel> chatModelList;

    public ChatListAdapter(Context context, List<ChatModel> chatModelList) {
        this.context = context;
        this.chatModelList = chatModelList;
    }

    @NonNull
    @Override
    public chatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout,parent,false);
        return new chatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull chatListViewHolder holder, int position) {
        ChatModel chatModel = chatModelList.get(position);

        holder.tvFullName.setText(chatModel.getUserName());
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_FOLDER + "/" + chatModel.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .into(holder.ivProfile);
            }
        });
        holder.llChatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Constants.USER_KEY,chatModel.getUserId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public class chatListViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llChatList;
        private TextView tvFullName, tvLastMessage, tvLastMessageTime, tvUnreadCount;
        private ImageView ivProfile;
        public chatListViewHolder(@NonNull View itemView) {
            super(itemView);

            llChatList = itemView.findViewById(R.id.llChatList);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }
}
