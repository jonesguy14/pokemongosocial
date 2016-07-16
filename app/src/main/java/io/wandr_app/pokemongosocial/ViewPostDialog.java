package io.wandr_app.pokemongosocial;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kylel on 7/15/2016.
 */
public class ViewPostDialog {
    TextView usernameTextView;
    TextView teamTextView;
    TextView postTimeTextView;
    TextView publicOrTeamTextView;

    ImageView postImageView;
    ImageView publicOrTeamImageView;

    TextView postPlaceTextView;
    TextView postCaptionTextView;

    TextView numLikes;
    ImageButton thumbUpButton;
    ImageButton thumbDownButton;
    Button commentButton;
    public static final int layout = R.layout.view_post_dialog;

    // Build from a visible dialog with the correct layout, and post
    public ViewPostDialog(AlertDialog dialog, PokeGoPost post, Resources resources) {
        usernameTextView = (TextView) dialog.findViewById(R.id.textViewUsername);
        teamTextView = (TextView) dialog.findViewById(R.id.textViewTeam);
        postTimeTextView = (TextView) dialog.findViewById(R.id.textViewPostTime);
        publicOrTeamTextView = (TextView) dialog.findViewById(R.id.textViewPublicOrTeam);

        postImageView = (ImageView) dialog.findViewById(R.id.imageViewPost);
        publicOrTeamImageView = (ImageView) dialog.findViewById(R.id.imageViewPublicOrTeam);

        postPlaceTextView = (TextView) dialog.findViewById(R.id.textViewPostPlace);
        postCaptionTextView = (TextView) dialog.findViewById(R.id.textViewPostCaption);

        numLikes = (TextView) dialog.findViewById(R.id.viewNumLikes);
        thumbUpButton = (ImageButton) dialog.findViewById(R.id.buttonThumbUp);
        thumbDownButton = (ImageButton) dialog.findViewById(R.id.buttonThumbDown);
        commentButton = (Button) dialog.findViewById(R.id.buttonComment);
        populateFromPost(post, resources);
    }

    private void populateFromPost(PokeGoPost post, Resources resources) {
        usernameTextView.setText(post.user_id);

        teamTextView.setText(post.user_team);
        switch (post.user_team) {
            case "Instinct":
                teamTextView.setTextColor(resources.getColor(R
                        .color.Instinct));
                break;
            case "Mystic":
                teamTextView.setTextColor(resources.getColor(R.color.Mystic));
                break;
            case "Valor":
                teamTextView.setTextColor(resources.getColor(R.color.Valor));
                break;
        }
        postTimeTextView.setText(getTimeDisplayString(post.time));
        setPublicOrTeamView(post.onlyVisibleTeam);
        postPlaceTextView.setText(post.title);
        postCaptionTextView.setText(post.caption);

        numLikes.setText(getNumLikesString(post.likes));
        if (post.thumbs == 1) {
            thumbUpButton.setColorFilter(Color.GREEN);
        } else if (post.thumbs == -1) {
            thumbDownButton.setColorFilter(Color.RED);
        }
    }

    private void setPublicOrTeamView(boolean isOnlyVisibleToTeam) {
        if (isOnlyVisibleToTeam) {
            publicOrTeamImageView.setImageResource(R.drawable.ic_lock_black_24dp);
            publicOrTeamTextView.setText("Team Only");
        } else {
            publicOrTeamImageView.setImageResource(R.drawable.ic_public_black_24dp);
            publicOrTeamTextView.setText("Public");
        }
        publicOrTeamImageView.setColorFilter(Color.GRAY);
    }

    private String getTimeDisplayString(int minutesAgo) {
        return minutesAgo > 1 ? minutesAgo + "min ago" : "Just now";
    }

    public static String getNumLikesString(int numLikes) {
        return numLikes > 0 ? "+" + numLikes : String.valueOf(numLikes);
    }
}
