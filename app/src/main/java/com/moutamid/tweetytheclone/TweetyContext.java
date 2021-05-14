package com.moutamid.tweetytheclone;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class TweetyContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //--ADMIN IS FALSE OTHERWISE TRUE

        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
    }

}
