package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.responses.UserListRes;
import com.softdesign.devintensive.ui.custom.CustomClickListener;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable{
    Context mContext;
    List<UserListRes.Data> mUsers;
    List<UserListRes.Data> mFilteredUsers;
    CustomClickListener mCustomClickListener;
    UserFilter mFilter;

    public UsersAdapter(List<UserListRes.Data> users, CustomClickListener customClickListener) {
        mUsers = users;
        mFilteredUsers = new ArrayList<>();
        mFilteredUsers.addAll(users);
        mCustomClickListener = customClickListener;
        mFilter = new UserFilter(UsersAdapter.this);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user_list, parent, false);
        return new UserViewHolder(convertView, mCustomClickListener);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        UserListRes.Data user = mFilteredUsers.get(position);

        String photoPath = user.getPublicInfo().getPhoto();
        if (!photoPath.isEmpty()) {
            WindowManager manager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
            int width = manager.getDefaultDisplay().getWidth();
            Picasso.with(mContext)
                    .load(photoPath)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(width, (int)(width/1.78))
                    .centerCrop()
                    .placeholder(R.drawable.user_bg)
                    .error(R.drawable.user_bg)
                    .into(holder.userPhoto);
        }

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(String.valueOf(user.getProfileValues().getRating()));
        holder.mCodeLines.setText(String.valueOf(user.getProfileValues().getLinesCode()));
        holder.mProjects.setText(String.valueOf(user.getProfileValues().getProjects()));

        if (user.getPublicInfo().getBio() == null || user.getPublicInfo().getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getPublicInfo().getBio());
        }
    }

    @Override
    public int getItemCount() {
        return mFilteredUsers.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        protected AspectRatioImageView userPhoto;
        protected TextView mFullName, mRating, mCodeLines, mProjects, mBio;
        protected Button mShowMore;

        private CustomClickListener mListener;

        public UserViewHolder(View itemView, CustomClickListener customClickListener) {
            super(itemView);
            mListener = customClickListener;

            userPhoto = (AspectRatioImageView) itemView.findViewById(R.id.user_photo_img);
            mFullName = (TextView) itemView.findViewById(R.id.user_full_name_tv);
            mRating = (TextView) itemView.findViewById(R.id.user_rating_tv);
            mCodeLines = (TextView) itemView.findViewById(R.id.user_code_lines_tv);
            mProjects = (TextView) itemView.findViewById(R.id.user_projects_tv);
            mBio = (TextView) itemView.findViewById(R.id.user_bio_tv);
            mShowMore = (Button) itemView.findViewById(R.id.more_info_btn);

            mShowMore.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null){
                mListener.onUserItemClickListener(mUsers.indexOf(mFilteredUsers.get(getAdapterPosition())));
            }
        }
    }

    public List<UserListRes.Data> getUsers() {
        return mUsers;
    }

    public List<UserListRes.Data> getFilteredUsers() {
        return mFilteredUsers;
    }

    public void setUsers(List<UserListRes.Data> users) {
        mUsers = users;
    }

    public void setFilteredUsers(List<UserListRes.Data> filteredUsers) {
        mFilteredUsers = filteredUsers;
    }

    public class UserFilter extends Filter{
        private UsersAdapter mAdapter;

        public UserFilter(UsersAdapter adapter) {
            super();
            mAdapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            mFilteredUsers.clear();
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) {
                mFilteredUsers.addAll(mUsers);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final UserListRes.Data user : mUsers) {
                    if (user.getFullName().toLowerCase().startsWith(filterPattern)) {
                        mFilteredUsers.add(user);
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
