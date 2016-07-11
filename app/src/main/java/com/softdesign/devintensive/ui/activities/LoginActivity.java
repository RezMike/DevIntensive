package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.softdesign.devintensive.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.login_enter_btn)
    Button mEnterBtn;
    @BindView(R.id.login_et)
    EditText mLoginEt;
    @BindView(R.id.password_et)
    EditText mPasswordEt;
    @BindView(R.id.login_indicator)
    View mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mEnterBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_enter_btn:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
        }
    }
}
