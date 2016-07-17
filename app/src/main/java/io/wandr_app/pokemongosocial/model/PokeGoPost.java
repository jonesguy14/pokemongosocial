package io.wandr_app.pokemongosocial.model;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import org.json.JSONObject;

/**
 * Class that encapsulates all the data in a post.
 * Has data from the database like lat, long, img url, etc.
 * Has an associated map marker.
 * Created by Achi Jones on 7/1/2016.
 */
public class PokeGoPost {

    public int post_id;
    public String user_id;
    public Team user_team;
    public String title;
    public String caption;
    public int time;
    public double latitude;
    public double longitude;
    public int likes;
    public int thumbs; // -1 for down, 0 for none, 1 for up
    public boolean onlyVisibleTeam;

    /**
     * This constructor is used when the current user makes a post, so it just happened.
     */
    public PokeGoPost(int post_id, String user_id, Team user_team, String title, String caption,
                      double latitude, double longitude, boolean onlyVisibleTeam) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.user_team = user_team;
        this.title = title;
        this.caption = caption;
        this.time = 0;
        this.latitude = latitude;
        this.longitude = longitude;
        this.likes = 1;
        this.thumbs = 1;
        this.onlyVisibleTeam = onlyVisibleTeam;
    }

    /**
     * This constructor is used when grabbing posts from online.
     */
    public PokeGoPost(JSONObject postJSON) {
        try {
            post_id = postJSON.getInt("post_id");
            user_id = postJSON.getString("user_id");
            user_team = Team.fromString(postJSON.getString("user_team"));
            title = postJSON.getString("title");
            caption = postJSON.getString("caption");
            time = postJSON.getInt("time");
            latitude = postJSON.getDouble("latitude");
            longitude = postJSON.getDouble("longitude");
            likes = postJSON.getInt("likes");
            onlyVisibleTeam = postJSON.getInt("only_visible_team") == 1;
            thumbs = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is guaranteed retarded
     * I found a #XXX blah blah! => XXX.png
     * @return image url of the image of the pokemon
     */
    public String getImageURL() {
        if (title.split(" ").length >= 4) {
            return title.split(" ")[3].substring(1) + ".png";
        } else {
            return title.split(" ")[0].substring(1) + ".png";
        }
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
