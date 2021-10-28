package com.example.chat.searchfriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chat.common.Constants;
import com.example.chat.common.nodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchFriendFragment extends Fragment {

    private RecyclerView rvFindFriends;
    private searchFriendsAdapter searchFriendsAdapter;
    private List<SearchFriendModel> searchFriendModelList;
    private TextView tvEmptyFriendsList;

    private DatabaseReference databaseReference, databaseReferenceFriendRequests;
    private FirebaseUser currentUser;
    private  View progressBar;


    public SearchFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFindFriends = view.findViewById(R.id.rvSearchFriends);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyFriendsList = view.findViewById(R.id.tvEmptyFriendsList);

        rvFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchFriendModelList =new ArrayList<>();
        searchFriendsAdapter = new searchFriendsAdapter(getActivity(), searchFriendModelList);
        rvFindFriends.setAdapter(searchFriendsAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(nodeNames.USERS);
        currentUser  = FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference().child(nodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        tvEmptyFriendsList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Query query = databaseReference.orderByChild(nodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchFriendModelList.clear();
                for (final DataSnapshot ds : dataSnapshot.getChildren())
                {
                    final String userId= ds.getKey();

                    if(userId.equals(currentUser.getUid()))
                        continue;

                    if(ds.child(nodeNames.NAME).getValue()!=null)
                    {
                        final String fullName = ds.child(nodeNames.NAME).getValue().toString();
                        final String photoName = ds.child(nodeNames.PHOTO).getValue()!=null? ds.child(nodeNames.PHOTO).getValue().toString():"";

                        databaseReferenceFriendRequests.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    String requestType =  dataSnapshot.child(nodeNames.REQUEST_TYPE).getValue().toString();
                                    if(requestType.equals(Constants.REQUEST_STATUS_SENT))
                                    {
                                        searchFriendModelList.add(new SearchFriendModel(fullName, photoName, userId, true));
                                        searchFriendsAdapter.notifyDataSetChanged();

                                    }
                                }
                                else{
                                    searchFriendModelList.add(new SearchFriendModel(fullName, photoName, userId, false));
                                    searchFriendsAdapter.notifyDataSetChanged();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });


                        tvEmptyFriendsList.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getContext().getString( R.string.failed_to_fetch_friends, databaseError.getMessage())
                        , Toast.LENGTH_SHORT).show();
            }
        });

    }
}