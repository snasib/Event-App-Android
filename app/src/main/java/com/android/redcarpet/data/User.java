package com.android.redcarpet.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User implements Parcelable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private final Uri DEFAULT_PROFILE_PIC = Uri.parse("https://firebasestorage.googleapis.com/v0/b/redcarpet-6ce47.appspot.com/o/default_profile_pic.svg?alt=media&token=6a4b9f94-7fa2-41f1-99e8-20bb98594fb8");
    private String providerId;
    private String email;
    private String name;
    private String phone;
    private String photoUri;

    private User(String providerId, String email, String name, String phone, String photoUri) {
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.photoUri = photoUri;
        if (TextUtils.isEmpty(photoUri)) {
            this.photoUri = String.valueOf(DEFAULT_PROFILE_PIC);
        }
    }

    public User() {
    }

    protected User(Parcel in) {
        providerId = in.readString();
        email = in.readString();
        name = in.readString();
        phone = in.readString();
        photoUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public String getProviderId() {
        return providerId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(providerId);
        parcel.writeString(email);
        parcel.writeString(name);
        parcel.writeString(phone);
        parcel.writeString(photoUri);
    }

    public static class Builder {
        private String mProviderId;
        private String mEmail;
        private String mPhoneNumber;
        private String mName;
        private String mPhotoUri = null;

        public Builder(@NonNull String providerId, @Nullable String email) {
            mProviderId = providerId;
            mEmail = email;
        }

        public Builder(@NonNull FirebaseUser user) {
            for (UserInfo profile : user.getProviderData()) {
                mProviderId = profile.getProviderId();
                mEmail = user.getEmail();
                mName = user.getDisplayName();
                mPhoneNumber = user.getPhoneNumber();
                mPhotoUri = String.valueOf(user.getPhotoUrl());
            }
        }

        public Builder setPhoneNumber(String phoneNumber) {
            mPhoneNumber = phoneNumber;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setPhotoUri(String photoUri) {
            mPhotoUri = photoUri;
            return this;
        }

        public User build() {
            return new User(mProviderId, mEmail, mName, mPhoneNumber, mPhotoUri);
        }
    }
}
