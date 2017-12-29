package com.android.redcarpet.auth.email;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;

public class EmailPasswordActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String TAG = EmailPasswordActivity.class.getSimpleName();

    private EditText mEmailField;
    private ProgressDialogHolder mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password);

        mEmailField = findViewById(R.id.password_email_edit);
        mEmailField.setOnClickListener(this);
        mEmailField.setOnEditorActionListener(this);

        String mEmail = getIntent().getStringExtra("email");
        if (!TextUtils.isEmpty(mEmail)) {
            mEmailField.setText(mEmail);
        }

        findViewById(R.id.password_email_send_bt).setOnClickListener(this);

        mProgress = new ProgressDialogHolder(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.password_email_send_bt:
                validateAndSend();
                return;
            case R.id.password_email_edit:
                mEmailField.setError(null);
        }
    }

    private void validateAndSend() {
        Log.i(TAG, "validateAndSend: ");
        mEmailField.setError(null);
        String email = mEmailField.getText().toString();

        if (Validator.isEmailValid(mEmailField, email)) {
            mProgress.showLoadingDialog("Sending...");
            resetPassword(email);
        } else {
            mEmailField.requestFocus();
        }
    }

    private void resetPassword(String email) {
        Log.i(TAG, "resetPassword: ");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgress.dismissDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                        mEmailField.setText(e.getLocalizedMessage());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: ");
                        Toast.makeText(EmailPasswordActivity.this, "Please check your inbox", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }

    @Override
    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
            validateAndSend();
            return true;
        }
        return false;
    }
}
