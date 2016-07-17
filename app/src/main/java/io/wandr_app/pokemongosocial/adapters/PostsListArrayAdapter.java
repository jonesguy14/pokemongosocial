package io.wandr_app.pokemongosocial.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import io.wandr_app.pokemongosocial.R;
import io.wandr_app.pokemongosocial.VolleySingleton;
import io.wandr_app.pokemongosocial.model.PokeGoPost;
import io.wandr_app.pokemongosocial.util.CommonUtils;

/**
 * List adapter for displaying a particular user's posts.
 * Created by Achi Jones on 7/16/2016.
 */
public class PostsListArrayAdapter extends ArrayAdapter<PokeGoPost> {
    private final Context context;
    private PokeGoPost[] values;

    public PostsListArrayAdapter(Context context, PokeGoPost[] values) {
        super(context, R.layout.comment_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Reuse views if we can
        View rowView;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.recent_posts_list_item, parent, false);
        } else {
            rowView = convertView;
        }
        //View rowView = inflater.inflate(R.layout.recent_posts_list_item, parent, false);

        TextView titleTextView = (TextView) rowView.findViewById(R.id.textViewTitle);
        TextView captionTextView = (TextView) rowView.findViewById(R.id.textViewCaption);
        TextView timeTextView = (TextView) rowView.findViewById(R.id.textViewTime);
        TextView likesTextView = (TextView) rowView.findViewById(R.id.textViewNumLikes);

        PokeGoPost post = values[position];

        titleTextView.setText(post.title);
        captionTextView.setText(post.caption);
        timeTextView.setText(CommonUtils.getTimeDisplayString(post.time));
        likesTextView.setText(CommonUtils.getNumLikesString(post.likes));

        final NetworkImageView postImageView = (NetworkImageView) rowView.findViewById(R.id.imageViewPost);
        postImageView.setImageUrl(CommonUtils.IMAGE_URL_BASE + post.getImageURL(),
                VolleySingleton.getInstance(context).getImageLoader());

        return rowView;
    }
}
