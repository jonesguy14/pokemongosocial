package io.wandr_app.pokemongosocial;

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
    public String user_team;
    public String title;
    public String caption;
    public String time;
    public double latitude;
    public double longitude;
    public int likes;

    public PokeGoPost(int post_id, String user_id, String user_team, String title, String caption, String time, double latitude, double longitude, int likes) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.user_team = user_team;
        this.title = title;
        this.caption = caption;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.likes = 0;
    }

    public PokeGoPost(JSONObject postJSON) {
        try {
            post_id = postJSON.getInt("post_id");
            user_id = postJSON.getString("user_id");
            user_team = postJSON.getString("user_team");
            title = postJSON.getString("title");
            caption = postJSON.getString("caption");
            time = postJSON.getString("time");
            latitude = postJSON.getDouble("latitude");
            longitude = postJSON.getDouble("longitude");
            likes = postJSON.getInt("likes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
