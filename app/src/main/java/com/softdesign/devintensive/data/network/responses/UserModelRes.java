package com.softdesign.devintensive.data.network.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserModelRes {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public class Data {
        @SerializedName("user")
        @Expose
        private UserInfoRes.Data user;
        @SerializedName("token")
        @Expose
        private String token;

        public UserInfoRes.Data getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }
}