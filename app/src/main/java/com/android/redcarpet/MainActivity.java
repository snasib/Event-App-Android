package com.android.redcarpet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.android.redcarpet.adapter.EventAdapter;
import com.android.redcarpet.event.Event;
import com.android.redcarpet.event.EventCreateActivity;
import com.android.redcarpet.profile.UserProfileActivity;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView mListView;
    private DatabaseReference mEventsRef;
    private FirebaseListAdapter<Event> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView mBottomNavigation = findViewById(R.id.navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(this);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EventCreateActivity.class));
            }
        });

        mListView = findViewById(R.id.listView);

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("events")
                .limitToLast(5);

        FirebaseListOptions<Event> options = new FirebaseListOptions.Builder<Event>()
                .setLayout(R.layout.list_items_layout)
                .setQuery(query, Event.class)
                .build();

        mAdapter = new EventAdapter(options, MainActivity.this);
        mListView.setAdapter(mAdapter);

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart: ");
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop: ");
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
        }
        return true;
    }
}