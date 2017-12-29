package com.android.redcarpet.util;

import android.support.design.widget.TextInputLayout;
import android.util.Patterns;
import android.widget.EditText;

import com.android.redcarpet.R;

public final class Validator {

    public static boolean isPasswordValid(CharSequence charSequence) {
        return charSequence.length() > 5 && charSequence.length() < 13;
    }

    public static boolean isPasswordValid(TextInputLayout inputLayout, CharSequence charSequence) {
        boolean result = (charSequence.length() > 5 && charSequence.length() < 13);
        inputLayout.setError(null);
        if (!result) {
            inputLayout.setError(inputLayout.getResources().getString(R.string.error_password_length));
        }
        return result;
    }

    public static boolean isPasswordValid(EditText editText, CharSequence charSequence) {
        boolean result = (charSequence.length() > 5 && charSequence.length() < 13);
        editText.setError(null);
        if (!result) {
            editText.setError(editText.getResources().getString(R.string.error_password_length));
        }
        return result;
    }

    public static boolean isEmailValid(CharSequence charSequence) {
        return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
    }

    public static boolean isEmailValid(TextInputLayout inputLayout, CharSequence charSequence) {
        boolean result = Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
        inputLayout.setError(null);
        if (!result) {
            inputLayout.setError(inputLayout.getResources().getString(R.string.error_invalid_email));
        }
        return result;
    }

    public static boolean isEmailValid(EditText editText, CharSequence charSequence) {
        boolean result = Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
        editText.setError(null);
        if (!result) {
            editText.setError(editText.getResources().getString(R.string.error_invalid_email));
        }
        return result;
    }

    public static boolean isRequiredFieldValid(CharSequence charSequence) {
        return charSequence != null && charSequence.length() > 0;
    }

    public static boolean isRequiredFieldValid(TextInputLayout inputLayout, CharSequence charSequence) {
        boolean result = charSequence != null && charSequence.length() > 0;
        inputLayout.setError(null);
        if (!result) {
            inputLayout.setError(inputLayout.getResources().getString(R.string.error_required_field));
        }
        return result;
    }

    public static boolean isRequiredFieldValid(EditText editText, CharSequence charSequence) {
        boolean result = charSequence != null && charSequence.length() > 0;
        editText.setError(null);
        if (!result) {
            editText.setError(editText.getResources().getString(R.string.error_required_field));
        }
        return result;
    }
}
