package com.softdesign.devintensive.data.network.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLikeRes {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
    private UserInfoRes.ProfileValues data;

    public UserInfoRes.ProfileValues getData() {
        return data;
    }
}
