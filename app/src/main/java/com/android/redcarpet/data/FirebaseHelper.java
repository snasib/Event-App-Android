package com.android.redcarpet.data;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class FirebaseHelper {
    private final static String TAG = FirebaseHelper.class.getSimpleName();
    private final static String USERS = "users";

    private FirebaseHelper() {
    }

    public static void writeNewUser(FirebaseUser currentUser, User user) {
        Log.i(TAG, "writeNewUser: " + user.toString());
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(USERS);
        rootRef.child(currentUser.getUid()).setValue(user);
    }
}
