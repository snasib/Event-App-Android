package com.android.redcarpet.auth.email;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.redcarpet.R;

import java.util.List;

public class EmailActivity extends AppCompatActivity
        implements EmailCheckFragment.CheckEmailListener, EmailRegisterFragment.OnRegisterFragmentListener {

    private static final String TAG = EmailActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        //Start with check email
        EmailCheckFragment fragment = new EmailCheckFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_login_email, fragment, EmailCheckFragment.TAG)
                .disallowAddToBackStack()
                .commit();
    }

    @Override
    public void onExistingEmailUser(String email) {
        Log.i(TAG, "onExistingEmailUser: ");
        Intent intent = new Intent(EmailActivity.this, EmailUserActivity.class);
        intent.putExtra("email", email);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onExistingIdpUser(List<String> providers) {
        Log.i(TAG, "onExistingIdpUser: ");

    }

    @Override
    public void onNewUser(String email) {
        Log.i(TAG, "onNewUser: ");
        EmailRegisterFragment fragment = EmailRegisterFragment.newInstance(email);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_login_email, fragment, EmailRegisterFragment.TAG)
                .disallowAddToBackStack()
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public void onRegisterComplete(int resultCode) {
        if (resultCode == EmailRegisterFragment.RESULT_OK) {
            finish();
        }
    }
}
