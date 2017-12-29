package com.android.redcarpet.auth.email;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.redcarpet.R;
import com.android.redcarpet.util.ProgressDialogHolder;
import com.android.redcarpet.util.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailUserActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String TAG = EmailUserActivity.class.getSimpleName();

    private String mEmail;
    private EditText mPasswordField;

    private ProgressDialogHolder mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_user);

        mPasswordField = findViewById(R.id.email_user_password_edit);
        mPasswordField.setOnClickListener(this);
        mPasswordField.setOnEditorActionListener(this);

        // Click listeners
        findViewById(R.id.email_user_bt_done).setOnClickListener(this);
        findViewById(R.id.trouble_signing_in).setOnClickListener(this);

        mProgressDialog = new ProgressDialogHolder(this);

        mEmail = getIntent().getStringExtra("email");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.email_user_bt_done:
                validateAndSignIn();
                return;
            case R.id.trouble_signing_in:
                Intent intent = new Intent(EmailUserActivity.this, EmailPasswordActivity.class);
                intent.putExtra("email", mEmail);
                startActivity(intent);
                return;
            case R.id.email_user_password_edit:
                mPasswordField.setError(null);
        }
    }

    private void validateAndSignIn() {
        Log.i(TAG, "validateAndSignIn: ");
        mPasswordField.setError(null);
        String password = mPasswordField.getText().toString();

        if (Validator.isPasswordValid(mPasswordField, password)) {
            mProgressDialog.showLoadingDialog(R.string.progress_signing_in);
            signIn(mEmail, password);
        } else {
            mPasswordField.requestFocus();
        }
    }

    private void signIn(String email, String password) {
        Log.i(TAG, "signIn: ");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i(TAG, "onSuccess: ");
                        Toast.makeText(EmailUserActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ");
                        String error = e.getLocalizedMessage();
                        mPasswordField.setError(error);
                        setResult(RESULT_CANCELED);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressDialog.dismissDialog();
                    }
                });
    }

    @Override
    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
            validateAndSignIn();
            return true;
        }
        return false;
    }
}
