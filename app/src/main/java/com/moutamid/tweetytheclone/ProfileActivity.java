package com.moutamid.tweetytheclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private ArrayList<PostModel> postsArraylist = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private Utils utils = new Utils();

    private TextView emailTextView, nameTextView;
    private CircleImageView profileImageView;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private static final int GALLERY_REQUEST = 1;
    private String profileImageUrl = null;

    private LottieAnimationView progressBarImageView, progressBar;

    private String otherUserUid = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        emailTextView = findViewById(R.id.email_text_view_activity_profile);
        nameTextView = findViewById(R.id.user_name_activity_profile);
        profileImageView = findViewById(R.id.profile_image_view_activity_profile);
        progressBarImageView = findViewById(R.id.progress_bar_image_view_activity_profile);
        progressBar = findViewById(R.id.progress_bar_activity_profile);
        progressBarImageView.pauseAnimation();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        otherUserUid = getIntent().getStringExtra("uid");

        getUserDetails();

//        profileImageView.setOnClickListener(profileImageViewClickListener());

//        getUserPosts();

    }

    private static class PostModel {

        private String userName, postDate, imageUrl,
                postKey, posterUid, postData;
        private int likesCount;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPostDate() {
            return postDate;
        }

        public void setPostDate(String postDate) {
            this.postDate = postDate;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPostKey() {
            return postKey;
        }

        public void setPostKey(String postKey) {
            this.postKey = postKey;
        }

        public String getPosterUid() {
            return posterUid;
        }

        public void setPosterUid(String posterUid) {
            this.posterUid = posterUid;
        }

        public String getPostData() {
            return postData;
        }

        public void setPostData(String postData) {
            this.postData = postData;
        }

        public int getLikesCount() {
            return likesCount;
        }

        public void setLikesCount(int likesCount) {
            this.likesCount = likesCount;
        }

        public PostModel(String userName, String postDate, String imageUrl, String postKey, String posterUid, String postData, int likesCount) {
            this.userName = userName;
            this.postDate = postDate;
            this.imageUrl = imageUrl;
            this.postKey = postKey;
            this.posterUid = posterUid;
            this.postData = postData;
            this.likesCount = likesCount;
        }

        PostModel() {
        }
    }

    private void getUserPosts() {
        databaseReference.child("posts")
                .orderByChild("posterUid")
                .equalTo(otherUserUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        postsArraylist.clear();

                        if (!snapshot.exists())
                            return;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            PostModel model = dataSnapshot.getValue(PostModel.class);

                            postsArraylist.add(model);

                        }

//                        if (utils.getStoredString(getActivity(), "current_fragment").equals("profile"))
                        initRecyclerView();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);
                        progressBar.pauseAnimation();
                    }
                });
    }

    private static class UserDetails {

        private String name, email, profileUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getProfileUrl() {
            return profileUrl;
        }

        public void setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
        }

        public UserDetails(String name, String email, String profileUrl) {
            this.name = name;
            this.email = email;
            this.profileUrl = profileUrl;
        }

        UserDetails() {
        }
    }

    private void getUserDetails() {
//        nameTextView.setText(
//                utils.getStoredString(ProfileActivity.this, "usernameStr")
//        );
//
//        emailTextView.setText(mAuth.getCurrentUser().getEmail());
//
//        Glide.with(ProfileActivity.this)
//                .load(utils.getStoredString(ProfileActivity.this, "profileUrl"))
//                .apply(new RequestOptions()
//                        .placeholder(R.color.grey)
//                        .error(R.color.grey)
//                )
//                .into(profileImageView);

        databaseReference.child("users").child(otherUserUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (!snapshot.exists()) {
                                    progressBar.setVisibility(View.GONE);
                                    progressBar.pauseAnimation();
                                    Toast.makeText(ProfileActivity.this, "No Profile exists!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                UserDetails details = snapshot.getValue(UserDetails.class);

                                nameTextView.setText(details.getName());

                                emailTextView.setText(details.getEmail());

                                Glide.with(ProfileActivity.this)
                                        .load(details.getProfileUrl())
                                        .apply(new RequestOptions()
                                                .placeholder(R.color.grey)
                                                .error(R.color.grey)
                                        )
                                        .into(profileImageView);

                                getUserPosts();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: "+error.getMessage());
                                progressBar.setVisibility(View.GONE);
                                progressBar.pauseAnimation();
                                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );

    }

    private void initRecyclerView() {

        conversationRecyclerView = findViewById(R.id.profile_activity_recycler_view);
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProfileActivity.this);
        linearLayoutManager.setReverseLayout(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        if (adapter.getItemCount() != 0) {

            progressBar.setVisibility(View.GONE);
            progressBar.pauseAnimation();

            //        noChatsLayout.setVisibility(View.GONE);
            //        chatsRecyclerView.setVisibility(View.VISIBLE);

        }

    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public RecyclerViewAdapterMessages.ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tweet_profile, parent, false);
            return new RecyclerViewAdapterMessages.ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerViewAdapterMessages.ViewHolderRightMessage holder, int position) {
            PostModel post = postsArraylist.get(position);

            holder.postData.setText(post.getPostData());

            holder.postData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(ProfileActivity.this, TweetActivity.class);
                    intent.putExtra("postKey", post.getPostKey());
                    startActivity(intent);

//                    Toast.makeText(ProfileActivity.this, post.getPostKey(), Toast.LENGTH_SHORT).show();
                }
            });

            holder.deleteBtn.setVisibility(View.GONE);

//            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    utils.showDialog(ProfileActivity.this,
//                            "Are you sure?",
//                            "Do you really want to delete this post?",
//                            "Yes",
//                            "No",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//
//                                    postsArraylist.remove(position);
//                                    notifyItemRemoved(position);
//                                    notifyItemRangeChanged(position, getItemCount());
//
//                                    databaseReference.child("posts")
//                                            .child(post.getPostKey())
//                                            .removeValue();
//
//                                    Toast.makeText(ProfileActivity.this, "Done", Toast.LENGTH_SHORT).show();
//
//                                    dialogInterface.dismiss();
//                                }
//                            }, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    dialogInterface.dismiss();
//                                }
//                            }, true);
//                }
//            });

        }

        @Override
        public int getItemCount() {
            if (postsArraylist == null)
                return 0;
            return postsArraylist.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView postData;
            ImageView deleteBtn;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                postData = v.findViewById(R.id.post_data_text_view_profile_tweet_layout);
                deleteBtn = v.findViewById(R.id.delete_post_image_view_profile_tweet_layout);

            }
        }

    }


}