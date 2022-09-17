package com.moutamid.tweetytheclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TweetActivity extends AppCompatActivity {
    private static final String TAG = "TweetActivity";

    private PostModel currentPostModel;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private Utils utils = new Utils();

    private RelativeLayout userProfileLayout;
    private TextView userNameTextView, postDateTextView, postDataTextView, likesCountTextView;
    private CircleImageView userProfileImageView;
    private LikeButton likeButton;

    private String postKeyStr = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        userProfileLayout = findViewById(R.id.user_profile_layout_post_tweet_activity);
        userNameTextView = findViewById(R.id.user_name_post_tweet_activity);
        postDateTextView = findViewById(R.id.user_date_post_tweet_activity);
        userProfileImageView = findViewById(R.id.user_profile_imageview_post_tweet_activity);
        postDataTextView = findViewById(R.id.post_data_text_view_tweet_activity);
        likesCountTextView = findViewById(R.id.likes_count_text_view_post_tweet_activity);
        likeButton = findViewById(R.id.like_button_post_tweet_activity);
        setbackBtnListener();

        setAddReplyBtnClickListener();
        setRetweetBtnClickListener();
        setMessageBtnClickListener();

        postKeyStr = getIntent().getStringExtra("postKey");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        databaseReference.child("posts").child(postKeyStr)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (!snapshot.exists()) {
                                    Toast.makeText(TweetActivity.this, "No post exist", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    return;
                                }

                                PostModel post = snapshot.getValue(PostModel.class);

                                currentPostModel = post;

                                setListenersOnAllViewsOfTweet(post);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error.getMessage());
                            }
                        }
                );

    }

    private void setMessageBtnClickListener() {
        findViewById(R.id.message_btn_activity_tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(TweetActivity.this, "Get your remaining money done now. I'll add this as well lol", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(TweetActivity.this, ConversationActivity.class);
                intent.putExtra("first", true);
                intent.putExtra("name", currentPostModel.getUserName());
                intent.putExtra("url", currentPostModel.getImageUrl());
                intent.putExtra("uid", currentPostModel.getPosterUid());

                startActivity(intent);

            }
        });
    }

    private void setRetweetBtnClickListener() {
        findViewById(R.id.retweet_btn_activity_tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(TweetActivity.this, "Get your remaining money done now. I'll add this as well lol", Toast.LENGTH_LONG).show();
                utils.showDialog(TweetActivity.this,
                        "Are you sure?",
                        "Do you really want to retweet this post to your timeline?",
                        "Yes",
                        "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                progressDialog.show();

                                final String key = databaseReference.child("posts").push().getKey();
                                final String uid = mAuth.getCurrentUser().getUid();

                                PostModel model1 = new PostModel();
                                model1.setUserName(utils.getStoredString(TweetActivity.this, "usernameStr"));
                                model1.setPostDate(utils.getDate());
                                model1.setImageUrl(utils.getStoredString(TweetActivity.this, "profileUrl"));
                                model1.setPostKey(key);
                                model1.setPosterUid(uid);
                                model1.setPostData(currentPostModel.getPostData());
                                model1.setLikesCount(0);

                                databaseReference.child("posts").child(key).setValue(model1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    Toast.makeText(TweetActivity.this, "Done!", Toast.LENGTH_SHORT).show();

                                                } else {

                                                    Toast.makeText(TweetActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                                progressDialog.dismiss();
                                                dialogInterface.dismiss();

                                            }
                                        });

                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }, true);

            }
        });
    }

    private void setAddReplyBtnClickListener() {
        EditText editText = findViewById(R.id.reply_edit_text_activity_tweet);

        findViewById(R.id.send_reply_btn_activity_tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(TweetActivity.this, "I added this as a compliment. Give me thanks later!", Toast.LENGTH_LONG).show();
                String replyText = editText.getText().toString();

                if (TextUtils.isEmpty(replyText)) {
                    editText.setError("Please enter a reply!");
                    editText.requestFocus();
                    return;
                }

                String name = utils.getStoredString(TweetActivity.this,
                        "usernameStr");
                String url = utils.getStoredString(TweetActivity.this, "profileUrl");

                ReplyModel model = new ReplyModel();
                model.setName(name);
                model.setProfileImageUrl(url);
                model.setReplyData(replyText);

                editText.setText("");

                databaseReference.child("posts").child(postKeyStr)
                        .child("replies")
                        .push()
                        .setValue(model)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    // Code here.

                                } else {
                                    Toast.makeText(TweetActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

            }
        });

    }

    private void setbackBtnListener() {
        findViewById(R.id.backbtn_tweet_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setListenersOnAllViewsOfTweet(PostModel post) {
        userProfileLayout.setOnClickListener(
                userProfileLayoutClickListener(post.getPosterUid()));

        userNameTextView.setText(post.getUserName());

        postDateTextView.setText(post.getPostDate());

        postDataTextView.setText(post.getPostData());

        postDataTextView.setOnClickListener(
                postDataTextViewClickListener(post.getPostKey()));

        likesCountTextView.setText(post.getLikesCount() + "");

        setUserProfileImageview(userProfileImageView, post.getImageUrl());

        setLikeButtonValueThatIfLikedOrNot(likeButton, post.getPostKey());

        setLikeButtonClickListener(
                likeButton,
                likesCountTextView,
                post.getPostKey());

        getRepliesFromDatabase();

    }

    private static class ReplyModel {

        private String name, profileImageUrl, replyData;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }

        public String getReplyData() {
            return replyData;
        }

        public void setReplyData(String replyData) {
            this.replyData = replyData;
        }

        public ReplyModel(String name, String profileImageUrl, String replyData) {
            this.name = name;
            this.profileImageUrl = profileImageUrl;
            this.replyData = replyData;
        }

        ReplyModel() {
        }
    }

    private ArrayList<ReplyModel> replyModelArrayList = new ArrayList<>();
    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void getRepliesFromDatabase() {
        databaseReference.child("posts").child(postKeyStr)
                .child("replies")
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (!snapshot.exists()) {
                                    progressDialog.dismiss();
                                    return;
                                }

                                replyModelArrayList.clear();

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                    ReplyModel model = dataSnapshot.getValue(ReplyModel.class);

                                    replyModelArrayList.add(model);

                                }

                                initRecyclerView();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error.getMessage());
                            }
                        }
                );
    }

    private void setLikeButtonClickListener(LikeButton button, TextView likesCountTextView, String postKey) {
        button.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                // SETTING VALUES ON TEXTVIEW OFFLINE
                int likesValue = Integer.parseInt(likesCountTextView.getText().toString());
                likesCountTextView.setText(likesValue + 1 + "");

                databaseReference.child("posts").child(postKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    return;
                                }

                                int count = snapshot.child("likesCount")
                                        .getValue(Integer.class);

                                databaseReference.child("posts").child(postKey)
                                        .child("likesCount")
                                        .setValue(count + 1);

                                databaseReference.child("posts").child(postKey)
                                        .child("likes")
                                        .push()
                                        .child("uid")
                                        .setValue(mAuth.getCurrentUser()
                                                .getUid());


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

            }

            @Override
            public void unLiked(LikeButton likeButton) {

                // SETTING VALUES ON TEXTVIEW OFFLINE
                int likesValue = Integer.parseInt(likesCountTextView.getText().toString());
                likesCountTextView.setText(likesValue - 1 + "");

                databaseReference.child("posts").child(postKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    return;
                                }

                                int count = snapshot.child("likesCount")
                                        .getValue(Integer.class);

                                databaseReference.child("posts").child(postKey)
                                        .child("likesCount")
                                        .setValue(count - 1);

                                databaseReference.child("posts")
                                        .child(postKey).child("likes")
                                        .orderByChild("uid")
                                        .equalTo(mAuth.getCurrentUser()
                                                .getUid())
                                        .addListenerForSingleValueEvent(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        ArrayList<String> uidPushKeys = new ArrayList<>();

                                                        if (snapshot.exists()) {

                                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                                                String key = dataSnapshot.getKey();
                                                                uidPushKeys.add(key);

                                                            }

                                                            for (String keys : uidPushKeys) {

                                                                databaseReference.child("posts")
                                                                        .child(postKey).child("likes")
                                                                        .child(keys).removeValue();

                                                            }

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.d(TAG, "onCancelled: " + error.getMessage());
                                                    }
                                                }
                                        );

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error.getMessage());
                            }
                        });

            }
        });
    }

    private void setLikeButtonValueThatIfLikedOrNot(LikeButton likeButton, String postKey) {

        databaseReference.child("posts").child(postKey).child("likes")
                .orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        likeButton.setLiked(snapshot.exists());


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + error.getMessage());
                    }
                });

    }

    private View.OnClickListener postDataTextViewClickListener(String key) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(TweetActivity.this,
//                        key, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setUserProfileImageview(CircleImageView imageview, String url) {
        Glide.with(TweetActivity.this)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(R.color.grey)
                        .error(R.color.grey)
                )
                .into(imageview);
    }

    private View.OnClickListener userProfileLayoutClickListener(String uid) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TweetActivity.this, ProfileActivity.class);
                intent.putExtra("uid", uid);

                TweetActivity.this.startActivity(intent);

//                    Toast.makeText(getActivity(),
//                            uid, Toast.LENGTH_SHORT).show();
            }
        };
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

    private void initRecyclerView() {

        conversationRecyclerView = findViewById(R.id.replies_recycler_view);
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        if (adapter.getItemCount() != 0) {

            //        noChatsLayout.setVisibility(View.GONE);
            //        chatsRecyclerView.setVisibility(View.VISIBLE);

        }
        progressDialog.dismiss();
    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_reply_tweet, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            ReplyModel replyModel = replyModelArrayList.get(position);

            //CircleImageView profileImageView;
            //            TextView nameTextView, replyDataTextView;

            holder.replyDataTextView.setText(replyModel.getReplyData());

            holder.nameTextView.setText(replyModel.getName());

            Glide.with(TweetActivity.this)
                    .load(replyModel.getProfileImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.grey)
                            .error(R.color.grey)
                    )
                    .into(holder.profileImageView);

        }

        @Override
        public int getItemCount() {
            if (replyModelArrayList == null)
                return 10;
            return replyModelArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            CircleImageView profileImageView;
            TextView nameTextView, replyDataTextView;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                profileImageView = v.findViewById(R.id.user_profile_imageview_reply_tweet_layout);
                nameTextView = v.findViewById(R.id.user_name_reply_tweet_layout);
                replyDataTextView = v.findViewById(R.id.reply_data_text_view_tweet_layout);

            }
        }

    }

}