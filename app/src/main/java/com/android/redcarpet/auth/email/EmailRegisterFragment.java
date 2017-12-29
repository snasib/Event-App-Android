package com.android.redcarpet.auth.email;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Objects;

public class EmailRegisterFragment extends android.app.Fragment implements View.OnFocusChangeListener, View.OnClickListener {
    public static final String TAG = EmailRegisterFragment.class.getSimpleName();

    public static final int RESULT_OK = 1;
    public static final int RESULT_FAIL = -1;

    private String mEmail;

    private EditText mEmailField;
    private EditText mNameField;
    private EditText mSurnameField;
    private EditText mPasswordField;

    private ProgressDialogHolder mProgress;

    private OnRegisterFragmentListener mListener;

    public EmailRegisterFragment() {
        // Required empty public constructor
    }

    public static EmailRegisterFragment newInstance(String email) {
        EmailRegisterFragment fragment = new EmailRegisterFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEmail = getArguments().getString("email");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_register, container, false);

        mEmailField = view.findViewById(R.id.register_email_edit);
        mNameField = view.findViewById(R.id.register_name_edit);
        mSurnameField = view.findViewById(R.id.register_surname_edit);
        mPasswordField = view.findViewById(R.id.register_password_edit);

        mEmailField.setOnFocusChangeListener(this);
        mNameField.setOnFocusChangeListener(this);
        mSurnameField.setOnFocusChangeListener(this);
        mPasswordField.setOnFocusChangeListener(this);

        if (!TextUtils.isEmpty(mEmail)) {
            mEmailField.setText(mEmail);
            mNameField.requestFocus();
        }

        mPasswordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    validateAndRegisterUser();
                    return true;
                }
                return false;
            }
        });

        mProgress = new ProgressDialogHolder(getActivity());

        view.findViewById(R.id.register_create_bt).setOnClickListener(this);

        return view;
    }

    private void validateAndRegisterUser() {
        Log.i(TAG, "validateAndRegisterUser: ");

        mEmailField.setError(null);
        mNameField.setError(null);
        mSurnameField.setError(null);
        mPasswordField.setError(null);

        String email = mEmailField.getText().toString();
        String name = mNameField.getText().toString();
        String surname = mSurnameField.getText().toString();
        String password = mPasswordField.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!Validator.isEmailValid(mEmailField, email)) {
            cancel = true;
            focusView = mEmailField;
        }
        if (!Validator.isRequiredFieldValid(mNameField, name)) {
            cancel = true;
            focusView = mNameField;
        }
        if (!Validator.isRequiredFieldValid(mSurnameField, surname)) {
            cancel = true;
            focusView = mSurnameField;
        }
        if (!Validator.isPasswordValid(mPasswordField, password)) {
            cancel = true;
            focusView = mPasswordField;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mProgress.showLoadingDialog(R.string.registering);
            registerUser(email, password, name, surname);
        }
    }

    private void registerUser(String email, String password, String name, String surname) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(Objects.requireNonNull(getActivity()), new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(getActivity(), "User created", Toast.LENGTH_SHORT).show();
                        mListener.onRegisterComplete(RESULT_OK);
                        Objects.requireNonNull(getActivity()).finish();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mListener.onRegisterComplete(RESULT_FAIL);
                        if (e instanceof FirebaseAuthWeakPasswordException) {
                            mPasswordField.setError(getString(R.string.password_is_weak));
                            mPasswordField.requestFocus();
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            mEmailField.setError(getString(R.string.error_invalid_email));
                            mEmailField.requestFocus();
                        } else if (e instanceof FirebaseAuthUserCollisionException) {
                            mEmailField.setError(getString(R.string.user_with_email_already_exist));
                            mEmailField.requestFocus();
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgress.dismissDialog();
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterFragmentListener) {
            mListener = (OnRegisterFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRegisterFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            EditText text = (EditText) view;
            text.setError(null);
            return;
        }
        int id = view.getId();
        switch (id) {
            case R.id.register_email_edit:
                Validator.isEmailValid(mEmailField, mEmailField.getText().toString());
                return;
            case R.id.register_name_edit:
                Validator.isRequiredFieldValid(mNameField, mNameField.getText().toString());
                return;
            case R.id.register_surname_edit:
                Validator.isRequiredFieldValid(mSurnameField, mSurnameField.getText().toString());
                return;
            case R.id.register_password_edit:
                Validator.isPasswordValid(mPasswordField, mPasswordField.getText().toString());
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.register_create_bt) {
            validateAndRegisterUser();
        }
    }

    public interface OnRegisterFragmentListener {
        void onRegisterComplete(int resultCode);
    }
}
