package io.wandr_app.pokemongosocial.model;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import io.wandr_app.pokemongosocial.R;
import io.wandr_app.pokemongosocial.util.CommonUtils;

/**
 * Created by kylel on 7/15/2016.
 */
public class ViewPostDialog {
    public TextView usernameTextView;
    public TextView teamTextView;
    public TextView postTimeTextView;
    public TextView publicOrTeamTextView;

    public ImageView postImageView;
    public ImageView publicOrTeamImageView;

    public TextView postPlaceTextView;
    public TextView postCaptionTextView;

    public TextView numLikes;
    public ImageButton thumbUpButton;
    public ImageButton thumbDownButton;
    public Button commentButton;
    public static final int layout = R.layout.view_post_dialog;

    public Resources resources;

    // Build from a visible dialog with the correct layout, and post
    public ViewPostDialog(AlertDialog dialog, PokeGoPost post, Resources resources) {
        this.resources = resources;

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

        teamTextView.setText(post.user_team.toString());
        CommonUtils.setTeamTextViewColor(teamTextView, resources, post.user_team);
        postTimeTextView.setText(CommonUtils.getTimeDisplayString(post.time));
        setPublicOrTeamView(post.onlyVisibleTeam);
        postPlaceTextView.setText(post.title);
        postCaptionTextView.setText(post.caption);

        numLikes.setText(CommonUtils.getNumLikesString(post.likes));
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

    public void makeThumbsNeutral(int likes) {
        thumbDownButton.setColorFilter(null);
        thumbUpButton.setColorFilter(null);
        numLikes.setText(CommonUtils.getNumLikesString(likes));
    }

    public void makeThumbsUp(int likes) {
        thumbUpButton.setColorFilter(resources.getColor(R.color.thumbsUp));
        thumbDownButton.setColorFilter(Color.WHITE);
        numLikes.setText(CommonUtils.getNumLikesString(likes));
    }

    public void makeThumbsDown(int likes) {
        thumbDownButton.setColorFilter(resources.getColor(R.color.thumbsDown));
        thumbUpButton.setColorFilter(Color.WHITE);
        numLikes.setText(CommonUtils.getNumLikesString(likes));
    }
}
