package com.softdesign.devintensive.data.managers;

import android.content.Context;

import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.requests.UserLoginReq;
import com.softdesign.devintensive.data.network.responses.UploadPhotoRes;
import com.softdesign.devintensive.data.network.responses.UserListRes;
import com.softdesign.devintensive.data.network.responses.UserModelRes;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.utils.DevintensiveApplication;

import okhttp3.MultipartBody;
import retrofit2.Call;

public class DataManager {
    private static DataManager INSTANCE = null;

    private Context mContext;
    private PreferencesManager mPreferencesManager;
    private RestService mRestService;

    private DataManager() {
        mPreferencesManager = new PreferencesManager();
        mContext = DevintensiveApplication.getContext();
        mRestService = ServiceGenerator.createService(RestService.class);
    }

    public static DataManager getInstance(){
        if (INSTANCE == null){
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    public Context getContext() {
        return mContext;
    }

    public RestService getRestService() {
        return mRestService;
    }

    //region ==================== Network =======================

    public Call<UserModelRes> loginUser(UserLoginReq userLoginReq){
        return mRestService.loginUser(userLoginReq);
    }

    public Call<UserInfoRes> loginToken(String userId){
        return mRestService.loginToken(userId);
    }

    public Call<UserListRes> getUserList(){
        return mRestService.getUserList();
    }

    public Call<UploadPhotoRes> uploadPhoto(String userId, MultipartBody.Part file){
        return mRestService.uploadPhoto(userId, file);
    }

    public Call<UploadPhotoRes> uploadAvatar(String userId, MultipartBody.Part file){
        return mRestService.uploadAvatar(userId, file);
    }

    //end region

    // region ==================== Database =======================



    //end region
}
