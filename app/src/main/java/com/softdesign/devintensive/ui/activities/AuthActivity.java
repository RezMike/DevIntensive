package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.requests.UserLoginReq;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.data.network.responses.UserListRes;
import com.softdesign.devintensive.data.network.responses.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements View.OnClickListener {

    private DataManager mDataManager;
    private RepositoryDao mRepositoryDao;
    private UserDao mUserDao;

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.login_btn)
    Button mSignInBtn;
    @BindView(R.id.login_email)
    EditText mLoginEt;
    @BindView(R.id.login_password)
    EditText mPasswordEt;
    @BindView(R.id.remember_tv)
    TextView mRememberPassword;
    @BindView(R.id.login_indicator)
    View mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        mUserDao = mDataManager.getDaoSession().getUserDao();
        mRepositoryDao = mDataManager.getDaoSession().getRepositoryDao();

        signInByToken();

        mSignInBtn.setOnClickListener(this);
        mRememberPassword.setOnClickListener(this);

        mLoginEt.setText(mDataManager.getPreferencesManager().getLoginEmail());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                signIn();
                break;
            case R.id.remember_tv:
                rememberPassword();
                break;
        }
    }

    private void showSnackBar(String massage) {
        Snackbar.make(mCoordinatorLayout, massage, Snackbar.LENGTH_LONG).show();
    }

    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }

    private void loginSuccess(UserInfoRes.Data data) {
        mDataManager.getPreferencesManager().saveUserId(data.getId());
        saveUserValues(data);
        saveUserFields(data);
        mDataManager.getPreferencesManager().saveUserName(data.getFullName());
        mDataManager.getPreferencesManager().saveUserPhoto(
                Uri.parse(data.getPublicInfo().getPhoto()));
        mDataManager.getPreferencesManager().saveUserAvatar(
                Uri.parse(data.getPublicInfo().getAvatar()));
        saveUserInDb();

        mDataManager.getPreferencesManager().saveLoginEmail(mLoginEt.getText().toString());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(AuthActivity.this, MainActivity.class);
                startActivity(loginIntent);
            }
        }, AppConfig.START_DELAY);
    }

    private void signInByToken() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            if (!mDataManager.getPreferencesManager().getAuthToken().equals("")) {
                showProgress();
                Call<UserInfoRes> call = mDataManager.loginToken(mDataManager.getPreferencesManager().getUserId());
                call.enqueue(new Callback<UserInfoRes>() {
                    @Override
                    public void onResponse(Call<UserInfoRes> call, Response<UserInfoRes> response) {
                        hideProgress();
                        if (response.code() == 200) {
                            loginSuccess(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserInfoRes> call, Throwable t) {
                        showSnackBar(getString(R.string.error_all_bad));
                    }
                });
            }
        } else {
            showSnackBar(getString(R.string.error_network_not_available));
        }
    }

    private void signIn() {
        showProgress();
        Call<UserModelRes> call = mDataManager.loginUser(
                new UserLoginReq(
                        mLoginEt.getText().toString(),
                        mPasswordEt.getText().toString()
                )
        );
        call.enqueue(new Callback<UserModelRes>() {
            @Override
            public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                hideProgress();
                if (response.code() == 200) {
                    mDataManager.getPreferencesManager().saveAuthToken(response.body().getData().getToken());
                    loginSuccess(response.body().getData().getUser());
                } else if (response.code() == 404) {
                    showSnackBar(getString(R.string.error_wrong_login_or_password));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserModelRes> call, Throwable t) {
                hideProgress();
                if (!NetworkStatusChecker.isNetworkAvailable(AuthActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }

    private void saveUserValues(UserInfoRes.Data data) {
        int[] userValues = {
                data.getProfileValues().getRating(),
                data.getProfileValues().getLinesCode(),
                data.getProfileValues().getProjects()
        };
        mDataManager.getPreferencesManager().saveUserProfileValues(userValues);
    }

    private void saveUserFields(UserInfoRes.Data data) {
        String[] userFields = {
                data.getContacts().getPhone(),
                data.getContacts().getEmail(),
                data.getContacts().getVk(),
                data.getRepositories().getRepo().get(0).getGit(),
                data.getPublicInfo().getBio()
        };
        mDataManager.getPreferencesManager().saveUserProfileFields(Arrays.asList(userFields));
    }

    private void saveUserInDb() {
        showProgress();
        Call<UserListRes> call = mDataManager.getUserListFromNetwork();
        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, final Response<UserListRes> response) {
                hideProgress();
                try {
                    if (response.code() == 200) {
                        List<Repository> allRepositories = new ArrayList<>();
                        List<User> allUsers = new ArrayList<>();

                        for (UserListRes.Data userRes : response.body().getData()) {
                            allRepositories.addAll(getRepoListFromUserRes(userRes));
                            allUsers.add(new User(userRes));
                        }

                        mRepositoryDao.insertOrReplaceInTx(allRepositories);
                        mUserDao.insertOrReplaceInTx(allUsers);
                    } else {
                        Log.e(TAG, "onResponse: " + String.valueOf(response.errorBody().source()));
                        showSnackBar(getString(R.string.error_load_users_list));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                hideProgress();
                if (!NetworkStatusChecker.isNetworkAvailable(AuthActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }

    private List<Repository> getRepoListFromUserRes(UserListRes.Data userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserInfoRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }

        return repositories;
    }
}
