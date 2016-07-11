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

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthActivity extends BaseActivity implements View.OnClickListener{

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

        mSignInBtn.setOnClickListener(this);
        mRememberPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
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

    private void loginSuccess(){

    }
}
