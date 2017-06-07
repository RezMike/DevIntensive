package com.softdesign.devintensive.ui.custom;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import com.softdesign.devintensive.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditTextWatcher implements TextWatcher {
    private static final String phonePattern = "^\\+\\d \\(\\d{3}\\) \\d{3}\\-\\d{2}\\-\\d{2}$";
    private static final String emailPattern = "^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$";
    private static final String vkPattern = "^vk\\.com\\/\\w{3,}$";
    private static final String gitPattern = "^github\\.com\\/.{3,}$";

    Context mContext;
    EditText mEditText;
    ImageView mButton;
    TextInputLayout mLayout;

    public EditTextWatcher(Context context, EditText editText, ImageView button, TextInputLayout layout) {
        mContext = context;
        mEditText = editText;
        mButton = button;
        mLayout = layout;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String str = s.toString().toLowerCase();
        if (str.contains("http://")) {
            mEditText.setText(str.replace("http://", ""));
        }
        if (str.contains("https://")) {
            mEditText.setText(str.replace("https://", ""));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        switch (mEditText.getId()) {
            case R.id.phone_et:
                checkInputString(
                        phonePattern,
                        s.toString(),
                        mContext.getString(R.string.error_phone_et)
                );
                break;
            case R.id.email_et:
                checkInputString(
                        emailPattern,
                        s.toString(),
                        mContext.getString(R.string.error_email_et)
                );
                break;
            case R.id.vk_et:
                checkInputString(
                        vkPattern,
                        s.toString(),
                        mContext.getString(R.string.error_vk_et)
                );
                break;
            case R.id.git_et:
                checkInputString(
                        gitPattern,
                        s.toString(),
                        mContext.getString(R.string.error_git_et)
                );
                break;
        }
    }

    private void checkInputString(String patternString, String inputString, String error) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.matches()) {
            mButton.setEnabled(true);
            mLayout.setError("");
            mLayout.setErrorEnabled(false);
        } else {
            mButton.setEnabled(false);
            mLayout.setError(error);
        }
    }
}
