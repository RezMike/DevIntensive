package com.softdesign.devintensive.data.network.responses;

import android.support.v7.app.ActionBar;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserListRes {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<Data> data = new ArrayList<>();

    public List<Data> getData() {
        return data;
    }

    public class Data {
        @SerializedName("_id")
        @Expose
        private String id;
        @SerializedName("first_name")
        @Expose
        private String firstName;
        @SerializedName("second_name")
        @Expose
        private String secondName;
        @SerializedName("__v")
        @Expose
        private Integer v;
        @SerializedName("repositories")
        @Expose
        private UserInfoRes.Repositories repositories;
        @SerializedName("profileValues")
        @Expose
        private UserInfoRes.ProfileValues profileValues;
        @SerializedName("publicInfo")
        @Expose
        private UserInfoRes.PublicInfo publicInfo;
        @SerializedName("specialization")
        @Expose
        private String specialization;
        @SerializedName("updated")
        @Expose
        private String updated;

        public String getId() {
            return id;
        }

        public String getFullName() {
            return secondName + " " + firstName;
        }

        public UserInfoRes.Repositories getRepositories() {
            return repositories;
        }

        public UserInfoRes.ProfileValues getProfileValues() {
            return profileValues;
        }

        public UserInfoRes.PublicInfo getPublicInfo() {
            return publicInfo;
        }
    }
}
