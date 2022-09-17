package com.moutamid.tweetytheclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPostActivity extends AppCompatActivity {

    private Utils utils = new Utils();
    private AppCompatLineEditText editText;
    private LottieAnimationView progressBar;
    private AppCompatButton tweetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        editText = findViewById(R.id.tweet_data_edit_text_add_post);
        progressBar = findViewById(R.id.progress_bar_add_post);
        tweetButton = findViewById(R.id.tweet_btn_add_post);

        progressBar.pauseAnimation();

        setTextOnEdittext();

        findViewById(R.id.backbtn_addPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editText.getText() != null
                        && !editText.getText().toString().equals("")
                        && !TextUtils.isEmpty(editText.getText().toString())
                ) {

                    String data = editText.getText().toString();

                    addPostToDatabase(data);

//                    Toast.makeText(AddPostActivity.this, data, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(AddPostActivity.this, "Your thought is empty!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTextOnEdittext() {

        if (!utils.getStoredString(AddPostActivity.this, "postData").equals("Error"))
            editText.setText(utils.getStoredString(AddPostActivity.this, "postData"));

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                utils.storeString(AddPostActivity.this, "postData", charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean isUploading = false;

    private void addPostToDatabase(String data) {
        isUploading = true;

        progressBar.playAnimation();
        progressBar.setVisibility(View.VISIBLE);

        tweetButton.setEnabled(false);
        tweetButton.setBackgroundResource(R.drawable.bg_tweet_btn_grey_add_post);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final String key = databaseReference.child("posts").push().getKey();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();

        PostModel model = new PostModel();
        model.setUserName(utils.getStoredString(AddPostActivity.this, "usernameStr"));
        model.setPostDate(utils.getDate());
        model.setImageUrl(utils.getStoredString(AddPostActivity.this, "profileUrl"));
        model.setPostKey(key);
        model.setPosterUid(uid);
        model.setPostData(data);
        model.setLikesCount(0);

        databaseReference.child("posts").child(key).setValue(model)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

//                            databaseReference.child("users")
//                                    .child(uid).child("posts")
//                                    .push()
//                                    .setValue(key)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {

                                                clearEdittextData();
                                                isUploading = false;
//
//                                            } else {
//                                                isUploading = false;
//
//                                                progressBar.setVisibility(View.GONE);
//                                                progressBar.pauseAnimation();
//
//                                                tweetButton.setEnabled(true);
//                                                tweetButton.setBackgroundResource(R.drawable.bg_tweet_btn_add_post);
//
//                                                Toast.makeText(AddPostActivity.this,
//                                                        task.getException().getMessage(),
//                                                        Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });

                        } else {
                            isUploading = false;

                            progressBar.setVisibility(View.GONE);
                            progressBar.pauseAnimation();

                            tweetButton.setEnabled(true);
                            tweetButton.setBackgroundResource(R.drawable.bg_tweet_btn_add_post);

                            Toast.makeText(AddPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    private void clearEdittextData() {
        utils.storeString(AddPostActivity.this, "postData", "Error");
        editText.setText("");
        Toast.makeText(this, "Your thought is uploaded!", Toast.LENGTH_SHORT).show();
        finish();
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

    @Override
    public void onBackPressed() {
        if (!isUploading)
            super.onBackPressed();
    }
}