package com.android.redcarpet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.redcarpet.auth.email.EmailActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = LoginActivity.class.getSimpleName();

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
                return;
            case R.id.bt_idp_facebook:
        }
    }

    private void createIntent(Class<?> targetClass) {
        startActivity(new Intent(LoginActivity.this, targetClass));
    }
}
