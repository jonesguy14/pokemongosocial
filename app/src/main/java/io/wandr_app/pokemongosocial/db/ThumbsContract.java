package io.wandr_app.pokemongosocial.db;

import android.provider.BaseColumns;

public final class ThumbsContract {
    public ThumbsContract() {}
    public static abstract class PostThumbsEntry implements BaseColumns {
        public static final String TABLE_NAME = "postThumbs";
        public static final String COLUMN_NAME_POST_ID = "postId";
        public static final String COLUMN_NAME_THUMBS_STATUS = "thumbsStatus";
    }
    public static abstract class CommentThumbsEntry implements BaseColumns {
        public static final String TABLE_NAME = "commentThumbs";
        public static final String COLUMN_NAME_COMMENT_ID = "commentId";
        public static final String COLUMN_NAME_THUMBS_STATUS = "thumbsStatus";
    }
}
