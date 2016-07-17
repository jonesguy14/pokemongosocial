package io.wandr_app.pokemongosocial.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.wandr_app.pokemongosocial.R;
import io.wandr_app.pokemongosocial.VolleySingleton;
import io.wandr_app.pokemongosocial.model.PokeGoComment;
import io.wandr_app.pokemongosocial.model.Team;
import io.wandr_app.pokemongosocial.util.CommonUtils;
import io.wandr_app.pokemongosocial.util.ThumbsMapWorker;

/**
 * List adapter to show comments on a post.
 * Created by Achi Jones on 7/2/2016.
 */
public class CommentsListArrayAdapter extends ArrayAdapter<PokeGoComment> {

    private static final String TAG = "CommentsListArrAdapter";

    private final Context context;
    private PokeGoComment[] values;
    private int post_id;
    private ListView list;

    private String username;
    private String password;

    private Map<Integer, Integer> commentThumbsMap;

    private ThumbsMapWorker worker;

    public CommentsListArrayAdapter(Context context, Map<Integer, Integer> commentThumbsMap,
                                    String username, String password, ListView list,
                                    PokeGoComment[] values, int post_id, boolean makeRequest) {
        super(context, R.layout.comment_list_item, values);
        this.context = context;
        this.values = values;
        this.post_id = post_id;
        this.list = list;
        this.username = username;
        this.password = password;
        this.commentThumbsMap = commentThumbsMap;
        worker = new ThumbsMapWorker(context);
        if (makeRequest) {
            makeRequestGetComments("");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_list_item, parent, false);
        }

        TextView teamTextView = (TextView) convertView.findViewById(R.id.textViewTeam);
        TextView usernameTextView = (TextView) convertView.findViewById(R.id.textViewUsername);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.textViewTime);
        final TextView likesTextView = (TextView) convertView.findViewById(R.id.textViewLikes);
        final TextView contentTextView = (TextView) convertView.findViewById(R.id.textViewContent);

        // The thumb up/down button
        final ImageButton thumbsUpDownButton = (ImageButton) convertView.findViewById(R.id.buttonThumbsUpDown);

        if (values[position].username.equals("")) {
            // Is the "No Comments" comment
            usernameTextView.setText("");
            timeTextView.setText("");
            likesTextView.setText("");
            teamTextView.setText("");
            thumbsUpDownButton.setVisibility(View.GONE);
        } else {
            // Is a real comment
            usernameTextView.setText(values[position].username);
            teamTextView.setText(values[position].team);

            CommonUtils.setTeamTextViewColor(teamTextView, context.getResources(), Team.fromString
                    (values[position].team));

            timeTextView.setText(CommonUtils.getTimeDisplayString(values[position].time));
            likesTextView.setText(CommonUtils.getNumLikesString(values[position].likes));

            // Set the button to correct image if the comment was already voted on
            if (values[position].thumbs == 1) {
                thumbsUpDownButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_thumb_up_black_24dp));
                thumbsUpDownButton.setColorFilter(context.getResources().getColor(R.color.thumbsUp));
            } else if (values[position].thumbs == -1) {
                thumbsUpDownButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_thumb_down_black_24dp));
                thumbsUpDownButton.setColorFilter(context.getResources().getColor(R.color.thumbsDown));
            }

            // Set up the thumbs up/down pop up menu
            thumbsUpDownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(context, thumbsUpDownButton);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.comment_thumb_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().toString().equals(context.getResources().getString(R.string.thumbsUp))) {
                                if (values[position].thumbs == 1) {
                                    // Un thumb it
                                    thumbsComment(values[position], 0, thumbsUpDownButton, likesTextView);
                                } else {
                                    // thumbs up
                                    thumbsComment(values[position], 1, thumbsUpDownButton, likesTextView);
                                }
                            } else if (item.getTitle().toString().equals(context.getResources().getString(R.string.thumbsDown))) {
                                if (values[position].thumbs == -1) {
                                    // Un thumb it
                                    thumbsComment(values[position], 0, thumbsUpDownButton, likesTextView);
                                } else {
                                    // thumbs down
                                    thumbsComment(values[position], -1, thumbsUpDownButton, likesTextView);
                                }
                            }
                            return true;
                        }
                    });

                    popup.show(); //showing popup menu
                }
            }); //closing the setOnClickListener method

        }

        // Always show the content, this is where the "No comments" is
        contentTextView.setText(values[position].content);

        return convertView;
    }

    /**
     * Apply a thumb/unthumb to a comment
     * @param comment comment affected
     * @param thumbValue new value, -1, 0, or 1
     * @param thumbsUpDownButton button so that image is changed
     * @param thumbsTextView textview that displays # of likes
     */
    public void thumbsComment(PokeGoComment comment, int thumbValue, ImageButton thumbsUpDownButton, TextView thumbsTextView) {
        makeRequestLikeComment(comment, thumbValue - comment.thumbs);

        if (thumbValue == -1) {
            thumbsUpDownButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_thumb_down_black_24dp));
            thumbsUpDownButton.setColorFilter(context.getResources().getColor(R.color.thumbsDown));
        } else if (thumbValue == 1) {
            thumbsUpDownButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_thumb_up_black_24dp));
            thumbsUpDownButton.setColorFilter(context.getResources().getColor(R.color.thumbsUp));
        } else {
            thumbsUpDownButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_thumbs_up_down_black_24dp));
            thumbsUpDownButton.setColorFilter(Color.parseColor("#888888"));
        }

        comment.likes += thumbValue - comment.thumbs;
        comment.thumbs = thumbValue;
        thumbsTextView.setText(CommonUtils.getNumLikesString(comment.likes));

        String[] thumbs = {"DOWN", "NONE", "UP"};
        worker.recordCommentThumbs(comment.action_id, thumbs[thumbValue+1], commentThumbsMap);
    }

    /**
     * Gets a list of comments for the current post as a JSON.
     * Updates the listview to show all of them.
     * @param content comment that was just made (if applicable)
     */
    public void makeRequestGetComments(final String content) {
        RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_comments.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.has("num_rows")) {
                                int numRows = responseJSON.getInt("num_rows");
                                final PokeGoComment[] items = new PokeGoComment[numRows];
                                for (int i = 0; i < numRows; ++i) {
                                    items[i] = new PokeGoComment(responseJSON.getJSONObject("" + i));

                                    if (items[i].content.equals(content)) {
                                        // Is the comment that the user just made, auto-upvote it
                                        items[i].thumbs = 1;
                                        worker.recordCommentThumbs(items[i].action_id, "UP", commentThumbsMap);
                                    }

                                    // If the user has thumbed it before set it to value
                                    if (commentThumbsMap.containsKey(items[i].action_id)) {
                                        items[i].thumbs = commentThumbsMap.get(items[i].action_id);
                                    }
                                }

                                list.setAdapter(new CommentsListArrayAdapter(context, commentThumbsMap, username, password, list, items, post_id, false));
                                CommonUtils.setListViewHeightBasedOnChildren(list);
                            } else {
                                values[0].content = "No comments to display.";
                            }

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                            Toast.makeText(context, "Something went wrong.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
                Toast.makeText(context, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("post_id", "" + post_id);
                return map;
            }
        };
        requestQueue.add(request);
    }

    /**
     * Like a comment.
     */
    public void makeRequestLikeComment(final PokeGoComment comment, final int change) {
        RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/like_comment.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Should we even tell user that it failed?
                        /*try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(context, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
                Toast.makeText(context, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("username", username);
                map.put("password", password);
                map.put("action_id", "" + comment.action_id);
                map.put("action_user_id", comment.username);
                map.put("change", "" + change);
                return map;
            }
        };
        requestQueue.add(request);
    }
}
