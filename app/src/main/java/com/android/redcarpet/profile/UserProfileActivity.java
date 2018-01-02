package com.android.redcarpet.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.redcarpet.LoginActivity;
import com.android.redcarpet.MainActivity;
import com.android.redcarpet.R;
import com.android.redcarpet.util.ProgressDialogHolder;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = UserProfileActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 18;
    private static final int TAKE_IMAGE = 19;

    private CircleImageView mProfileImage;
    private Uri mPhotoUri;
    private EditText mName;
    private EditText mEmail;

    private ProgressDialogHolder mProgressDialog;

    private boolean mImageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        BottomNavigationView mBottomNavigation = findViewById(R.id.menu_profile_nav);
        mBottomNavigation.setOnNavigationItemSelectedListener(this);

        mProgressDialog = new ProgressDialogHolder(this);

        mName = findViewById(R.id.profile_name_et);
        mEmail = findViewById(R.id.profile_email_et);

        mProfileImage = findViewById(R.id.profile_image);
        mProfileImage.setImageDrawable(getDrawable(R.drawable.default_profile_picture));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            rootRef.getRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mName.setText((CharSequence) dataSnapshot.child("name").getValue());
                    mEmail.setText((CharSequence) dataSnapshot.child("email").getValue());
                    mPhotoUri = Uri.parse(String.valueOf(dataSnapshot.child("photoUri").getValue()));
                    Log.i(TAG, "onDataChange: " + mPhotoUri.toString());
                    if (mPhotoUri != null) {
                        Glide.with(UserProfileActivity.this).load(mPhotoUri)
                                .into(mProfileImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        findViewById(R.id.profile_image_add_bt).setOnClickListener(this);
        findViewById(R.id.profile_save_bt).setOnClickListener(this);
        findViewById(R.id.profile_reset_pass_bt).setOnClickListener(this);
        findViewById(R.id.profile_sign_out_bt).setOnClickListener(this);
        findViewById(R.id.profile_del_acc_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.profile_image_add_bt:
                profilePicSelection();
                return;
            case R.id.profile_save_bt:
                saveUserProfile();
                return;
            case R.id.profile_reset_pass_bt:
                mProgressDialog.showLoadingDialog(R.string.reset_pass_sending);
                resetPassword(mEmail.getText().toString());
                return;
            case R.id.profile_sign_out_bt:
                signOut();
                return;
            case R.id.profile_del_acc_bt:
                deleteAccount();
        }
    }

    private void deleteAccount() {
        Log.i(TAG, "deleteAccount: ");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users");
        rootRef.child(user.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: Account data deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });

        user.delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(UserProfileActivity.this, "Account teletion failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: Account deleted");
                        Toast.makeText(UserProfileActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                        finish();
                    }
                });

    }

    private void signOut() {
        Log.i(TAG, "signOut: ");
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void resetPassword(String email) {
        Log.i(TAG, "resetPassword: ");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgressDialog.dismissDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                        mEmail.setError(e.getLocalizedMessage());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: ");
                        Toast.makeText(UserProfileActivity.this, "Please check your inbox", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserProfile() {
        Log.i(TAG, "saveUserProfile: ");
        String name = mName.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (mImageChanged) {
            // Create a storage reference from our app
            mProfileImage.setDrawingCacheEnabled(true);
            mProfileImage.buildDrawingCache();
            Bitmap bitmap = mProfileImage.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(user.getUid() + ".jpg");

            UploadTask uploadTask = storageReference.putBytes(data);
            uploadTask
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mPhotoUri = taskSnapshot.getDownloadUrl();
                            Log.i(TAG, "onSuccess: " + mPhotoUri.toString());
                        }
                    });
            mImageChanged = false;
        }

        if (user != null) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            rootRef.child("name").setValue(name);
            rootRef.child("photoUri").setValue(String.valueOf(mPhotoUri));
        }

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(mPhotoUri)
                .build();

        if (user != null) {
            user.updateProfile(profileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                            }
                        }
                    });
        }
    }

    private void profilePicSelection() {
        Log.i(TAG, "profilePicSelection: ");
        final CharSequence[] items = {"Take with camera", "Choose from library"};

        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Add profile photo");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                switch (item) {
                    case 0:
                        Log.i(TAG, "onClick: Take Photo");
                        cameraIntent();
                        return;
                    case 1:
                        Log.i(TAG, "onClick: Choose from Library");
                        galleryIntent();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Log.i(TAG, "galleryIntent: ");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void cameraIntent() {
        Log.i(TAG, "cameraIntent: ");
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && (requestCode == PICK_IMAGE || requestCode == TAKE_IMAGE)) {
            mImageChanged = true;
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mPhotoUri = result.getUri();
                mProfileImage.setImageURI(mPhotoUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(TAG, "onActivityResult: ", result.getError());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_home) {
            startActivity(new Intent(this, MainActivity.class));
        }
        return true;
    }
}
