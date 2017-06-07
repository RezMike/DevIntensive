package com.softdesign.devintensive.data.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.DevintensiveApplication;

import java.util.List;

public class LoadUserDataOperation extends ChronosOperation<List<User>> {
    @Nullable
    @Override
    public List<User> run() {
        return DevintensiveApplication.getDaoSession().queryBuilder(User.class)
                .whereOr(UserDao.Properties.CodeLines.gt(0), UserDao.Properties.FullRating.gt(0))
                .orderAsc(UserDao.Properties.Index)
                .build()
                .list();
    }

    @NonNull
    @Override
    public Class<Result> getResultClass() {
        return Result.class;
    }

    public static final class Result extends ChronosOperationResult<List<User>> {

    }
}