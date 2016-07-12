package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.requests.UserLoginReq;
import com.softdesign.devintensive.data.network.responses.UserModelRes;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements View.OnClickListener{

    private DataManager mDataManager;

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

        mSignInBtn.setOnClickListener(this);
        mRememberPassword.setOnClickListener(this);

        mLoginEt.setText(mDataManager.getPreferencesManager().getLoginEmail());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                signIn();
                break;
            case R.id.remember_tv:
                rememberPassword();
                break;
        }
    }

    private void showSnackbar(String massage){
        Snackbar.make(mCoordinatorLayout, massage, Snackbar.LENGTH_LONG).show();
    }

    private void rememberPassword(){
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }

    private void loginSuccess(UserModelRes userModel){
        mDataManager.getPreferencesManager().saveAuthToken(userModel.getData().getToken());
        mDataManager.getPreferencesManager().saveUserId(userModel.getData().getUser().getId());
        saveUserValues(userModel);
        saveUserFields(userModel);
        mDataManager.getPreferencesManager().saveLoginEmail(mLoginEt.getText().toString());

        Intent loginIntent = new Intent(this, MainActivity.class);
        startActivity(loginIntent);
        AuthActivity.this.finish();
    }

    private void signIn(){
        if (NetworkStatusChecker.isNetworkAvailable(this)) {
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
                        loginSuccess(response.body());
                    } else if (response.code() == 404) {
                        showSnackbar("Неверный логин или пароль");
                    } else {
                        showSnackbar("Все пропало, Шеф!!!");
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    //// TODO: 12.07.2016 обработать ошибки
                }
            });
        } else {
            showSnackbar("Сеть на данный момент не доступна, попробуйте позже");
        }
    }

    private void saveUserValues(UserModelRes userModel){
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects(),
        };
        mDataManager.getPreferencesManager().saveUserProfileValues(userValues);
    }

    private void saveUserFields(UserModelRes userModel) {
        String[] userFields = {
                userModel.getData().getUser().getContacts().getPhone(),
                userModel.getData().getUser().getContacts().getEmail(),
                userModel.getData().getUser().getContacts().getVk(),
                userModel.getData().getUser().getRepositories().getRepo().get(0).getGit(),
                userModel.getData().getUser().getPublicInfo().getBio()
        };
        mDataManager.getPreferencesManager().saveUserProfileFields(Arrays.asList(userFields));
    }
}
