package com.softdesign.devintensive.ui.fragments;

import android.os.Bundle;
import android.app.Fragment;

import java.util.List;

import com.softdesign.devintensive.data.storage.models.User;

public class UsersRetainedFragment extends Fragment {
    private List<User> mUsers;
    private List<User> mFilteredUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setUsers(List<User> users) {
        mUsers = users;
    }

    public void setFilteredUsers(List<User> filteredUsers) {
        mFilteredUsers = filteredUsers;
    }

    public List<User> getUsers() {
        return mUsers;
    }

    public List<User> getFilteredUsers() {
        return mFilteredUsers;
    }
}