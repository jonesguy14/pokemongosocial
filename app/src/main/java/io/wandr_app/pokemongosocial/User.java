package io.wandr_app.pokemongosocial;

import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kylel on 7/15/2016.
 * Represents the user information relevant to us
 */
public final class User {
    private final String username;
    private final String password;
    private final TextView usernameTextView;
    private final TextView teamTextView;
    private final TextView timeJoinedTextView;
    private final TextView reputationTextView;

    public String team;
    public String profileImagePath;

    public User(final String username, final String password, final TextView usernameTextView,
                final TextView teamTextView, final TextView timeJoinedTextView, final TextView
                        reputationTextView) {
        this.username = username;
        this.password = password;
        this.usernameTextView = usernameTextView;
        this.teamTextView = teamTextView;
        this.timeJoinedTextView = timeJoinedTextView;
        this.reputationTextView = reputationTextView;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void updateFields(JSONObject requestUserInfoResponse) throws JSONException {
        usernameTextView.setText(requestUserInfoResponse.getString("username"));
        teamTextView.setText(requestUserInfoResponse.getString("team") + " Team");
        timeJoinedTextView.setText("Joined " + requestUserInfoResponse.getString("time_joined").split(" ")[0]);
        reputationTextView.setText("Reputation: " + getNumLikesString(requestUserInfoResponse.getInt("reputation")));

        team = requestUserInfoResponse.getString("team");
        profileImagePath = requestUserInfoResponse.getString("profile_image_path");
    }

    private String getNumLikesString(int numLikes) {
        return numLikes > 0 ? "+" + numLikes : String.valueOf(numLikes);
    }
}
