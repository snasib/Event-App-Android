package com.android.redcarpet.auth.facebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.android.redcarpet.data.FirebaseHelper;
import com.android.redcarpet.data.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class FacebookFragment extends Fragment implements FacebookCallback<LoginResult> {
    public static final String TAG = FacebookFragment.class.getSimpleName();
    public static final int RESULT_OK = 1;
    public static final int RESULT_CANCEL = 0;
    public static final int RESULT_FAIL = -1;
    private static final String EMAIL = "email";
    private static final String PUBLIC_PROFILE = "public_profile";

    private OnFacebookInteractionListener mListener;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;

    public FacebookFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.registerCallback(mCallbackManager, this);

        ArrayList<String> permissionList = new ArrayList<>();
        permissionList.add(EMAIL);
        permissionList.add(PUBLIC_PROFILE);

        loginManager.logInWithReadPermissions(this, permissionList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCallbackManager != null) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFacebookInteractionListener) {
            mListener = (OnFacebookInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        Log.d(TAG, "facebook:onSuccess:" + loginResult);
        handleFacebookAccessToken(loginResult.getAccessToken());
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            FirebaseHelper.writeNewUser(currentUser, new User.Builder(currentUser).build());
                        }
                        mListener.onFacebookSignInComplete(RESULT_OK);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "signInWithCredential:failure" + e.getLocalizedMessage());
                        mListener.onFacebookSignInComplete(RESULT_FAIL);
                    }
                });
    }

    @Override
    public void onCancel() {
        Log.i(TAG, "onCancel: ");
        mListener.onFacebookSignInComplete(RESULT_CANCEL);
    }

    @Override
    public void onError(FacebookException error) {
        Log.e(TAG, "onError: ", error);
    }

    public interface OnFacebookInteractionListener {
        void onFacebookSignInComplete(int resultCode);
    }
}
