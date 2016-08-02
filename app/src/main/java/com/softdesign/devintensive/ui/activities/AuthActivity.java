package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.requests.UserLoginReq;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.data.network.responses.UserListRes;
import com.softdesign.devintensive.data.network.responses.UserModelRes;
import com.softdesign.devintensive.data.storage.SaveUserDataOperation;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements View.OnClickListener {

    private DataManager mDataManager;
    private ChronosConnector mConnector;
    private Bus mBus;

    private CoordinatorLayout mCoordinatorLayout;
    private Button mSignInBtn;
    private EditText mLoginEt;
    private EditText mPasswordEt;
    private TextView mRememberPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mBus = new Bus();
        mBus.register(AuthActivity.this);
        mConnector = new ChronosConnector();
        mConnector.onCreate(this, savedInstanceState);

        mDataManager = DataManager.getInstance();

        signInByToken();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnector.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConnector.onPause();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mConnector.onSaveInstanceState(outState);
    }

    /**
     * Вызывается при вызове mBus.post(error) (в случае ошибки при запросе на сервер)
     *
     * @param error - текст ошибки
     */
    @Subscribe
    public void answerError(String error) {
        hideProgress();
        if (mLoginEt == null) {
            setContentView(R.layout.activity_auth);

            mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_container);
            mSignInBtn = (Button) findViewById(R.id.login_btn);
            mLoginEt = (EditText) findViewById(R.id.login_email);
            mPasswordEt = (EditText) findViewById(R.id.login_password);
            mRememberPassword = (TextView) findViewById(R.id.remember_tv);

            mSignInBtn.setOnClickListener(this);
            mRememberPassword.setOnClickListener(this);

            mLoginEt.setText(mDataManager.getPreferencesManager().getLoginEmail());
        }
        if (!error.isEmpty()) {
            showSnackBar(error);
        }
    }

    /**
     * Вызывается после записи данных, полученных с сервера, в базу данных
     *
     * @param result - результат выполнения
     */
    public void onOperationFinished(final SaveUserDataOperation.Result result) {
        hideProgress();
        Intent loginIntent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(loginIntent);
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
        if (mLoginEt != null) {
            mDataManager.getPreferencesManager().saveLoginEmail(mLoginEt.getText().toString());
        }

        saveUserInDb();
    }

    private void signInByToken() {
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            mBus.post(getString(R.string.error_network_not_available));
            return;
        }
        if (mDataManager.getPreferencesManager().getAuthToken().equals("")) {
            mBus.post("");
            return;
        }
        Call<UserInfoRes> call = mDataManager.loginToken(mDataManager.getPreferencesManager().getUserId());
        call.enqueue(new Callback<UserInfoRes>() {
            @Override
            public void onResponse(Call<UserInfoRes> call, Response<UserInfoRes> response) {
                if (response.code() == 200) {
                    loginSuccess(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<UserInfoRes> call, Throwable t) {
                mBus.post(getString(R.string.error_all_bad));
            }
        });
    }

    private void signIn() {
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            mBus.post(getString(R.string.error_network_not_available));
            return;
        }
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
                if (response.code() == 200) {
                    mDataManager.getPreferencesManager().saveAuthToken(response.body().getData().getToken());
                    loginSuccess(response.body().getData().getUser());
                } else if (response.code() == 404) {
                    mBus.post(getString(R.string.error_wrong_login_or_password));
                } else {
                    mBus.post(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserModelRes> call, Throwable t) {
                if (!NetworkStatusChecker.isNetworkAvailable(AuthActivity.this)) {
                    mBus.post(getString(R.string.error_network_not_available));
                } else {
                    mBus.post(getString(R.string.error_all_bad));
                }
            }
        });
    }

    private void saveUserValues(UserInfoRes.Data data) {
        int[] userValues = {
                data.getProfileValues().getFullRating(),
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
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            mBus.post(getString(R.string.error_network_not_available));
            return;
        }
        Call<UserListRes> call = mDataManager.getUserListFromNetwork();
        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, final Response<UserListRes> response) {
                try {
                    if (response.code() == 200) {
                        mConnector.runOperation(new SaveUserDataOperation(response), false);
                    } else {
                        Log.e(TAG, "onResponse: " + String.valueOf(response.errorBody().source()));
                        mBus.post(getString(R.string.error_load_users_list));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    mBus.post(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                if (!NetworkStatusChecker.isNetworkAvailable(AuthActivity.this)) {
                    mBus.post(getString(R.string.error_network_not_available));
                } else {
                    mBus.post(getString(R.string.error_all_bad));
                }
            }
        });
    }
}
