package com.softdesign.devintensive.data.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.data.network.responses.UserListRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class SaveUserDataOperation extends ChronosOperation<String> {
    private Response<UserListRes> mResponse;

    public SaveUserDataOperation(Response<UserListRes> response) {
        mResponse = response;
    }

    @Nullable
    @Override
    public String run() {
        DataManager dataManager = DataManager.getInstance();
        UserDao userDao = dataManager.getDaoSession().getUserDao();
        RepositoryDao repositoryDao = dataManager.getDaoSession().getRepositoryDao();
        List<Repository> allRepositories = new ArrayList<>();
        List<User> allUsers = new ArrayList<>();

        for (UserListRes.Data userRes : mResponse.body().getData()) {
            allRepositories.addAll(getRepoListFromUserRes(userRes));
            allUsers.add(new User(userRes));
        }

        repositoryDao.insertOrReplaceInTx(allRepositories);
        userDao.insertOrReplaceInTx(allUsers);

        return null;
    }

    private List<Repository> getRepoListFromUserRes(UserListRes.Data userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserInfoRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }

        return repositories;
    }

    @NonNull
    @Override
    public Class<Result> getResultClass() {
        return Result.class;
    }

    public static final class Result extends ChronosOperationResult<String> {

    }
}
