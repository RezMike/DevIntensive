package com.softdesign.devintensive.data.storage.models;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "LIKES".
*/
public class LikeDao extends AbstractDao<Like, Long> {

    public static final String TABLENAME = "LIKES";

    /**
     * Properties of entity Like.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property UserRemoteId = new Property(1, String.class, "userRemoteId", false, "USER_REMOTE_ID");
        public final static Property LikedUserId = new Property(2, String.class, "likedUserId", false, "LIKED_USER_ID");
    };

    private DaoSession daoSession;

    private Query<Like> user_LikesQuery;

    public LikeDao(DaoConfig config) {
        super(config);
    }
    
    public LikeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"LIKES\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"USER_REMOTE_ID\" TEXT," + // 1: userRemoteId
                "\"LIKED_USER_ID\" TEXT);"); // 2: likedUserId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LIKES\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Like entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String userRemoteId = entity.getUserRemoteId();
        if (userRemoteId != null) {
            stmt.bindString(2, userRemoteId);
        }
 
        String likedUserId = entity.getLikedUserId();
        if (likedUserId != null) {
            stmt.bindString(3, likedUserId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Like entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String userRemoteId = entity.getUserRemoteId();
        if (userRemoteId != null) {
            stmt.bindString(2, userRemoteId);
        }
 
        String likedUserId = entity.getLikedUserId();
        if (likedUserId != null) {
            stmt.bindString(3, likedUserId);
        }
    }

    @Override
    protected final void attachEntity(Like entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Like readEntity(Cursor cursor, int offset) {
        Like entity = new Like( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // userRemoteId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // likedUserId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Like entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUserRemoteId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setLikedUserId(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Like entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Like entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "likes" to-many relationship of User. */
    public List<Like> _queryUser_Likes(String userRemoteId) {
        synchronized (this) {
            if (user_LikesQuery == null) {
                QueryBuilder<Like> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.UserRemoteId.eq(null));
                user_LikesQuery = queryBuilder.build();
            }
        }
        Query<Like> query = user_LikesQuery.forCurrentThread();
        query.setParameter(0, userRemoteId);
        return query.list();
    }

}
