package com.android.redcarpet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.redcarpet.auth.email.EmailActivity;
import com.android.redcarpet.auth.facebook.FacebookFragment;
import com.android.redcarpet.auth.google.GoogleFragment;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleFragment.OnGoogleFragmentListener, FacebookFragment.OnFacebookInteractionListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.bt_idp_email).setOnClickListener(this);
        findViewById(R.id.bt_idp_google).setOnClickListener(this);
        findViewById(R.id.bt_idp_facebook).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_idp_email:
                createIntent(EmailActivity.class);
                return;
            case R.id.bt_idp_google:
                signInWithGoogle();
                return;
            case R.id.bt_idp_facebook:
                signInWithFacebook();
        }
    }

    private void signInWithFacebook() {
        Log.i(TAG, "signInWithFacebook: ");
        FacebookFragment fragment = new FacebookFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(fragment, FacebookFragment.TAG);
        transaction.commit();
    }

    private void signInWithGoogle() {
        Log.i(TAG, "signInWithGoogle: ");
        GoogleFragment fragment = new GoogleFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(fragment, GoogleFragment.TAG);
        transaction.commit();
    }

    private void createIntent(Class<?> targetClass) {
        Log.i(TAG, "createIntent: ");
        startActivity(new Intent(LoginActivity.this, targetClass));
    }

    @Override
    public void onGoogleSignInComplete(int resultCode) {
        Log.i(TAG, "onGoogleSignInComplete: " + resultCode);
        if (resultCode == GoogleFragment.RESULT_OK) {
            Toast.makeText(this, R.string.google_sign_in_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFacebookSignInComplete(int resultCode) {
        Log.i(TAG, "onFacebookSignInComplete: ");
        switch (resultCode) {
            case FacebookFragment.RESULT_OK:
                Toast.makeText(this, R.string.fb_logged_in, Toast.LENGTH_SHORT).show();
                finish();
                return;
            case FacebookFragment.RESULT_FAIL:
                Toast.makeText(this, R.string.fb_failed, Toast.LENGTH_SHORT).show();
                return;
            case FacebookFragment.RESULT_CANCEL:
                Toast.makeText(this, R.string.fb_cancelled, Toast.LENGTH_SHORT).show();
        }
    }
}
