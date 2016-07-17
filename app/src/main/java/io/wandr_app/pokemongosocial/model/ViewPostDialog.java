package io.wandr_app.pokemongosocial.model;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
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

    public Context context;

    // Build from a visible dialog with the correct layout, and post
    public ViewPostDialog(AlertDialog dialog, PokeGoPost post, Context context) {
        this.context = context;

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
        populateFromPost(post, context);
    }

    private void populateFromPost(PokeGoPost post, Context context) {
        usernameTextView.setText(post.user_id);

        teamTextView.setText(post.user_team.toString());
        CommonUtils.setTeamTextViewColor(teamTextView, context, post.user_team);
        postTimeTextView.setText(CommonUtils.getTimeDisplayString(post.time));
        setPublicOrTeamView(post.onlyVisibleTeam);
        postPlaceTextView.setText(post.title);
        postCaptionTextView.setText(post.caption);


        numLikes.setText(CommonUtils.getNumLikesString(post.likes));
        switch (post.thumbs) {
            case -1:
                makeThumbsDown(post.likes);
                break;
            case 0:
                makeThumbsNeutral(post.likes);
                break;
            case 1:
                makeThumbsUp(post.likes);
                break;
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
        thumbUpButton.setColorFilter(ContextCompat.getColor(context, R.color.thumbsUp));
        thumbDownButton.setColorFilter(Color.WHITE);
        numLikes.setText(CommonUtils.getNumLikesString(likes));
    }

    public void makeThumbsDown(int likes) {
        thumbDownButton.setColorFilter(ContextCompat.getColor(context, R.color.thumbsDown));
        thumbUpButton.setColorFilter(Color.WHITE);
        numLikes.setText(CommonUtils.getNumLikesString(likes));
    }
}
