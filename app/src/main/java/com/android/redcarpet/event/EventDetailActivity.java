package com.android.redcarpet.event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.redcarpet.R;
import com.android.redcarpet.util.ProgressDialogHolder;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class EventDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = EventDetailActivity.class.getSimpleName();

    private Event mEvent;

    private ImageView mProfileImage;
    private TextView mEventTitle;
    private TextView mEventOrganizer;
    private TextView mEventDate;
    private TextView mEventLoc;
    private TextView mEventPrice;
    private TextView mEventDesc;
    private boolean isAdmin = false;
    private ProgressDialogHolder dialogHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mProfileImage = findViewById(R.id.event_pic_iv);
        mEventTitle = findViewById(R.id.event_title_tv);
        mEventOrganizer = findViewById(R.id.event_organizer_tv);
        mEventDate = findViewById(R.id.event_date_tv);
        mEventLoc = findViewById(R.id.event_loc_tv);
        mEventPrice = findViewById(R.id.event_price_tv);
        mEventDesc = findViewById(R.id.event_desc_tv);
        FloatingActionButton mFabDelete = findViewById(R.id.event_del_bt);

        dialogHolder = new ProgressDialogHolder(this);

        Intent intent = getIntent();
        mEvent = intent.getParcelableExtra("event");

        checkForAdmin(mEvent);
        updateUI(mEvent);

        if (!isAdmin) {
            mFabDelete.setVisibility(View.GONE);
        } else {
            mFabDelete.setOnClickListener(this);
        }
    }

    private void checkForAdmin(Event event) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().equals(event.getuId())) {
            isAdmin = true;
        }
    }

    private void updateUI(@NonNull Event event) {
        Glide.with(this)
                .load(Uri.parse(event.getPhotoUri()))
                .into(mProfileImage);
        mEventTitle.setText(event.getTitle());
        mEventOrganizer.setText(event.getOrganizer());
        mEventDate.setText(event.getDate());
        mEventLoc.setText(event.getLocation());
        mEventPrice.setText(event.getPrice());
        mEventDesc.setText(event.getDescription());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.event_del_bt) {
            deleteEvent();
        }
    }

    private void deleteEvent() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        dialogHolder.showLoadingDialog("Deleting the event");
        db.getReference("events").child(String.valueOf(mEvent.hashCode())).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialogHolder.dismissDialog();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: deletion successful");
                        Toast.makeText(EventDetailActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                        deleteImageFromStorage();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(EventDetailActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteImageFromStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference(mEvent.getTitle() + ".jpg").delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: Image deleted from Storage");
                    }
                });
    }

}
