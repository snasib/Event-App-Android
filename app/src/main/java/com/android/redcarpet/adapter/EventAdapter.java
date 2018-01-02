package com.android.redcarpet.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.redcarpet.R;
import com.android.redcarpet.event.Event;
import com.android.redcarpet.event.EventDetailActivity;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;

public class EventAdapter extends FirebaseListAdapter<Event> {
    private final static String TAG = EventAdapter.class.getSimpleName();
    private Context mContext;

    public EventAdapter(@NonNull FirebaseListOptions<Event> options) {
        super(options);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    protected void populateView(View view, Event model, int position) {
        Log.i(TAG, "populateView: ");

        ImageView profileImage = view.findViewById(R.id.list_event_pic);
        TextView eventDate = view.findViewById(R.id.list_event_date);
        TextView eventTitle = view.findViewById(R.id.list_event_title);
        TextView eventOrganizer = view.findViewById(R.id.list_event_organizer);
        TextView eventLocation = view.findViewById(R.id.list_event_loc);

        final Event event = this.getItem(position);

        Glide.with(mContext)
                .load(Uri.parse(event.getPhotoUri()))
                .into(profileImage);
        eventDate.setText(event.getDate());
        eventTitle.setText(event.getTitle());
        eventOrganizer.setText(event.getOrganizer());
        eventLocation.setText(event.getLocation());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, EventDetailActivity.class);
                intent.putExtra("event", event);
                mContext.startActivity(intent);
            }
        });
    }
}
