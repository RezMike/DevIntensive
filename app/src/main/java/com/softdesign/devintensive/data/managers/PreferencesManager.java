package com.softdesign.devintensive.data.managers;

import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevintensiveApplication;

import java.util.ArrayList;
import java.util.List;

public class PreferencesManager {

    private SharedPreferences mSharedPreferences;

    private static final String[] USER_FIELDS = {
            ConstantManager.USER_PHONE_KEY,
            ConstantManager.USER_EMAIL_KEY,
            ConstantManager.USER_VK_KEY,
            ConstantManager.USER_GIT_KEY,
            ConstantManager.USER_BIO_KEY,
    };

    private static final String[] USER_VALUES = {
            ConstantManager.USER_RATING_VALUE,
            ConstantManager.USER_CODE_LINES_VALUE,
            ConstantManager.USER_PROJECT_VALUE,
    };

    public PreferencesManager() {
        this.mSharedPreferences = DevintensiveApplication.getSharedPreferences();
    }

    public void saveUserProfileFields(List<String> userFields){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (int i = 0; i < USER_FIELDS.length; ++i) {
            editor.putString(USER_FIELDS[i], userFields.get(i));
        }
        editor.apply();
    }

    public List<String> loadUserProfileFields(){
        List<String> userFields = new ArrayList<>();
        for (int i = 0; i < USER_FIELDS.length; ++i) {
            userFields.add(mSharedPreferences.getString(USER_FIELDS[i], ""));
        }
        return userFields;
    }

    public void saveUserProfileValues(int[] userValues){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (int i = 0; i < USER_VALUES.length; ++i) {
            editor.putString(USER_VALUES[i], String.valueOf(userValues[i]));
        }
        editor.apply();
    }

    public List<String> loadUserProfileValues(){
        List<String> userValues = new ArrayList<>();
        for (int i = 0; i < USER_VALUES.length; ++i) {
            userValues.add(mSharedPreferences.getString(USER_VALUES[i], "0"));
        }
        return userValues;
    }

    public void saveUserPhoto(Uri uri){
        SharedPreferences.Editor  editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_PHOTO_KEY, uri.toString());
        editor.apply();
    }

    public Uri loadUserPhoto(){
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PHOTO_KEY,
                        "android.resource://com.softdesign.devintensive/" + R.drawable.user_bg));
    }

    public String getUserDataField(String field){
        return mSharedPreferences.getString(field, "");
    }

    public void saveAuthToken(String authToken){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.AUTH_TOKEN_KEY, authToken);
        editor.apply();
    }

    public String getAuthToken(){
        return mSharedPreferences.getString(ConstantManager.AUTH_TOKEN_KEY, "");
    }

    public void saveUserId(String userId){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_ID_KEY, userId);
        editor.apply();
    }

    public String getUserId(){
        return mSharedPreferences.getString(ConstantManager.USER_ID_KEY, "");
    }

    public void saveLoginEmail(String loginEmail){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_LOGIN_EMAIL, loginEmail);
        editor.apply();
    }

    public String getLoginEmail(){
        return mSharedPreferences.getString(ConstantManager.USER_LOGIN_EMAIL, "");
    }
}