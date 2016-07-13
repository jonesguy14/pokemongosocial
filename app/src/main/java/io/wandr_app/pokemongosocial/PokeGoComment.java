package io.wandr_app.pokemongosocial;

import org.json.JSONObject;

/**
 * Class to encapsulate a comment on a post, like user_id, content, time, etc.
 * Created by Achi Jones on 7/2/2016.
 */
public class PokeGoComment {

    public String username;
    public String team;
    public String profileImagePath;

    public String content;
    public int time; // time elapsed in minutes
    public int likes;

    public PokeGoComment(JSONObject comment) {
        try {
            username = comment.getString("username");
            team = comment.getString("team");
            profileImagePath = comment.getString("profile_image_path");
            content = comment.getString("content");
            time = comment.getInt("time");
            likes = comment.getInt("likes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PokeGoComment() {
        // Used as a placeholder while comments load
        username = "";
        team = "";
        time = 0;
        content = "No comments to display.";
        likes = 0;
    }

}
