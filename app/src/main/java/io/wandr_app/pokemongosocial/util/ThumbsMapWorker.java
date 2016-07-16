package io.wandr_app.pokemongosocial.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

import io.wandr_app.pokemongosocial.db.CommentsThumbsDbHelper;
import io.wandr_app.pokemongosocial.db.PostThumbsDbHelper;
import io.wandr_app.pokemongosocial.db.ThumbsContract;

/**
 * Created by kylel on 7/16/2016.
 */
public class ThumbsMapWorker {
    private static final String TAG = "ThumbsMapWorker";
    private PostThumbsDbHelper postHelper;
    private CommentsThumbsDbHelper commentHelper;

    public ThumbsMapWorker(Context context) {
        postHelper = new PostThumbsDbHelper(context);
        commentHelper = new CommentsThumbsDbHelper(context);
    }

    /**
     * Records if a post has been thumbed up or down, so that repeated voting is stopped.
     *
     * @param post_id      the post that is liked
     * @param thumbsString "UP", "DOWN", "NONE" based on thumb
     */
    public void recordPostThumbs(int post_id, String thumbsString, Map<Integer, Integer>
            postThumbsMap) {
        if (thumbsString.equals("NONE")) {
            postThumbsMap.remove(post_id);
            removePostThumbsEntry(post_id);
            return;
        }
        postThumbsMap.put(post_id, thumbsStringToInt(thumbsString));

        SQLiteDatabase db = postHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ThumbsContract.PostThumbsEntry.COLUMN_NAME_POST_ID, post_id);
        values.put(ThumbsContract.PostThumbsEntry.COLUMN_NAME_THUMBS_STATUS, thumbsStringToInt
                (thumbsString));
        db.insert(ThumbsContract.PostThumbsEntry.TABLE_NAME, null, values);
    }

    private void removePostThumbsEntry(int post_id) {
        SQLiteDatabase db = postHelper.getWritableDatabase();
        db.delete(ThumbsContract.PostThumbsEntry.TABLE_NAME, ThumbsContract
                .PostThumbsEntry.COLUMN_NAME_POST_ID + "=?", new String[]{String.valueOf
                (post_id)});
    }

    /**
     * Records if a comment has been thumbed up or down, so that repeated voting is stopped.
     *
     * @param action_id    the comment that is liked
     * @param thumbsString "UP", "DOWN", "NONE based on thumb
     */
    public void recordCommentThumbs(int action_id, String thumbsString, Map<Integer, Integer>
            commentThumbsMap) {
        if (thumbsString.equals("NONE")) {
            commentThumbsMap.remove(action_id);
            removeCommentThumbsEntry(action_id);
            return;
        }
        commentThumbsMap.put(action_id, thumbsStringToInt(thumbsString));

        SQLiteDatabase db = commentHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ThumbsContract.CommentThumbsEntry.COLUMN_NAME_COMMENT_ID, action_id);
        values.put(ThumbsContract.CommentThumbsEntry.COLUMN_NAME_THUMBS_STATUS, thumbsStringToInt
                (thumbsString));
        db.insert(ThumbsContract.CommentThumbsEntry.TABLE_NAME, null, values);
    }

    private void removeCommentThumbsEntry(int action_id) {
        SQLiteDatabase db = commentHelper.getWritableDatabase();
        db.delete(ThumbsContract.CommentThumbsEntry.TABLE_NAME, ThumbsContract
                .CommentThumbsEntry.COLUMN_NAME_COMMENT_ID + "=?", new String[]{String.valueOf
                (action_id)});
    }

    private int thumbsStringToInt(String thumbsString) {
        switch (thumbsString) {
            case "UP":
                return 1;
            case "DOWN":
                return -1;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Make the hash map that says which posts have been thumbed already.
     */
    public Map<Integer, Integer> loadPostThumbsMap() {
        Map<Integer, Integer> postThumbsMap = new HashMap<>();
        SQLiteDatabase db = postHelper.getReadableDatabase();
        String[] projection = {
                ThumbsContract.PostThumbsEntry.COLUMN_NAME_POST_ID,
                ThumbsContract.PostThumbsEntry.COLUMN_NAME_THUMBS_STATUS
        };
        try (Cursor c = db.query(ThumbsContract.PostThumbsEntry.TABLE_NAME, projection, null, null,
                null,
                null, null)) {
            while (c.moveToNext()) {
                postThumbsMap.put(c.getInt(c.getColumnIndexOrThrow(ThumbsContract.PostThumbsEntry
                        .COLUMN_NAME_POST_ID)), c.getInt(c.getColumnIndexOrThrow(ThumbsContract.PostThumbsEntry.COLUMN_NAME_THUMBS_STATUS)));
            }
        }
        return postThumbsMap;
    }

    /**
     * Make the hash map that says which comments have been thumbed already
     */
    public Map<Integer, Integer> loadCommentThumbsMap() {
        Map<Integer, Integer> commentThumbsMap = new HashMap<>();
        SQLiteDatabase db = commentHelper.getReadableDatabase();
        String[] projection = {
                ThumbsContract.CommentThumbsEntry.COLUMN_NAME_COMMENT_ID,
                ThumbsContract.CommentThumbsEntry.COLUMN_NAME_THUMBS_STATUS
        };
        try (Cursor c = db.query(ThumbsContract.CommentThumbsEntry.TABLE_NAME, projection, null, null,
                null,
                null, null)) {
            while (c.moveToNext()) {
                commentThumbsMap.put(c.getInt(c.getColumnIndexOrThrow(ThumbsContract.CommentThumbsEntry
                        .COLUMN_NAME_COMMENT_ID)), c.getInt(c.getColumnIndexOrThrow(ThumbsContract.CommentThumbsEntry
                        .COLUMN_NAME_THUMBS_STATUS)));
            }
        }
        return commentThumbsMap;
    }
}
