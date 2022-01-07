package com.moutamid.tweetytheclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private View rootView;

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

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        emailTextView = rootView.findViewById(R.id.email_text_view_fragment_profile);
        nameTextView = rootView.findViewById(R.id.user_name_fragment_profile);
        profileImageView = rootView.findViewById(R.id.profile_image_view_fragment_profile);
        progressBarImageView = rootView.findViewById(R.id.progress_bar_image_view_fragment_profile);
        progressBar = rootView.findViewById(R.id.progress_bar_fragment_profile);
        progressBarImageView.pauseAnimation();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        getUserDetails();

        profileImageView.setOnClickListener(profileImageViewClickListener());

        getUserPosts();

        rootView.findViewById(R.id.logoutBtn).setOnClickListener(view -> {
            utils.removeSharedPref(requireContext());
            mAuth.signOut();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            requireActivity().finish();
            startActivity(intent);
        });

        rootView.findViewById(R.id.settingsBtn).setOnClickListener(view -> {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });

        return rootView;
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
                .equalTo(mAuth.getCurrentUser().getUid())
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
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);
                        progressBar.pauseAnimation();
                    }
                });
    }

    private View.OnClickListener profileImageViewClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                utils.showDialog(getActivity(),
                        "Hi",
                        "Do you want to change your profile image?",
                        "Yes",
                        "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                galleryIntent.setType("image/*");
                                startActivityForResult(galleryIntent, GALLERY_REQUEST);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }, true);

            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            //imageUri = data.getData();
            Uri imageUri = data.getData();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages");

            progressBarImageView.playAnimation();
            progressBarImageView.setVisibility(View.VISIBLE);
//            progressDialog.show();

            final StorageReference filePath = storageReference
                    .child(mAuth.getCurrentUser().getUid() + imageUri.getLastPathSegment());
//            final StorageReference filePath = storageReference.child("sliders")
//                    .child(imageUri.getLastPathSegment());

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri photoUrl) {

//                            TextView othertxt = findViewById(R.id.othertextregistration);
//                            othertxt.setText(photoUrl.toString());

                            profileImageUrl = photoUrl.toString();

                            databaseReference.child("users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .child("profileUrl")
                                    .setValue(profileImageUrl)
                                    .addOnCompleteListener(
                                            new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        utils.storeString(
                                                                getActivity(),
                                                                "profileUrl",
                                                                profileImageUrl
                                                        );

                                                        profileImageView.setImageURI(data.getData());

                                                        progressBarImageView.setVisibility(View.GONE);
                                                        progressBarImageView.pauseAnimation();

                                                        Toast.makeText(getActivity(), "Upload done!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        progressBarImageView.pauseAnimation();
                                                        progressBarImageView.setVisibility(View.GONE);

                                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });

                        }
                    });
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressBarImageView.pauseAnimation();
                    progressBarImageView.setVisibility(View.GONE);

                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    private void getUserDetails() {
        nameTextView.setText(
                utils.getStoredString(getActivity(), "usernameStr")
        );

        emailTextView.setText(mAuth.getCurrentUser().getEmail());

        Glide.with(getActivity())
                .load(utils.getStoredString(getActivity(), "profileUrl"))
                .apply(new RequestOptions()
                        .placeholder(R.color.grey)
                        .error(R.color.grey)
                )
                .into(profileImageView);
    }

    private void initRecyclerView() {

        conversationRecyclerView = rootView.findViewById(R.id.profile_recycler_view);
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
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
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tweet_profile, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            PostModel post = postsArraylist.get(position);

            holder.postData.setText(post.getPostData());

            holder.postData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getActivity(), TweetActivity.class);
                    intent.putExtra("postKey", post.getPostKey());
                    getActivity().startActivity(intent);

//                    Toast.makeText(getActivity(), post.getPostKey(), Toast.LENGTH_SHORT).show();
                }
            });

            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    utils.showDialog(getActivity(),
                            "Are you sure?",
                            "Do you really want to delete this post?",
                            "Yes",
                            "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    postsArraylist.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, getItemCount());

                                    databaseReference.child("posts")
                                            .child(post.getPostKey())
                                            .removeValue();

                                    Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();

                                    dialogInterface.dismiss();
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