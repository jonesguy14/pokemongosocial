package io.wandr_app.pokemongosocial.model;

import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.wandr_app.pokemongosocial.util.CommonUtils;

/**
 * Represents the user information relevant to us
 */
public final class User {
    private final String username;
    private final String password;
    private final TextView usernameTextView;
    private final TextView teamTextView;
    private final TextView timeJoinedTextView;
    private final TextView reputationTextView;

    public Team team;
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
        teamTextView.setText(String.format("%s Team", requestUserInfoResponse.getString("team")));
        timeJoinedTextView.setText("Joined " + requestUserInfoResponse.getString("time_joined").split(" ")[0]);
        reputationTextView.setText("Reputation: " + CommonUtils.getNumLikesString(requestUserInfoResponse.getInt
                ("reputation")));

        team = Team.fromString(requestUserInfoResponse.getString("team"));
        profileImagePath = requestUserInfoResponse.getString("profile_image_path");
    }
}
