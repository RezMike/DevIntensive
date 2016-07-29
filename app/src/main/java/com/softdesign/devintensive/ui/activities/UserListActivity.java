package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.responses.UserInfoRes;
import com.softdesign.devintensive.data.network.responses.UserLikeRes;
import com.softdesign.devintensive.data.storage.LoadUserDataOperation;
import com.softdesign.devintensive.data.storage.models.Like;
import com.softdesign.devintensive.data.storage.models.LikeDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.ui.custom.CustomClickListener;
import com.softdesign.devintensive.ui.custom.RoundedDrawable;
import com.softdesign.devintensive.ui.fragments.SearchRetainedFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ItemTouchHelperCallback;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends BaseActivity {
    private static final String TAG = ConstantManager.TAG_PREFIX + "UserListActivity";

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;
    private List<User> mUsers;
    private ChronosConnector mConnector;
    private SearchRetainedFragment mRetainedFragment;
    private Parcelable mSavedState;

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.user_list)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);
        mConnector = new ChronosConnector();
        mConnector.onCreate(this, savedInstanceState);

        mDataManager = DataManager.getInstance();

        setupToolbar();
        setupDrawer();
        if (savedInstanceState != null) {
            mSavedState = savedInstanceState.getParcelable(ConstantManager.SAVED_STATE_KEY);
        }
        loadUsersFromDb();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnector.onResume();
        if (mUsersAdapter != null) {
            mUsersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConnector.onPause();
    }

    @Override
    protected void onStop() {
        if (mUsersAdapter != null && mUsersAdapter.getRemovalUsers() != null) {
            List<User> removalUsers = mUsersAdapter.getRemovalUsers();
            for (User user : removalUsers) {
                user.delete();
            }
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.enter_user_name));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mUsersAdapter != null) {
                    mUsersAdapter.getFilter().filter(query);
                }
                mRetainedFragment.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mUsersAdapter != null) {
                    mUsersAdapter.getFilter().filter(newText);
                }
                mRetainedFragment.setSearchQuery(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mConnector.onSaveInstanceState(outState);
        outState.putParcelable(ConstantManager.SAVED_STATE_KEY,
                mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    private void loadUsersFromDb() {
        try {
            showProgress();
            mConnector.runOperation(new LoadUserDataOperation(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Вызывается после загрузки данных из базы данных
     *
     * @param result - результат выполнения
     */
    public void onOperationFinished(final LoadUserDataOperation.Result result) {
        mUsers = result.getOutput();
        if (mUsers.size() == 0) {
            showSnackBar(getString(R.string.error_load_users_list));
        } else {
            mUsersAdapter = new UsersAdapter(mUsers, new CustomClickListener() {
                @Override
                public void onUserItemClickListener(String action, int position) {
                    if (action.equals(ConstantManager.START_PROFILE_ACTIVITY_KEY)) {
                        UserDTO userDTO = new UserDTO(mUsers.get(position));
                        Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                        profileIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);
                        startActivity(profileIntent);
                    } else if (action.equals(ConstantManager.LIKE_USER_KEY)) {
                        likeUser(position);
                    } else if (action.equals(ConstantManager.UNLIKE_USER_KEY)) {
                        unlikeUser(position);
                    }
                }
            });
            mRecyclerView.setAdapter(mUsersAdapter);
        }
        hideProgress();
        setupAdapters();
        loadSearchQuery();
    }

    private void setupAdapters() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mUsersAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedState);
    }

    /**
     * Загрузка запроса на поиск в списке после поворота экрана при поиске
     */
    private void loadSearchQuery() {
        FragmentManager fm = getFragmentManager();
        mRetainedFragment = (SearchRetainedFragment) fm.findFragmentByTag(ConstantManager.RETAINED_FRAGMENT_KEY);
        if (mRetainedFragment == null) {
            mRetainedFragment = new SearchRetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, ConstantManager.RETAINED_FRAGMENT_KEY).commit();
        } else {
            String searchQuery = mRetainedFragment.getSearchQuery();
            if (searchQuery != null && !searchQuery.isEmpty()) {
                mUsersAdapter.getFilter().filter(searchQuery);
            }
        }
    }

    private void showSnackBar(String massage) {
        Snackbar.make(mCoordinatorLayout, massage, Snackbar.LENGTH_LONG).show();
    }

    private void setupToolbar() {
        mToolbar.setTitle(getString(R.string.team));

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

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

        setRoundedAvatar();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.team_menu:
                        mNavigationDrawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.user_profile_menu:
                        Intent profileIntent = new Intent(UserListActivity.this, MainActivity.class);
                        startActivity(profileIntent);
                        mNavigationDrawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.exit_menu:
                        mDataManager.getPreferencesManager().saveAuthToken("");
                        Intent exitIntent = new Intent(UserListActivity.this, AuthActivity.class);
                        startActivity(exitIntent);
                }
                return true;
            }
        });
    }

    private void setRoundedAvatar() {
        View headerLayout = mNavigationView.getHeaderView(0);
        ImageView avatarImg = (ImageView) headerLayout.findViewById(R.id.avatar);
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.avatar);
        RoundedDrawable roundedDrawable = new RoundedDrawable(bitmap);
        avatarImg.setImageDrawable(roundedDrawable);
    }

    private void likeUser(final int position) {
        final User user = mUsers.get(position);
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            showSnackBar(getString(R.string.error_network_not_available));
            return;
        }
        Call<UserLikeRes> call = mDataManager.likeUser(user.getRemoteId());
        call.enqueue(new Callback<UserLikeRes>() {
            @Override
            public void onResponse(Call<UserLikeRes> call, Response<UserLikeRes> response) {
                if (response.code() == 200) {
                    UserInfoRes.ProfileValues data = response.body().getData();
                    user.setRating(data.getFullRating());
                    user.setCodeLines(data.getLinesCode());
                    user.setProjects(data.getProjects());

                    mDataManager.getDaoSession().getLikeDao().insert(
                            new Like(mDataManager.getPreferencesManager().getUserId(), user.getRemoteId())
                    );
                    user.resetLikes();

                    mUsersAdapter.notifyItemChanged(position);
                } else if (response.code() == 404) {
                    showSnackBar(getString(R.string.error_wrong_login_or_password));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserLikeRes> call, Throwable t) {
                if (!NetworkStatusChecker.isNetworkAvailable(UserListActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }

    private void unlikeUser(final int position) {
        final User user = mUsers.get(position);
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            showSnackBar(getString(R.string.error_network_not_available));
            return;
        }
        Call<UserLikeRes> call = mDataManager.unlikeUser(user.getRemoteId());
        call.enqueue(new Callback<UserLikeRes>() {
            @Override
            public void onResponse(Call<UserLikeRes> call, Response<UserLikeRes> response) {
                if (response.code() == 200) {
                    UserInfoRes.ProfileValues data = response.body().getData();
                    user.setRating(data.getFullRating());
                    user.setCodeLines(data.getLinesCode());
                    user.setProjects(data.getProjects());

                    List<Like> likes = mDataManager.getDaoSession().queryBuilder(Like.class).where(
                            LikeDao.Properties.UserRemoteId.eq(user.getRemoteId()),
                            LikeDao.Properties.LikedUserId.eq(mDataManager.getPreferencesManager().getUserId())
                    ).list();
                    for (Like like : likes) {
                        like.delete();
                    }
                    user.resetLikes();

                    mUsersAdapter.notifyItemChanged(position);
                } else if (response.code() == 404) {
                    showSnackBar(getString(R.string.error_wrong_login_or_password));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }

            @Override
            public void onFailure(Call<UserLikeRes> call, Throwable t) {
                if (!NetworkStatusChecker.isNetworkAvailable(UserListActivity.this)) {
                    showSnackBar(getString(R.string.error_network_not_available));
                } else {
                    showSnackBar(getString(R.string.error_all_bad));
                }
            }
        });
    }
}