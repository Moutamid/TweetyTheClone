package com.moutamid.tweetytheclone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.deleteDataBtn).setOnClickListener(view -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setMessage("Note: You are about to email the developer to request the deletion of your data. This may take few days to complete. You'll receive an email when it's done!")
                    .setPositiveButton("OK", (dialogInterface, i) -> sendEmail())
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();

        });

        findViewById(R.id.privacyPolicyBtn).setOnClickListener(view -> {
            String url = "https://earnreal.github.io/home/privacy-policy.html";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"lumianokia188@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "DATA DELETION REQUEST!");
        intent.putExtra(Intent.EXTRA_TEXT, "Hey!\n\nPlease delete all of my data from your app and servers including my images, posts and messages." +
                "\n\nMy UID is: " + mAuth.getUid() +
                "\n\n Tweety - the Socializer");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}