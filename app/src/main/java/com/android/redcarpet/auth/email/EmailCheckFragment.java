package com.android.redcarpet.auth.email;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.redcarpet.R;
import com.android.redcarpet.util.ProgressDialogHolder;
import com.android.redcarpet.util.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.List;
import java.util.Objects;

public class EmailCheckFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

    public static final String TAG = EmailCheckFragment.class.getSimpleName();

    private EditText mEmailEdit;
    private ProgressDialogHolder mProgressDialog;
    private CheckEmailListener mListener;

    public EmailCheckFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_check, container, false);

        mEmailEdit = view.findViewById(R.id.check_email_edit);
        mEmailEdit.setOnClickListener(this);
        mEmailEdit.setOnEditorActionListener(this);

        view.findViewById(R.id.check_email_next_bt).setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mProgressDialog = new ProgressDialogHolder(getContext());
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof CheckEmailListener) {
            mListener = (CheckEmailListener) getActivity();
        } else {
            throw new RuntimeException(Objects.requireNonNull(getActivity()).toString()
                    + " must implement CheckEmailListener");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.check_email_next_bt) {
            validateAndProceed();
        } else if (id == R.id.check_email_layout
                || id == R.id.check_email_edit) {
            mEmailEdit.setError(null);
        }
    }

    private void validateAndProceed() {
        Log.i(TAG, "validateAndProceed: ");
        mEmailEdit.setError(null);
        String email = mEmailEdit.getText().toString();

        if (Validator.isEmailValid(mEmailEdit, email)) {
            mProgressDialog.showLoadingDialog(R.string.check_existing_accounts);
            checkAccountExists(email);
        } else {
            mEmailEdit.requestFocus();
        }
    }

    private void checkAccountExists(final String email) {
        Log.i(TAG, "checkAccountExists: ");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.fetchProvidersForEmail(email)
                .addOnSuccessListener(new OnSuccessListener<ProviderQueryResult>() {
                    @Override
                    public void onSuccess(ProviderQueryResult provider) {
                        if (provider == null || String.valueOf(provider.getProviders()).equals("[]")) {
                            Log.i(TAG, "onSuccess: No such user ");
                            mListener.onNewUser(email);
                        } else if (String.valueOf(provider.getProviders()).equals("[password]")) {
                            Log.i(TAG, "onSuccess: User with email");
                            mListener.onExistingEmailUser(email);
                        } else {
                            Log.i(TAG, "onSuccess: User with Idp");
                            mListener.onExistingIdpUser(provider.getProviders());
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                        mProgressDialog.dismissDialog();
                    }
                });
    }

    @Override
    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
            validateAndProceed();
            return true;
        }
        return false;
    }

    interface CheckEmailListener {
        void onExistingEmailUser(String email);

        void onExistingIdpUser(List<String> providers);

        void onNewUser(String email);
    }
}
