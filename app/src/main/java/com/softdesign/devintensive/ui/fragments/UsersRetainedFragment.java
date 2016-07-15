package com.softdesign.devintensive.ui.fragments;

import android.os.Bundle;
import android.app.Fragment;

import com.softdesign.devintensive.data.network.responses.UserListRes;

import java.util.List;

public class UsersRetainedFragment extends Fragment {
    private List<UserListRes.Data> mUsers;
    private List<UserListRes.Data> mFilteredUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setUsers(List<UserListRes.Data> users) {
        mUsers = users;
    }

    public void setFilteredUsers(List<UserListRes.Data> filteredUsers){
        mFilteredUsers = filteredUsers;
    }

    public List<UserListRes.Data> getUsers() {
        return mUsers;
    }

    public List<UserListRes.Data> getFilteredUsers() {
        return mFilteredUsers;
    }
}