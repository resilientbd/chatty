package com.faisal.chatty.friend;
/**
 * Create by Sk. Faisal
 * purpose: display friend list
 * email:faisal.hossain.pk@gmail.com
 * github:https://github.com/resilientbd
 * Date: 03/07/2020
 */
/*
    IN THIS FRAGMENT A RECYCLER VIEW HOLDS THE DETAILS OF ALL FRIENDS
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.faisal.chatty.chat.ChatActivity;
import com.faisal.chatty.profile.ProfileActivity;
import com.faisal.chatty.R;
import com.faisal.util.remote.model.Friends;
import com.faisal.util.local.PermissionUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * IN THIS FRAGMENT A RECYCLER VIEW HOLDS THE DETAILS OF ALL FRIENDS
 */
public class FriendFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friend, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friendRecycleList);
        mAuth = FirebaseAuth.getInstance();

        //---CURRENT USER ID--
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrent_user_id);
        mFriendDatabase.keepSynced(true);

        //---USERS DATA
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //---FETCHING DATABASE FROM mFriendDatabase USING Friends.class AND ADDING TO RECYCLERVIEW----
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecycleAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.recycle_list_single_user,
                FriendsViewHolder.class,
                mFriendDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendViewHolder,
                                              Friends friends, int position) {
                friendViewHolder.setDate(friends.getDate());
                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //---IT WORKS WHENEVER CHILD OF mMessageDatabase IS CHANGED---

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userthumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendViewHolder.setOnline(userOnline);

                        }
                        friendViewHolder.setName(userName);
                        friendViewHolder.setUserImage(userthumbImage, getContext());

                        //--ALERT DIALOG FOR OPEN PROFILE OR SEND MESSAGE----

                        friendViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence[] options = new CharSequence[]{"Open Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if (which == 0) {
                                            Intent intent = new Intent(getContext(), ProfileActivity.class);
                                            intent.putExtra("user_id", list_user_id);
                                            startActivity(intent);
                                        }

                                        if (which == 1) {
                                            if (PermissionUtil.on(getActivity()).request(PermissionUtil.REQUEST_CODE_PERMISSION_CAMERA,
                                                    Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA)) {
                                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                                intent.putExtra("user_id", list_user_id);
                                                intent.putExtra("user_name", userName);
                                                startActivity(intent);
                                            }
                                        }

                                    }
                                });
                                builder.show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mFriendsList.setAdapter(friendsRecycleAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDate(String date) {

            TextView userNameView = (TextView) mView.findViewById(R.id.textViewSingleListStatus);
            userNameView.setText(date);

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.textViewSingleListName);
            userNameView.setText(name);

        }

        public void setUserImage(String userThumbImage, Context ctx) {
            CircleImageView userImageview = (CircleImageView) mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.with(ctx).load(userThumbImage).placeholder(R.drawable.user_img).into(userImageview);
        }

        public void setOnline(String isOnline) {
            ImageView online = (ImageView) mView.findViewById(R.id.userSingleOnlineIcon);
            if (isOnline.equals("true")) {
                online.setVisibility(View.VISIBLE);
            } else {
                online.setVisibility(View.INVISIBLE);
            }
        }
    }
}
