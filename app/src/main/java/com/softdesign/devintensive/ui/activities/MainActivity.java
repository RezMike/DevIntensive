package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.responses.UploadPhotoRes;
import com.softdesign.devintensive.ui.custom.EditTextWatcher;
import com.softdesign.devintensive.ui.views.CircleImageView;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = ConstantManager.TAG_PREFIX + "MainActivity";

    private DataManager mDataManager;

    private int mCurrentEditMode = 0;

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.profile_placeholder)
    RelativeLayout mProfilePlaceholder;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;
    @BindView(R.id.rating_layout)
    LinearLayout mRatingLayout;
    @BindView(R.id.phone_et)
    EditText mPhoneEt;

    @BindViews({R.id.user_info_rating, R.id.user_info_lines, R.id.user_info_projects})
    List<TextView> mUserValueViews;
    @BindViews({R.id.phone_et, R.id.email_et, R.id.vk_et, R.id.git_et, R.id.bio_et})
    List<EditText> mUserInfoViews;
    @BindViews({R.id.phone_img, R.id.email_img, R.id.vk_img, R.id.git_img})
    List<ImageView> mUserInfoImages;
    @BindViews({R.id.phone_et, R.id.email_et, R.id.vk_et, R.id.git_et})
    List<EditText> mUserInfoEdits;

    private AppBarLayout.LayoutParams mAppBarParams = null;

    private File mPhotoFile = null;

    private List<EditTextWatcher> mTextWatchers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.d(TAG, "OnCreate");

        mDataManager = DataManager.getInstance();

        mFab.setOnClickListener(this);
        mProfilePlaceholder.setOnClickListener(this);

        for (ImageView imageView : mUserInfoImages) {
            imageView.setOnClickListener(this);
        }

        setupToolbar();
        setupDrawer();
        initUserFields();
        initUserInfoValues();

        setUserPhoto();

        mCollapsingToolbar.setTitle(mDataManager.getPreferencesManager().getUserName());

        if (savedInstanceState != null) {
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_KEY, 0);
            changeEditMode(mCurrentEditMode);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        saveUserFields();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "OnRestart");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else if (mCurrentEditMode == 1) {
            changeEditMode(0);
            mCurrentEditMode = 0;
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (mCurrentEditMode == 0) {
                    changeEditMode(1);
                    mCurrentEditMode = 1;
                } else {
                    changeEditMode(0);
                    mCurrentEditMode = 0;
                }
                break;
            case R.id.profile_placeholder:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;
            case R.id.phone_img:
                String phoneNumber = mDataManager.getPreferencesManager().getUserProfileField(ConstantManager.USER_PHONE_KEY);
                if (!phoneNumber.equals("")) {
                    Intent mCallIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                    startActivity(mCallIntent);
                }
                break;
            case R.id.email_img:
                String email = mDataManager.getPreferencesManager().getUserProfileField(ConstantManager.USER_EMAIL_KEY);
                if (!email.equals("")) {
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_client)));
                }
                break;
            case R.id.vk_img:
                String vkAddress = mDataManager.getPreferencesManager().getUserProfileField(ConstantManager.USER_VK_KEY);
                if (!vkAddress.equals("")) {
                    Intent mVkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + vkAddress));
                    startActivity(Intent.createChooser(mVkIntent, getString(R.string.choose_browser)));
                }
                break;
            case R.id.git_img:
                String gitAddress = mDataManager.getPreferencesManager().getUserProfileField(ConstantManager.USER_GIT_KEY);
                if (!gitAddress.equals("")) {
                    Intent mGitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + gitAddress));
                    startActivity(Intent.createChooser(mGitIntent, getString(R.string.choose_browser)));
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ConstantManager.REQUEST_GALLERY_PHOTO:
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    insertProfileImage(selectedImage);
                    uploadPhoto(getFileFromUri(selectedImage));
                }
                break;
            case ConstantManager.REQUEST_CAMERA_PHOTO:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    Uri selectedImage = Uri.fromFile(mPhotoFile);
                    insertProfileImage(selectedImage);
                    uploadPhoto(mPhotoFile);
                }
                break;
            case ConstantManager.REQUEST_GALLERY_AVATAR:
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    insertAvatarImage(selectedImage);
                    setRoundedAvatar();
                    uploadAvatar(getFileFromUri(selectedImage));
                }
                break;
            case ConstantManager.REQUEST_CAMERA_AVATAR:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    Uri selectedImage = Uri.fromFile(mPhotoFile);
                    insertAvatarImage(selectedImage);
                    setRoundedAvatar();
                    uploadAvatar(mPhotoFile);
                }
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectPhotoItems = {
                        getString(R.string.user_profile_dialog_gallery),
                        getString(R.string.user_profile_dialog_camera),
                        getString(R.string.user_profile_dialog_cancel)
                };

                final AlertDialog.Builder photoBuilder = new AlertDialog.Builder(this);
                photoBuilder.setTitle(getString(R.string.user_profile_dialog_photo_title));
                photoBuilder.setItems(selectPhotoItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choiceItem) {
                        switch (choiceItem) {
                            case 0:
                                loadPhotoFromGallery();
                                break;
                            case 1:
                                loadPhotoFromCamera();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return photoBuilder.create();
            case ConstantManager.LOAD_PROFILE_AVATAR:
                String[] selectAvatarItems = {
                        getString(R.string.user_profile_dialog_gallery),
                        getString(R.string.user_profile_dialog_camera),
                        getString(R.string.user_profile_dialog_cancel)
                };

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.user_profile_dialog_avatar_title));
                builder.setItems(selectAvatarItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choiceItem) {
                        switch (choiceItem) {
                            case 0:
                                loadAvatarFromGallery();
                                break;
                            case 1:
                                loadAvatarFromCamera();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return builder.create();
            default:
                return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantManager.CAMERA_PERMISSION_REQUEST_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showToast(getString(R.string.need_camera_permission));
            }
            if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                showToast(getString(R.string.need_write_permission));
            }
        }
    }

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        mAppBarParams = (AppBarLayout.LayoutParams) mCollapsingToolbar.getLayoutParams();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawer() {
        View headerLayout = mNavigationView.getHeaderView(0);

        TextView userName = (TextView) headerLayout.findViewById(R.id.user_name_txt);
        TextView userEmail = (TextView) headerLayout.findViewById(R.id.user_email_txt);
        userName.setText(mDataManager.getPreferencesManager().getUserName());
        userEmail.setText(mDataManager.getPreferencesManager().getEmail());

        mNavigationView.setCheckedItem(R.id.user_profile_menu);

        CircleImageView avatarImg = (CircleImageView) headerLayout.findViewById(R.id.avatar);
        avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ConstantManager.LOAD_PROFILE_AVATAR);
            }
        });

        setRoundedAvatar();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.user_profile_menu:
                        mNavigationDrawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.team_menu:
                        Intent profileIntent = new Intent(MainActivity.this, UserListActivity.class);
                        startActivity(profileIntent);
                        mNavigationDrawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.exit_menu:
                        mDataManager.getPreferencesManager().saveAuthToken("");
                        Intent exitIntent = new Intent(MainActivity.this, AuthActivity.class);
                        startActivity(exitIntent);
                }
                return true;
            }
        });
    }

    private void setRoundedAvatar() {
        View headerLayout = mNavigationView.getHeaderView(0);
        CircleImageView avatarImg = (CircleImageView) headerLayout.findViewById(R.id.avatar);
        DataManager.getInstance().getPicasso()
                .load(mDataManager.getPreferencesManager().loadUserAvatar())
                .fit()
                .centerCrop()
                .error(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .into(avatarImg, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "Could not fetch avatar");
                    }
                });
    }

    private void setUserPhoto() {
        DataManager.getInstance().getPicasso()
                .load(mDataManager.getPreferencesManager().loadUserPhoto())
                .fit()
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .error(R.drawable.user_bg)
                .placeholder(R.drawable.user_bg)
                .into(mProfileImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "load from cache");
                    }

                    @Override
                    public void onError() {
                        DataManager.getInstance().getPicasso()
                                .load(mDataManager.getPreferencesManager().loadUserPhoto())
                                .fit()
                                .centerCrop()
                                .error(R.drawable.user_bg)
                                .placeholder(R.drawable.user_bg)
                                .into(mProfileImage, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.d(TAG, "Could not fetch image");
                                    }
                                });
                    }
                });
    }

    /**
     * Переключает режим редактирования
     *
     * @param mode 1 - редактирование, 0 - просмотр
     */
    private void changeEditMode(int mode) {
        if (mode == 1) {
            mFab.setImageResource(R.drawable.ic_done_white_24dp);
            mPhoneEt.setFocusableInTouchMode(true);

            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(true);
                userValue.setFocusable(true);
                userValue.setFocusableInTouchMode(true);
            }

            showProfilePlaceholder();
            lockToolbar();
            showEtErrors();
            mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        } else {
            mFab.setImageResource(R.drawable.ic_create_white_24dp);

            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(false);
                userValue.setFocusable(false);
                userValue.setFocusableInTouchMode(false);
            }

            hideProfilePlaceholder();
            unlockToolbar();
            hideEtErrors();
            mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.white));

            saveUserFields();
        }
    }

    private void hideProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.GONE);
    }

    private void showProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.VISIBLE);
    }

    private void lockToolbar() {
        //mAppBarLayout.setExpanded(true, true);
        mAppBarParams.setScrollFlags(0);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    private void unlockToolbar() {
        mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    private void hideEtErrors() {
        for (int i = 0; i < mUserInfoEdits.size(); ++i) {
            EditText editText = mUserInfoEdits.get(i);
            if (mTextWatchers != null)
                editText.removeTextChangedListener(mTextWatchers.get(i));
            ((TextInputLayout) editText.getParent()).setError(null);
            ((TextInputLayout) editText.getParent()).setErrorEnabled(false);
        }
        mTextWatchers = null;
    }

    private void showEtErrors() {
        mTextWatchers = new ArrayList<>();
        for (int i = 0; i < mUserInfoEdits.size(); ++i) {
            mTextWatchers.add(i,
                    new EditTextWatcher(
                            this,
                            mUserInfoEdits.get(i),
                            mUserInfoImages.get(i),
                            (TextInputLayout) mUserInfoEdits.get(i).getParent()
                    )
            );
            mUserInfoEdits.get(i).addTextChangedListener(mTextWatchers.get(i));
        }
    }

    private void initUserFields() {
        List<String> userFields = mDataManager.getPreferencesManager().loadUserProfileFields();
        for (int i = 0; i < userFields.size(); ++i) {
            mUserInfoViews.get(i).setText(userFields.get(i));
        }
    }

    private void saveUserFields() {
        List<String> userFields = new ArrayList<>();
        for (EditText userField : mUserInfoViews) {
            userFields.add(userField.getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileFields(userFields);
    }

    private void initUserInfoValues() {
        List<String> userValues = mDataManager.getPreferencesManager().loadUserProfileValues();
        for (int i = 0; i < userValues.size(); ++i) {
            mUserValueViews.get(i).setText(userValues.get(i));
        }
    }

    private void loadPhotoFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeGalleryIntent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(takeGalleryIntent, getString(R.string.user_profile_choose_photo)),
                    ConstantManager.REQUEST_GALLERY_PHOTO
            );
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_PERMISSION_REQUEST_CODE);
            Snackbar.make(mCoordinatorLayout, R.string.give_permission,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.permit, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.error_create_file));
            }
            if (mPhotoFile != null) {
                Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PHOTO);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_PERMISSION_REQUEST_CODE);
            Snackbar.make(mCoordinatorLayout, R.string.give_permission,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.permit, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    private void loadAvatarFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeGalleryIntent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(takeGalleryIntent, getString(R.string.user_profile_choose_photo)),
                    ConstantManager.REQUEST_GALLERY_AVATAR
            );
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_PERMISSION_REQUEST_CODE);
            Snackbar.make(mCoordinatorLayout, R.string.give_permission,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.permit, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    private void loadAvatarFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.error_create_file));
            }
            if (mPhotoFile != null) {
                Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_AVATAR);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_PERMISSION_REQUEST_CODE);
            Snackbar.make(mCoordinatorLayout, R.string.give_permission,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.permit, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    private void uploadPhoto(File file) {
        showProgress();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);
        Call<UploadPhotoRes> call = mDataManager.uploadPhoto(
                mDataManager.getPreferencesManager().getUserId(), body);
        call.enqueue(new Callback<UploadPhotoRes>() {
            @Override
            public void onResponse(Call<UploadPhotoRes> call, Response<UploadPhotoRes> response) {
                hideProgress();
                if (response.code() == 404) {
                    Intent loginIntent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivity(loginIntent);
                    MainActivity.this.finish();
                } else if (response.code() == 200) {
                    showSnackBar(getString(R.string.photo_uploaded_successfully));
                }
            }

            @Override
            public void onFailure(Call<UploadPhotoRes> call, Throwable t) {
                hideProgress();
                if (!NetworkStatusChecker.isNetworkAvailable(MainActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }

    private void uploadAvatar(File file) {
        showProgress();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
        Call<UploadPhotoRes> call = mDataManager.uploadAvatar(
                mDataManager.getPreferencesManager().getUserId(), body);
        call.enqueue(new Callback<UploadPhotoRes>() {
            @Override
            public void onResponse(Call<UploadPhotoRes> call, Response<UploadPhotoRes> response) {
                hideProgress();
                if (response.code() == 404) {
                    Intent loginIntent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivity(loginIntent);
                    MainActivity.this.finish();
                } else if (response.code() == 200) {
                    showSnackBar(getString(R.string.photo_uploaded_successfully));
                }
            }

            @Override
            public void onFailure(Call<UploadPhotoRes> call, Throwable t) {
                hideProgress();
                if (!NetworkStatusChecker.isNetworkAvailable(MainActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }

    private File getFileFromUri(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return new File(filePath);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    private void insertProfileImage(Uri selectedImage) {
        Picasso.with(this)
                .load(selectedImage)
                .into(mProfileImage);

        mDataManager.getPreferencesManager().saveUserPhoto(selectedImage);
    }

    private void insertAvatarImage(Uri selectedImage) {
        mDataManager.getPreferencesManager().saveUserAvatar(selectedImage);
    }

    private void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, ConstantManager.PERMISSION_REQUEST_SETTINGS_CODE);
    }
}