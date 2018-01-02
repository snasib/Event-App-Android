package com.android.redcarpet.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.redcarpet.MainActivity;
import com.android.redcarpet.R;
import com.android.redcarpet.util.ProgressDialogHolder;
import com.android.redcarpet.util.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class EventCreateActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = EventCreateActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 91;
    private static final int TAKE_IMAGE = 92;

    private ImageView mPhoto;
    private EditText mTitleField;
    private EditText mOrganizerField;
    private EditText mDateField;
    private EditText mLocField;
    private EditText mPriceField;
    private EditText mAboutField;

    private String mDateTime = "";
    private Uri mPhotoUri;
    private ProgressDialogHolder mProgress;

    private String mTitle;
    private String mOrganizer;
    private String mLocation;
    private String mPrice;
    private String mAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);

        mPhoto = findViewById(R.id.event_create_pic);
        mTitleField = findViewById(R.id.event_create_title);
        mOrganizerField = findViewById(R.id.event_create_organizer);
        mDateField = findViewById(R.id.event_create_date);
        mLocField = findViewById(R.id.event_create_loc);
        mPriceField = findViewById(R.id.event_create_price);
        mAboutField = findViewById(R.id.event_create_desc);

        findViewById(R.id.event_create_save_bt).setOnClickListener(this);

        mPhoto.setOnClickListener(this);
        mDateField.setOnClickListener(this);

        mDateField.setFocusable(false);

        mTitleField.setOnFocusChangeListener(this);
        mOrganizerField.setOnFocusChangeListener(this);
        mLocField.setOnFocusChangeListener(this);
        mPriceField.setOnFocusChangeListener(this);
        mAboutField.setOnFocusChangeListener(this);

        mProgress = new ProgressDialogHolder(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.event_create_pic:
                chooseImage();
                return;
            case R.id.event_create_date:
                showDatePickerDialog();
                return;
            case R.id.event_create_save_bt:
                validateAndSaveEvent();
        }
    }

    private void validateAndSaveEvent() {
        Log.i(TAG, "validateAndSaveEvent: ");

        mTitleField.setError(null);
        mOrganizerField.setError(null);
        mLocField.setError(null);
        mPriceField.setError(null);
        mAboutField.setError(null);


        mTitle = mTitleField.getText().toString();
        mOrganizer = mOrganizerField.getText().toString();
        mLocation = mLocField.getText().toString();
        mPrice = mPriceField.getText().toString();
        mAbout = mAboutField.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!Validator.isRequiredFieldValid(mTitleField, mTitle)) {
            cancel = true;
            focusView = mTitleField;
        }
        if (!Validator.isDateValid(mDateField, mDateTime)) {
            cancel = true;
            focusView = mDateField;
        }
        if (!Validator.isRequiredFieldValid(mOrganizerField, mOrganizer)) {
            cancel = true;
            focusView = mOrganizerField;
        }
        if (!Validator.isRequiredFieldValid(mLocField, mLocation)) {
            cancel = true;
            focusView = mLocField;
        }
        if (!Validator.isRequiredFieldValid(mPriceField, mPrice)) {
            cancel = true;
            focusView = mPriceField;
        }
        if (!Validator.isRequiredFieldValid(mAboutField, mAbout)) {
            cancel = true;
            focusView = mAboutField;
        }
        if (!Validator.isImageChosen(mPhotoUri)) {
            cancel = true;
            Toast.makeText(this, "You have to add an image", Toast.LENGTH_SHORT).show();
            focusView = mPhoto;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mProgress.showLoadingDialog(R.string.registering);
            uploadImageAndSaveEvent();
        }
    }

    private void uploadImageAndSaveEvent() {
        Log.i(TAG, "uploadImageAndSaveEvent: ");

        mProgress.showLoadingDialog("Saving Event...");

        mPhoto.setDrawingCacheEnabled(true);
        mPhoto.buildDrawingCache();
        Bitmap bitmap = mPhoto.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(mTitle + ".jpg");

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        mProgress.dismissDialog();
                        Toast.makeText(EventCreateActivity.this, "Upload failed. Please check your internet and try again", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "onSuccess: " + Objects.requireNonNull(downloadUrl).toString());
                        uploadToFirebase(downloadUrl);
                    }
                });

    }

    private void uploadToFirebase(Uri downloadUrl) {
        Log.i(TAG, "uploadToFirebase: ");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Event mEvent = new Event(user.getUid(), String.valueOf(downloadUrl), mTitle, mOrganizer, mDateTime, mLocation, mPrice, mAbout);
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("events");
            rootRef.push().setValue(mEvent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "onSuccess: Event saved");
                            Toast.makeText(EventCreateActivity.this, "Event saved", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EventCreateActivity.this, MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                            Toast.makeText(EventCreateActivity.this, "Uploading event failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mProgress.dismissDialog();
                        }
                    });
        }
    }

    private void chooseImage() {
        Log.i(TAG, "chooseImage: ");
        final CharSequence[] items = {"Take with camera", "Choose from library"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EventCreateActivity.this);
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
        Log.i(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && (requestCode == PICK_IMAGE || requestCode == TAKE_IMAGE)) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mPhotoUri = result.getUri();
                mPhoto.setImageURI(mPhotoUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(TAG, "onActivityResult: ", result.getError());
            }
        }
    }

    public void showTimePickerDialog() {
        Log.i(TAG, "showTimePickerDialog: ");
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        mDateTime += " " + hour + ":" + minute;
                        mDateField.setText(mDateTime);
                    }
                }, currentHour, currentMinute, false);
        timePickerDialog.show();
    }

    public void showDatePickerDialog() {
        Log.i(TAG, "showDatePickerDialog: ");
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Log.i(TAG, "onDateSet: " + year + " " + month + " " + day);
                        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                        mDateTime = format.format(new Date(year - 1900, month, day));
                        showTimePickerDialog();
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            ((EditText) view).setError(null);
            return;
        }

        int id = view.getId();
        switch (id) {
            case R.id.event_create_title:
                Validator.isRequiredFieldValid(mTitleField, mTitleField.getText().toString());
                return;
            case R.id.event_create_organizer:
                Validator.isRequiredFieldValid(mOrganizerField, mOrganizerField.getText().toString());
                return;
            case R.id.event_create_loc:
                Validator.isRequiredFieldValid(mLocField, mLocField.getText().toString());
                return;
            case R.id.event_create_price:
                Validator.isRequiredFieldValid(mPriceField, mPriceField.getText().toString());
                return;
            case R.id.event_create_desc:
                Validator.isRequiredFieldValid(mAboutField, mAboutField.getText().toString());
        }
    }


}
