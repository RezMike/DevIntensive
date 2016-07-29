package com.softdesign.devintensive.data.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.data.network.responses.UserListRes;
import com.softdesign.devintensive.data.storage.models.Like;
import com.softdesign.devintensive.data.storage.models.LikeDao;
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
        LikeDao likeDao = dataManager.getDaoSession().getLikeDao();
        RepositoryDao repositoryDao = dataManager.getDaoSession().getRepositoryDao();

        List<Repository> allRepositories = new ArrayList<>();
        List<Like> allLikes = new ArrayList<>();
        List<User> allUsers = new ArrayList<>();

        long index = 0;

        for (UserListRes.Data userRes : mResponse.body().getData()) {
            index += 1;
            long number = index;
            User user = dataManager.getDaoSession().queryBuilder(User.class)
                    .where(UserDao.Properties.RemoteId.eq(userRes.getId())).build().unique();
            if (user != null) number = user.getIndex();
            allRepositories.addAll(getRepoListFromUserRes(userRes));
            allLikes.addAll(getLikeListFromUserRes(userRes));
            allUsers.add(new User(userRes, number));
        }

        likeDao.deleteAll();
        repositoryDao.insertOrReplaceInTx(allRepositories);
        likeDao.insertOrReplaceInTx(allLikes);
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

    private List<Like> getLikeListFromUserRes(UserListRes.Data userData) {
        final String userId = userData.getId();

        List<Like> likes = new ArrayList<>();
        for (String likedUserId : userData.getProfileValues().getLikesArray()) {
            likes.add(new Like(likedUserId, userId));
        }

        return likes;
    }

    @NonNull
    @Override
    public Class<Result> getResultClass() {
        return Result.class;
    }

    public static final class Result extends ChronosOperationResult<String> {

    }
}
