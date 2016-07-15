package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.RepositoriesAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileUserActivity extends BaseActivity {

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;
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

        setupToolbar();
        initProfileData();
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
                String gitAddress = (String)repositoriesAdapter.getItem(position);
                if (gitAddress.contains("http://")){
                    gitAddress = gitAddress.replaceAll("http://", "");
                }
                if (gitAddress.contains("https://")){
                    gitAddress = gitAddress.replaceAll("https://", "");
                }
                if (!gitAddress.equals("")){
                    Intent mGitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + gitAddress));
                    startActivity(Intent.createChooser(mGitIntent, getString(R.string.choose_browser)));
                }
            }
        });

        Log.d("", 76 + "");

        mUserBio.setText(userDTO.getBio());
        mUserRating.setText(userDTO.getRating());
        mUserCodeLines.setText(userDTO.getCodeLines());
        mUserProjects.setText(userDTO.getProjects());

        mCollapsingToolbar.setTitle(userDTO.getFullName());

        String photoPath = userDTO.getPhoto();
        if (!photoPath.isEmpty()) {
            Picasso.with(this)
                    .load(photoPath)
                    .placeholder(R.drawable.user_bg)
                    .error(R.drawable.user_bg)
                    .into(mProfileImage);
        }

        setMaxHeightOfListView(mRepoListView);
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
}
