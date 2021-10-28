package com.example.chat.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chat.common.Constants;
import com.example.chat.common.Util;
import com.example.chat.common.nodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView  ivSend;
    private EditText etMessage;
    private DatabaseReference mRootRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId, chatUserId;
    private RecyclerView rvMessages;
    private SwipeRefreshLayout srlMessages;
    private MessagesAdapter messagesAdapter;
    private List<MessageModel> messagesList;

    private int currentPage = 1;
    private static final int RECORD_PER_PAGE =30;

    private DatabaseReference databaseReferenceMessages;
    private ChildEventListener childEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ivSend = findViewById(R.id.ivSend);
        etMessage = findViewById(R.id.etMessage);
        ivSend.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseAuth.getCurrentUser().getUid();


        if(getIntent().hasExtra(Constants.USER_KEY))
        {
            chatUserId = getIntent().getExtras().get(Constants.USER_KEY).toString();
        }
        rvMessages = findViewById(R.id.rvMessages);
        srlMessages = findViewById(R.id.srlMessages);
        messagesList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this,messagesList);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messagesAdapter);

        loadMessages();
        rvMessages.scrollToPosition(messagesList.size()-1);
        srlMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                loadMessages();
            }
        });
    }

    private void sendMessage(String msg, String msgType, String pushId)
    {
        try
        {
            if(!msg.equals(""))
            {
                HashMap messageMap = new HashMap();
                messageMap.put(nodeNames.MESSAGE_ID,pushId);
                messageMap.put(nodeNames.MESSAGE,msg);
                messageMap.put(nodeNames.MESSAGE_TYPE,msgType);
                messageMap.put(nodeNames.MESSAGE_FROM,currentUserId);
                messageMap.put(nodeNames.MESSAGE_TIME, ServerValue.TIMESTAMP);

                String currentUserRef = nodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId;
                String chatUserRef = nodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId;

                HashMap messageUserMap = new HashMap();
                messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                etMessage.setText("");
                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error != null)
                        {
                            Toast.makeText(ChatActivity.this, getString(R.string.Failed_Message_Send,error.getMessage()),Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this,getString(R.string.message_sent),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
        catch (Exception e)
        {
            Toast.makeText(ChatActivity.this, getString(R.string.Failed_Message_Send,e.getMessage()),Toast.LENGTH_SHORT).show();
        }
    }


    private void loadMessages()
    {
        messagesList.clear();
        databaseReferenceMessages = mRootRef.child(nodeNames.MESSAGES).child(currentUserId).child(chatUserId);
        //Loads the amount of messages depending on the swipes of the user
        Query messageQuery = databaseReferenceMessages.limitToLast(currentPage * RECORD_PER_PAGE);

        if(childEventListener != null){
            messageQuery.removeEventListener(childEventListener);
        }
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel message = snapshot.getValue(MessageModel.class);

                messagesList.add(message);
                messagesAdapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(messagesList.size()-1);
                srlMessages.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                srlMessages.setRefreshing(false);
            }
        };

        messageQuery.addChildEventListener(childEventListener);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.ivSend:
                if (Util.conectionAvailable(this)) {
                    DatabaseReference userMessagePush = mRootRef.child(nodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
                    String pushId = userMessagePush.getKey();
                    sendMessage(etMessage.getText().toString().trim(), Constants.MESSAGE_TYPE_TEXT, pushId);

                }else
                {
                    Toast.makeText(this,R.string.chk_internet,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}