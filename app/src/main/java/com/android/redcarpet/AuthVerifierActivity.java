package com.android.redcarpet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class AuthVerifierActivity extends AppCompatActivity {
    private static final String TAG = AuthVerifierActivity.class.getSimpleName();

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(AuthVerifierActivity.this, LoginActivity.class));
        } else {
            startActivity(new Intent(AuthVerifierActivity.this, MainActivity.class));
        }
    }
}
