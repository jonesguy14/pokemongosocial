package io.wandr_app.pokemongosocial;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that encapsulates all the data in a post.
 * Has data from the database like lat, long, img url, etc.
 * Has an associated map marker.
 * Created by Achi Jones on 7/1/2016.
 */
public class PokeGoPost {

    public int post_id;
    public String user_id;
    public String user_team;
    public String title;
    public String caption;
    public int time;
    public double latitude;
    public double longitude;
    public int likes;
    public boolean hasLiked;
    public boolean onlyVisibleTeam;

    /**
     * This constructor is used when the current use makes a post, so it just happened.
     * @param post_id
     * @param user_id
     * @param user_team
     * @param title
     * @param caption
     * @param timemsec
     * @param latitude
     * @param longitude
     * @param onlyVisibleTeam
     */
    public PokeGoPost(int post_id, String user_id, String user_team, String title, String caption, long timemsec, double latitude, double longitude, boolean onlyVisibleTeam) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.user_team = user_team;
        this.title = title;
        this.caption = caption;
        this.time = 0;
        this.latitude = latitude;
        this.longitude = longitude;
        this.likes = 1;
        this.hasLiked = false;
        this.onlyVisibleTeam = onlyVisibleTeam;
    }

    /**
     * This constructor is used when grabbing posts from online.
     * @param postJSON
     */
    public PokeGoPost(JSONObject postJSON) {
        try {
            post_id = postJSON.getInt("post_id");
            user_id = postJSON.getString("user_id");
            user_team = postJSON.getString("user_team");
            title = postJSON.getString("title");
            caption = postJSON.getString("caption");
            time = postJSON.getInt("time");
            latitude = postJSON.getDouble("latitude");
            longitude = postJSON.getDouble("longitude");
            likes = postJSON.getInt("likes");
            onlyVisibleTeam = postJSON.getInt("only_visible_team") == 1;
            hasLiked = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is borderline retarded
     * @return image url of the image of the pokemon
     */
    public String getImageURL() {
        return title.split(" ")[0].substring(1) + ".png";
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

}
