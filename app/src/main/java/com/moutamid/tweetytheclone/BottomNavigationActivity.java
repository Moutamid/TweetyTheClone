package com.moutamid.tweetytheclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class BottomNavigationActivity extends AppCompatActivity {
    private static final String TAG = "BottomNavigationActivit";

    private RelativeLayout profileTabBtn, homeTabBtn, messageTabBtn;
    private View homeViewLine, profileViewLine, messageViewLine;

    private RelativeLayout currentLayout;
    private View currentViewLine;

    private Utils utils = new Utils();

//    private HomeFragment homeFragment;
//    private ProfileFragment profileFragment;
//    private ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            Intent intent = new Intent(BottomNavigationActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
            return;
        }

        profileTabBtn = findViewById(R.id.profile_tab_button_bottom_navigation);
        homeTabBtn = findViewById(R.id.home_tab_button_bottom_navigation);
        messageTabBtn = findViewById(R.id.message_tab_button_bottom_navigation);
        currentLayout = homeTabBtn;

        homeViewLine = findViewById(R.id.secline);
        profileViewLine = findViewById(R.id.firstline);
        messageViewLine = findViewById(R.id.thirdline);
        currentViewLine = homeViewLine;

        profileTabBtn.setOnClickListener(profileTabBtnClickListener());
        homeTabBtn.setOnClickListener(homeTabBtnClickListener());
        messageTabBtn.setOnClickListener(messageTabBtnClickListener());

        utils.storeString(BottomNavigationActivity.this, "current_fragment", "home");

        loadFragment(new HomeFragment());

//        if (new Utils().getStoredString(
//                BottomNavigationActivity.this, "usernameStr")
//                .equals("Error")
//                || new Utils().getStoredString(
//                BottomNavigationActivity.this, "profileUrl")
//                .equals("Error")
//
//        ) {
        getUserdetails();
//        }

        listenForProfileImageChanges();
    }

    private void listenForProfileImageChanges() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid())
                .child("profileUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String url = snapshot.getValue(String.class);
                CircleImageView circleImageView = findViewById(R.id.profile_image_view_bottom_navigation);
                Glide.with(BottomNavigationActivity.this)
                        .load(url)
                        .apply(new RequestOptions()
                                .placeholder(R.color.grey)
                                .error(R.color.grey)
                        )
                        .into(circleImageView);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error.getMessage());
            }
        });

    }

    private void getUserdetails() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            return;
                        }

                        new Utils().storeString(BottomNavigationActivity.this,
                                "usernameStr", snapshot.child("name").getValue(String.class));

                        new Utils().storeString(BottomNavigationActivity.this,
                                "profileUrl", snapshot.child("profileUrl").getValue(String.class));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BottomNavigationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private View.OnClickListener messageTabBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLayout.setBackgroundColor(0);
                currentViewLine.setVisibility(View.INVISIBLE);

                currentLayout = messageTabBtn;
                currentViewLine = messageViewLine;

                currentLayout.setBackgroundColor(getResources().getColor(R.color.tabbtnsbg));
                currentViewLine.setVisibility(View.VISIBLE);

                utils.storeString(BottomNavigationActivity.this, "current_fragment", "message");

                loadFragment(new ChatFragment());
            }
        };
    }

    private View.OnClickListener homeTabBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLayout.setBackgroundColor(0);
                currentViewLine.setVisibility(View.INVISIBLE);

                currentLayout = homeTabBtn;
                currentViewLine = homeViewLine;

                currentLayout.setBackgroundColor(getResources().getColor(R.color.tabbtnsbg));
                currentViewLine.setVisibility(View.VISIBLE);

                utils.storeString(BottomNavigationActivity.this, "current_fragment", "home");

                loadFragment(new HomeFragment());
            }
        };
    }

    private View.OnClickListener profileTabBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentLayout.setBackgroundColor(0);
                currentViewLine.setVisibility(View.INVISIBLE);

                currentLayout = profileTabBtn;
                currentViewLine = profileViewLine;

                currentLayout.setBackgroundColor(getResources().getColor(R.color.tabbtnsbg));
                currentViewLine.setVisibility(View.VISIBLE);

                utils.storeString(BottomNavigationActivity.this, "current_fragment", "profile");

                loadFragment(new ProfileFragment());

            }
        };
    }

    private void loadFragment(Fragment fragment) {

        if (fragment != null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

        } else {
            Toast.makeText(this, "Fragment is null!", Toast.LENGTH_SHORT).show();
        }

    }

}