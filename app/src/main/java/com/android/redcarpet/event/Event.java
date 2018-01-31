package com.android.redcarpet.event;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

@IgnoreExtraProperties
public class Event implements Parcelable {
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    private String uId;
    private String photoUri;
    private String title;
    private String organizer;
    private String date;
    private String location;
    private String price;
    private String description;
    private String dateCreated;

    public Event(String uid, String photoUri, String title, String organizer, String date, String location, String price, String description) {
        this.uId = uid;
        this.photoUri = photoUri;
        this.title = title;
        this.organizer = organizer;
        this.date = date;
        this.location = location;
        this.price = price;
        this.description = description;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateCreated = df.format(Calendar.getInstance().getTime());
    }

    public Event() {
    }

    protected Event(Parcel in) {
        uId = in.readString();
        photoUri = in.readString();
        title = in.readString();
        organizer = in.readString();
        date = in.readString();
        location = in.readString();
        price = in.readString();
        description = in.readString();
        dateCreated = in.readString();
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.uId, this.dateCreated);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uId);
        parcel.writeString(photoUri);
        parcel.writeString(title);
        parcel.writeString(organizer);
        parcel.writeString(date);
        parcel.writeString(location);
        parcel.writeString(price);
        parcel.writeString(description);
        parcel.writeString(dateCreated);
    }
}
