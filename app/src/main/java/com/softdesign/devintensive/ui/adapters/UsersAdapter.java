package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.Like;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.ui.custom.CustomClickListener;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.DevintensiveApplication;
import com.softdesign.devintensive.utils.ItemTouchHelperAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>
        implements Filterable, ItemTouchHelperAdapter {
    private static final String TAG = ConstantManager.TAG_PREFIX + "UsersAdapter";
    private Context mContext;
    private List<User> mUsers;
    private List<User> mFilteredUsers;
    private List<User> mRemovalUsers;
    private CustomClickListener mCustomClickListener;
    private UserFilter mFilter;
    private DaoSession mDaoSession;

    public UsersAdapter(List<User> users, CustomClickListener customClickListener) {
        mUsers = users;
        mFilteredUsers = new ArrayList<>();
        mFilteredUsers.addAll(users);
        mRemovalUsers = new ArrayList<>();
        mCustomClickListener = customClickListener;
        mFilter = new UserFilter(UsersAdapter.this);
        mDaoSession = DevintensiveApplication.getDaoSession();
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user_list, parent, false);
        return new UserViewHolder(convertView, mCustomClickListener);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        final User user = mFilteredUsers.get(position);
        final String userPhoto;

        if (mRemovalUsers.contains(user)) {
            holder.mNotDeletedLayout.setVisibility(View.GONE);
            holder.mDeletedLayout.setVisibility(View.VISIBLE);

            holder.mDeleted.setText(String.format(mContext.getString(R.string.user_deleted_from_list),
                    mFilteredUsers.get(position).getFullName()));
            holder.mRevert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRemovalUsers.remove(user);
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        } else {
            holder.mNotDeletedLayout.setVisibility(View.VISIBLE);
            holder.mDeletedLayout.setVisibility(View.GONE);

            if (user.getPhoto().isEmpty()) {
                userPhoto = "null";
                Log.e(TAG, "onBindViewHolder: user with name " + user.getFullName() + " has no photo");
            } else {
                userPhoto = user.getPhoto();
            }

            DataManager.getInstance().getPicasso()
                    .load(userPhoto)
                    .fit()
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .error(holder.mDummy)
                    .placeholder(holder.mDummy)
                    .into(holder.userPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "load from cache");
                        }

                        @Override
                        public void onError() {
                            DataManager.getInstance().getPicasso()
                                    .load(userPhoto)
                                    .fit()
                                    .centerCrop()
                                    .error(holder.mDummy)
                                    .placeholder(holder.mDummy)
                                    .into(holder.userPhoto, new Callback() {
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

            holder.mFullName.setText(user.getFullName());
            holder.mRating.setText(String.valueOf(user.getFullRating()));
            holder.mCodeLines.setText(String.valueOf(user.getCodeLines()));
            holder.mProjects.setText(String.valueOf(user.getProjects()));
            holder.mLikesNumber.setText(String.valueOf(user.getLikes().size()));

            if (isUserLiked(user)) {
                holder.mLikesImg.setImageResource(R.drawable.like_active_orange_24dp);
            } else {
                holder.mLikesImg.setImageResource(R.drawable.like_inactive_orange_24dp);
            }

            if (user.getBio() == null || user.getBio().isEmpty()) {
                holder.mBio.setVisibility(View.GONE);
            } else {
                holder.mBio.setVisibility(View.VISIBLE);
                holder.mBio.setText(user.getBio());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFilteredUsers.size();
    }

    @Override
    public void onItemDismiss(int position) {
        if (mUsers.size() != mFilteredUsers.size()) return;
        User user = mUsers.get(position);
        if (mRemovalUsers.contains(user)) {
            mUsers.remove(user);
            mRemovalUsers.remove(user);
            mFilteredUsers.remove(user);
            mDaoSession.delete(user);
            notifyItemRemoved(position);
        } else {
            mRemovalUsers.add(user);
            notifyItemChanged(position);
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (mUsers.size() != mFilteredUsers.size()) return;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; ++i) {
                Collections.swap(mUsers, i, i + 1);
                Collections.swap(mFilteredUsers, i, i + 1);
                changeUsersInOrder(mUsers.get(i), mUsers.get(i + 1));
            }
        } else {
            for (int i = fromPosition; i > toPosition; --i) {
                Collections.swap(mUsers, i, i - 1);
                Collections.swap(mFilteredUsers, i, i - 1);
                changeUsersInOrder(mUsers.get(i), mUsers.get(i - 1));
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    private void changeUsersInOrder(User user1, User user2) {
        long tempIndex = user1.getIndex();
        user1.setIndex(user2.getIndex());
        user2.setIndex(tempIndex);
        user1.update();
        user2.update();
    }

    private boolean isUserLiked(User user) {
        List<Like> likes = user.getLikes();
        String userId = DataManager.getInstance().getPreferencesManager().getUserId();
        for (Like like : likes) {
            if (like.getLikedUserId().equals(userId)) return true;
        }
        return false;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public List<User> getRemovalUsers() {
        return mRemovalUsers;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected AspectRatioImageView userPhoto;

        protected LinearLayout mDeletedLayout, mNotDeletedLayout;
        protected LinearLayout mLikesLayout;
        protected TextView mFullName, mRating, mCodeLines, mProjects, mBio, mDeleted, mRevert, mLikesNumber;
        protected ImageView mLikesImg;
        protected Button mShowMore;
        protected Drawable mDummy;

        private CustomClickListener mListener;

        public UserViewHolder(View itemView, CustomClickListener customClickListener) {
            super(itemView);
            mListener = customClickListener;

            userPhoto = (AspectRatioImageView) itemView.findViewById(R.id.user_photo_img);
            mDeletedLayout = (LinearLayout) itemView.findViewById(R.id.user_deleted_layout);
            mNotDeletedLayout = (LinearLayout) itemView.findViewById(R.id.user_not_deleted_layout);
            mLikesLayout = (LinearLayout) itemView.findViewById(R.id.likes_layout);
            mFullName = (TextView) itemView.findViewById(R.id.user_full_name_tv);
            mRating = (TextView) itemView.findViewById(R.id.user_rating_tv);
            mCodeLines = (TextView) itemView.findViewById(R.id.user_code_lines_tv);
            mProjects = (TextView) itemView.findViewById(R.id.user_projects_tv);
            mBio = (TextView) itemView.findViewById(R.id.user_bio_tv);
            mDeleted = (TextView) itemView.findViewById(R.id.user_deleted_tv);
            mRevert = (TextView) itemView.findViewById(R.id.user_deleted_revert_tv);
            mLikesNumber = (TextView) itemView.findViewById(R.id.likes_number);
            mLikesImg = (ImageView) itemView.findViewById(R.id.likes_img);
            mShowMore = (Button) itemView.findViewById(R.id.more_info_btn);

            mLikesLayout.setOnClickListener(this);
            mShowMore.setOnClickListener(this);
            mDummy = userPhoto.getContext().getResources().getDrawable(R.drawable.user_bg);
        }

        @Override
        public void onClick(View v) {
            if (mListener == null) return;

            int position = mUsers.indexOf(mFilteredUsers.get(getAdapterPosition()));
            switch (v.getId()) {
                case R.id.more_info_btn:
                    mListener.onUserItemClickListener(ConstantManager.START_PROFILE_ACTIVITY_KEY, position);
                    break;
                case R.id.likes_layout:
                    String action;
                    if (isUserLiked(mFilteredUsers.get(getAdapterPosition()))) {
                        action = ConstantManager.UNLIKE_USER_KEY;
                    } else {
                        action = ConstantManager.LIKE_USER_KEY;
                    }
                    mListener.onUserItemClickListener(action, position);
                    break;
            }
        }
    }

    public class UserFilter extends Filter {
        private UsersAdapter mAdapter;

        public UserFilter(UsersAdapter adapter) {
            super();
            mAdapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            mFilteredUsers.clear();
            FilterResults results = new FilterResults();
            if (constraint.length() == 0) {
                mFilteredUsers.addAll(mUsers);
            } else {
                String filterString = constraint.toString().toLowerCase().trim();
                if (filterString.contains(" ")) {
                    int spaceIndex = filterString.indexOf(" ");
                    String filterPattern1 = filterString.substring(0, spaceIndex - 1);
                    String filterPattern2 = filterString.substring(spaceIndex + 1, filterString.length());
                    for (User user : mUsers) {
                        String userName = user.getFullName().toLowerCase();
                        if (userName.contains(filterPattern1)
                                && userName.contains(filterPattern2)) {
                            mFilteredUsers.add(user);
                        }
                    }
                } else {
                    for (User user : mUsers) {
                        if (user.getFullName().toLowerCase().contains(filterString)) {
                            mFilteredUsers.add(user);
                        }
                    }
                }
            }
            results.values = mFilteredUsers;
            results.count = mFilteredUsers.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
