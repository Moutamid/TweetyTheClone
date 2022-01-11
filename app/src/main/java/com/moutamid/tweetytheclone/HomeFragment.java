package com.moutamid.tweetytheclone;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private View rootView;
    private ArrayList<PostModel> postsArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private Utils utils = new Utils();

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private LottieAnimationView animationView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rootView.findViewById(R.id.add_post_button_fragment_home)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().startActivity(new Intent(
                                getActivity(), AddPostActivity.class
                        ));
                    }
                });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        animationView = rootView.findViewById(R.id.progress_bar_fragment_home);

        LottieAnimationView animationView1 = rootView.findViewById(R.id.error_layout_fragment_home);
        conversationRecyclerView = rootView.findViewById(R.id.home_recycler_view);

        databaseReference.child("posts").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        postsArrayList.clear();

                        if (!snapshot.exists())
                            return;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            PostModel model = dataSnapshot.getValue(PostModel.class);

                            postsArrayList.add(model);

                        }

//                        if (utils.getStoredString(getActivity(), "current_fragment").equals("home"))
                        initRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        animationView.setVisibility(View.GONE);
                        animationView.pauseAnimation();

                        animationView1.playAnimation();
                        animationView1.setVisibility(View.VISIBLE);

                        Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });

        databaseReference.child("reports")
                .child("uid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isCancellable = true;
                        String msg = "You have been reported once. Delete any appropriate content before you receive more reports and then eventually your account will be suspended!";
                        if (snapshot.exists()) {
                            if (snapshot.getChildrenCount() > 3) {
                                //BANNED
                                msg = "Your account have been suspended due to receiving many reports on your content!";
                                isCancellable = false;
                            }

                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Alert")
                                    .setCancelable(isCancellable)
                                    .setMessage(msg)
                                    .show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        return rootView;
    }

    private void testData() {
        //        model.setImageUrl("https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?ixid=MnwxMjA3fDB8MHxzZWFyY2h8MzZ8fHByb2ZpbGUlMjBwaWN0dXJlfGVufDB8fDB8fA%3D%3D&ixlib=rb-1.2.1&w=1000&q=80");


//        ArrayList<String> list = new ArrayList<>();
//        list.add("hjhgjhgjh");
//        list.add("hjhgjwrewhgdjh");
//        list.add("hjhgjhg213djh");
//        list.add("hjhgjhgdj123h");
//        list.add("hjhgj34hgweresjh");
//        list.add("hjhgjhs3gjh");
//        list.add("hjhgjhwewergjh");
//        list.add("hjqwwqwhgjhwewergjh");
//        list.add("hjqwwqwasashgjhwewergjh");
//        list.add("hjqwwqwasashgjhwewergjh");
//        list.add("hjqwwqwasashgjhwewergjh");
//        list.add("hjqwwqwasashgjhwewergjh");
//        list.add("hjqwwqwasashgjhwewergjh");
//
//
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
////      -----------------------------------------------------------------  databaseReference.child("test").setValue(list);
//
//        databaseReference.child("test").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                ArrayList<String> list = (ArrayList<String>) snapshot.getValue();
//
//                Toast.makeText(getActivity(), list.toString(), Toast.LENGTH_SHORT).show();
//
////              for (DataSnapshot snapshot1:snapshot.getChildren()){
////
////              }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

//    private void onStarClicked() {
//        databaseReference.child("posts").child("key")
//                .runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                mutableData.child("");
//
//
//                ArrayList<String> p = mutableData.getValue(ArrayList.class);
//
//                if (p == null) {
//                    return Transaction.success(mutableData);
//                }
//
//                if (p.stars.containsKey(getUid())) {
//                    // Unstar the post and remove self from stars
//                    p.starCount = p.starCount - 1;
//                    p.stars.remove(getUid());
//                } else {
//                    // Star the post and add self to stars
//                    p.starCount = p.starCount + 1;
//                    p.stars.put(getUid(), true);
//                }
//
//                // Set value and report transaction success
//                mutableData.setValue(p);
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(DatabaseError databaseError, boolean committed,
//                                   DataSnapshot currentData) {
//                // Transaction completed
//                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
//            }
//        });
//    }

    private void initRecyclerView() {

        adapter = new RecyclerViewAdapterMessages();
//                LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);

        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        if (adapter.getItemCount() != 0) {

//            LottieAnimationView animationView = getActivity().findViewById(R.id.progress_bar_fragment_home);

            if (animationView.getVisibility() != View.GONE) {
                animationView.setVisibility(View.GONE);
                animationView.pauseAnimation();
            }

            LinearLayoutManager layoutManager = (LinearLayoutManager) conversationRecyclerView
                    .getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }

//            ScrollView scrollView = getActivity().findViewById(R.id.scrollView_fragment_home);
//            scrollView.fullScroll(View.FOCUS_UP);

            //        noChatsLayout.setVisibility(View.GONE);
            //        chatsRecyclerView.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.notifyDataSetChanged();
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

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tweet_home, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {

            PostModel post = postsArrayList.get(position);

            holder.userProfileLayout.setOnClickListener(
                    userProfileLayoutClickListener(post.getPosterUid()));

            holder.userNameTextView.setText(post.getUserName());

            holder.postDateTextView.setText(post.getPostDate());

            holder.postDataTextView.setText(post.getPostData());

            holder.postDataTextView.setOnClickListener(
                    postDataTextViewClickListener(post.getPostKey()));

            holder.likesCountTextView.setText(post.getLikesCount() + "");

            setUserProfileImageview(holder.userProfileImageView, post.getImageUrl());

            setLikeButtonValueThatIfLikedOrNot(holder.likeButton, post.getPostKey());

            setLikeButtonClickListener(
                    holder.likeButton,
                    holder.likesCountTextView,
                    post.getPostKey());

            holder.menuBtn.setOnClickListener(menuBtnCLickListener(post));

        }

        private View.OnClickListener menuBtnCLickListener(PostModel post) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    final CharSequence[] items = {"Report User"};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {

                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Are you sure?")
                                    .setMessage("Do you really want to report this user?")
                                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                                        ProgressDialog progressDialog;
                                        progressDialog = new ProgressDialog(requireContext());
                                        progressDialog.setCancelable(false);
                                        progressDialog.setMessage("Loading...");
                                        progressDialog.show();

                                        /*HashMap<String, String> map = new HashMap<>();
                                        map.put("uid", post.getPosterUid());
                                        map.put("content", post.getPostData());*/
                                        databaseReference.child("reports")
                                                .child("uid")
                                                .push()
                                                .setValue(true).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show();
                                            }

                                            progressDialog.dismiss();
                                        });
                                    })
                                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .show();

                        }
                    });

                    dialog = builder.create();
                    dialog.show();
                }
            };
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
                                            .setValue(firebaseAuth.getCurrentUser()
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
                                            .equalTo(firebaseAuth.getCurrentUser()
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
                    .orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
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

                    Intent intent = new Intent(getActivity(), TweetActivity.class);
                    intent.putExtra("postKey", key);
                    getActivity().startActivity(intent);

//                    Toast.makeText(getActivity(),
//                            key, Toast.LENGTH_SHORT).show();
                }
            };
        }

        private void setUserProfileImageview(CircleImageView imageview, String url) {
            Glide.with(getActivity())
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

                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra("uid", uid);

                    getActivity().startActivity(intent);

//                    Toast.makeText(getActivity(),
//                            uid, Toast.LENGTH_SHORT).show();
                }
            };
        }

        @Override
        public int getItemCount() {
            if (postsArrayList == null)
                return 0;
            return postsArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            RelativeLayout userProfileLayout;
            TextView userNameTextView, postDateTextView, postDataTextView, likesCountTextView;
            CircleImageView userProfileImageView;
            LikeButton likeButton;
            ImageView menuBtn;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                userProfileLayout = v.findViewById(R.id.user_profile_layout_post_tweet_layout);
                menuBtn = v.findViewById(R.id.menutBtn);
                userNameTextView = v.findViewById(R.id.user_name_post_tweet_layout);
                postDateTextView = v.findViewById(R.id.user_date_post_tweet_layout);
                userProfileImageView = v.findViewById(R.id.user_profile_imageview_post_tweet_layout);
                postDataTextView = v.findViewById(R.id.post_data_text_view_tweet_layout);
                likesCountTextView = v.findViewById(R.id.likes_count_text_view_post_tweet_layout);
                likeButton = v.findViewById(R.id.like_button_post_tweet_layout);

            }
        }

    }

}