package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.ui.custom.CustomClickListener;
import com.softdesign.devintensive.ui.custom.RoundedDrawable;
import com.softdesign.devintensive.ui.fragments.UsersRetainedFragment;
import com.softdesign.devintensive.utils.ConstantManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListActivity extends BaseActivity{
    private static final String TAG = ConstantManager.TAG_PREFIX + "UserListActivity";

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;
    private List<User> mUsers;
    private UsersRetainedFragment mRetainedFragment;

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

        mDataManager = DataManager.getInstance();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        setupToolbar();
        setupDrawer();
        initUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUsersAdapter != null && mRetainedFragment != null) {
            mRetainedFragment.setUsers(mUsersAdapter.getUsers());
            mRetainedFragment.setFilteredUsers(mUsersAdapter.getFilteredUsers());
        }
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
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mUsersAdapter != null) {
                    mUsersAdapter.getFilter().filter(newText);
                }
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

    private void loadUsersFromDb() {
        mUsers = mDataManager.getUserListFromDb();
        if (mUsers.size() == 0){
            showSnackBar(getString(R.string.error_load_users_list));
        } else {
            mUsersAdapter = new UsersAdapter(mUsers, new CustomClickListener() {
                @Override
                public void onUserItemClickListener(int position) {
                    UserDTO userDTO = new UserDTO(mUsers.get(position));
                    Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                    profileIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);
                    startActivity(profileIntent);
                }
            });
            mRecyclerView.setAdapter(mUsersAdapter);
            mRetainedFragment.setUsers(mUsersAdapter.getUsers());
            mRetainedFragment.setFilteredUsers(mUsersAdapter.getFilteredUsers());
        }
    }

    private void initUsers() {
        FragmentManager fm = getFragmentManager();
        mRetainedFragment = (UsersRetainedFragment) fm.findFragmentByTag("users_data");
        if (mRetainedFragment == null) {
            mRetainedFragment = new UsersRetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, "users_data").commit();
            loadUsersFromDb();
        } else {
            mUsers = mRetainedFragment.getUsers();
            mUsersAdapter = new UsersAdapter(mUsers, new CustomClickListener() {
                @Override
                public void onUserItemClickListener(int position) {
                    UserDTO userDTO = new UserDTO(mUsers.get(position));
                    Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                    profileIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);
                    startActivity(profileIntent);
                }
            });
            mRecyclerView.setAdapter(mUsersAdapter);
            mUsersAdapter.setFilteredUsers(mRetainedFragment.getFilteredUsers());
        }
    }
}