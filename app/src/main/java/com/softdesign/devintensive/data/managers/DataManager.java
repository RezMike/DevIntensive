package com.softdesign.devintensive.data.managers;

import android.content.Context;

import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.requests.UserLoginReq;
import com.softdesign.devintensive.data.network.responses.UserModelRes;
import com.softdesign.devintensive.utils.DevintensiveApplication;

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

    //region ==================== Network =======================

    public Call<UserModelRes> loginUser(UserLoginReq userLoginReq){
        return mRestService.loginUser(userLoginReq);
    }

    //end region

    // region ==================== Database =======================



    //end region
}
