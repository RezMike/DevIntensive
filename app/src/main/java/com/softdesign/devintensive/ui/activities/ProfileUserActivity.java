package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.data.network.responses.UserLikeRes;
import com.softdesign.devintensive.data.storage.models.Like;
import com.softdesign.devintensive.data.storage.models.LikeDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.ui.adapters.RepositoriesAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileUserActivity extends BaseActivity implements View.OnClickListener {
    DataManager mDataManager;
    User mUser;

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.bio_et)
    EditText mUserBio;
    @BindView(R.id.user_info_rating)
    TextView mUserRating;
    @BindView(R.id.user_info_lines)
    TextView mUserCodeLines;
    @BindView(R.id.user_info_projects)
    TextView mUserProjects;
    @BindView(R.id.repositories_list)
    ListView mRepoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        mFab.setOnClickListener(this);

        setupToolbar();
        initProfileData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (isUserLiked()) {
                    unlikeUser();
                } else {
                    likeUser();
                }
                break;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initProfileData() {
        UserDTO userDTO = getIntent().getParcelableExtra(ConstantManager.PARCELABLE_KEY);

        final List<String> repositories = userDTO.getRepositories();
        final RepositoriesAdapter repositoriesAdapter = new RepositoriesAdapter(this, repositories);
        mRepoListView.setAdapter(repositoriesAdapter);

        mRepoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String gitAddress = (String) repositoriesAdapter.getItem(position);
                if (gitAddress.contains("http://")) {
                    gitAddress = gitAddress.replaceAll("http://", "");
                }
                if (gitAddress.contains("https://")) {
                    gitAddress = gitAddress.replaceAll("https://", "");
                }
                if (!gitAddress.equals("")) {
                    Intent mGitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + gitAddress));
                    startActivity(Intent.createChooser(mGitIntent, getString(R.string.choose_browser)));
                }
            }
        });

        mUser = mDataManager.getDaoSession().queryBuilder(User.class)
                .where(UserDao.Properties.RemoteId.eq(userDTO.getRemoteId())).build().unique();
        if (isUserLiked()) {
            mFab.setImageResource(R.drawable.like_active_white_24dp);
        } else {
            mFab.setImageResource(R.drawable.like_inactive_white_24dp);
        }
        mUserBio.setText(userDTO.getBio());
        mUserRating.setText(userDTO.getRating());
        mUserCodeLines.setText(userDTO.getCodeLines());
        mUserProjects.setText(userDTO.getProjects());

        mCollapsingToolbar.setTitle(userDTO.getFullName());

        String photoPath = userDTO.getPhoto();
        if (photoPath.isEmpty()) {
            photoPath = "null";
        }
        Picasso.with(this)
                .load(photoPath)
                .placeholder(R.drawable.user_bg)
                .error(R.drawable.user_bg)
                .into(mProfileImage);

        setMaxHeightOfListView(mRepoListView);
    }

    private void showSnackBar(String massage) {
        Snackbar.make(mCoordinatorLayout, massage, Snackbar.LENGTH_LONG).show();
    }

    public static void setMaxHeightOfListView(ListView listView) {
        ListAdapter adapter = listView.getAdapter();

        View view = adapter.getView(0, null, listView);
        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        int totalHeight = view.getMeasuredHeight() * adapter.getCount();

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + listView.getDividerHeight() * (adapter.getCount() - 1);
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private boolean isUserLiked() {
        List<Like> likes = mUser.getLikes();
        String userId = mDataManager.getPreferencesManager().getUserId();
        for (Like like : likes) {
            if (like.getLikedUserId().equals(userId)) return true;
        }
        return false;
    }

    private void likeUser() {
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            showSnackBar(getString(R.string.error_network_not_available));
            return;
        }
        Call<UserLikeRes> call = mDataManager.likeUser(mUser.getRemoteId());
        call.enqueue(new Callback<UserLikeRes>() {
            @Override
            public void onResponse(Call<UserLikeRes> call, Response<UserLikeRes> response) {
                if (response.code() == 200) {
                    UserInfoRes.ProfileValues data = response.body().getData();
                    mUser.setRating(data.getFullRating());
                    mUser.setCodeLines(data.getLinesCode());
                    mUser.setProjects(data.getProjects());

                    mDataManager.getDaoSession().getLikeDao().insert(
                            new Like(mDataManager.getPreferencesManager().getUserId(), mUser.getRemoteId())
                    );
                    mUser.resetLikes();
                    mFab.setImageResource(R.drawable.like_active_white_24dp);
                } else if (response.code() == 404) {
                    showSnackBar(getString(R.string.error_wrong_login_or_password));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserLikeRes> call, Throwable t) {
                if (!NetworkStatusChecker.isNetworkAvailable(ProfileUserActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }

    private void unlikeUser() {
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            showSnackBar(getString(R.string.error_network_not_available));
            return;
        }
        Call<UserLikeRes> call = mDataManager.unlikeUser(mUser.getRemoteId());
        call.enqueue(new Callback<UserLikeRes>() {
            @Override
            public void onResponse(Call<UserLikeRes> call, Response<UserLikeRes> response) {
                if (response.code() == 200) {
                    UserInfoRes.ProfileValues data = response.body().getData();
                    mUser.setRating(data.getFullRating());
                    mUser.setCodeLines(data.getLinesCode());
                    mUser.setProjects(data.getProjects());

                    List<Like> likes = mDataManager.getDaoSession().queryBuilder(Like.class).where(
                            LikeDao.Properties.UserRemoteId.eq(mUser.getRemoteId()),
                            LikeDao.Properties.LikedUserId.eq(mDataManager.getPreferencesManager().getUserId())
                    ).list();
                    for (Like like : likes) {
                        like.delete();
                    }
                    mUser.resetLikes();
                    mFab.setImageResource(R.drawable.like_inactive_white_24dp);
                } else if (response.code() == 404) {
                    showSnackBar(getString(R.string.error_wrong_login_or_password));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserLikeRes> call, Throwable t) {
                if (!NetworkStatusChecker.isNetworkAvailable(ProfileUserActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }
}
