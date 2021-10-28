package com.example.chat.searchfriends;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.common.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.chat.common.nodeNames;
import java.net.URI;
import java.util.List;

public class searchFriendsAdapter extends RecyclerView.Adapter<searchFriendsAdapter.searchFriendsViewHolder> {

    private Context context;
    private List<SearchFriendModel> searchFriendModelList;
    private DatabaseReference friendRequestDatabase;
    private FirebaseUser currentUser;
    private String userID;
    public searchFriendsAdapter(Context context, List<SearchFriendModel> SearchFriendModelList) {
        this.context = context;
        this.searchFriendModelList = SearchFriendModelList;
    }

    @NonNull
    @Override
    public searchFriendsAdapter.searchFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.searchfriends,parent,false);
        return new searchFriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull searchFriendsViewHolder holder, int position) {
        SearchFriendModel searchFriendModel = searchFriendModelList.get(position);

        holder.tvfullname.setText(searchFriendModel.getUserName());
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_FOLDER + "/" + searchFriendModel.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .into(holder.ivPhoto);
            }
        });

        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child(nodeNames.FRIEND_REQUESTS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(searchFriendModel.isRequestSend())
        {
            holder.btnSendRequest.setVisibility(View.GONE);
        }
        else
        {
            holder.btnSendRequest.setVisibility(View.VISIBLE);
        }
        holder.btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.btnSendRequest.setVisibility(View.GONE);

                userID = searchFriendModel.getUserId();

                friendRequestDatabase.child(currentUser.getUid()).child(userID).child(nodeNames.REQUEST_TYPE).setValue(Constants.REQUEST_SENT)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestDatabase.child(userID).child(currentUser.getUid()).child(nodeNames.REQUEST_TYPE).setValue(Constants.REQUEST_RECEIVED)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(context,"Se ha enviado la solicitud de amistad",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(context, context.getString(R.string.errorSendingFriendRequest,task.getException()),Toast.LENGTH_SHORT).show();
                                        holder.btnSendRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(context, context.getString(R.string.errorSendingFriendRequest,task.getException()),Toast.LENGTH_SHORT).show();
                            holder.btnSendRequest.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return searchFriendModelList.size();
    }

    public class searchFriendsViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private TextView tvfullname;
        private ImageButton btnSendRequest;
        public searchFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvfullname = itemView.findViewById(R.id.FullName);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);

        }
    }
}
