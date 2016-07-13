package com.softdesign.devintensive.data.network.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
        private UserRes.Data user;
        @SerializedName("token")
        @Expose
        private String token;

        public UserRes.Data getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }
}