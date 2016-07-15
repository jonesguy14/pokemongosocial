package io.wandr_app.pokemongosocial;

import android.app.Activity;
import android.content.Context;
import android.opengl.Visibility;
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

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * List adapter to show comments on a post.
 * Created by Achi Jones on 7/2/2016.
 */
public class CommentsListArrayAdapter extends ArrayAdapter<PokeGoComment> {

    private final Context context;
    private PokeGoComment[] values;
    private int post_id;
    private ListView list;

    private String username;
    private String password;

    private HashMap<Integer, String> commentThumbsMap;

    public CommentsListArrayAdapter(Context context, HashMap<Integer, String> commentThumbsMap, String username, String password, ListView list, PokeGoComment[] values, int post_id, boolean makeRequest) {
        super(context, R.layout.comment_list_item, values);
        this.context = context;
        this.values = values;
        this.post_id = post_id;
        this.list = list;
        this.username = username;
        this.password = password;
        this.commentThumbsMap = commentThumbsMap;
        if (makeRequest) {
            makeRequestGetComments();
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

            if (values[position].team.equals("Instinct")) {
                teamTextView.setTextColor(context.getResources().getColor(R.color.Instinct));
            } else if (values[position].team.equals("Mystic")) {
                teamTextView.setTextColor(context.getResources().getColor(R.color.Mystic));
            } else if (values[position].team.equals("Valor")) {
                teamTextView.setTextColor(context.getResources().getColor(R.color.Valor));
            }

            if (values[position].time >= 1) {
                timeTextView.setText(values[position].time + " min ago");
            } else {
                timeTextView.setText("Just now");
            }
            likesTextView.setText("+" + values[position].likes);

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
                                // Thumb Up
                                if (values[position].thumbs != 1) {
                                    makeRequestLikeComment(values[position], 1 - values[position].thumbs);

                                    thumbsUpDownButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_thumb_up_black_24dp));
                                    thumbsUpDownButton.setColorFilter(context.getResources().getColor(R.color.thumbsUp));

                                    values[position].likes += 1 - values[position].thumbs;
                                    values[position].thumbs = 1;
                                    likesTextView.setText(getNumLikesString(values[position].likes));

                                    recordCommentThumbs(values[position].action_id, "UP");
                                }
                            } else if (item.getTitle().toString().equals(context.getResources().getString(R.string.thumbsDown))) {
                                // Thumb Down
                                if (values[position].thumbs != -1) {
                                    makeRequestLikeComment(values[position], -1 - values[position].thumbs);

                                    thumbsUpDownButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_thumb_down_black_24dp));
                                    thumbsUpDownButton.setColorFilter(context.getResources().getColor(R.color.thumbsDown));

                                    values[position].likes += -1 - values[position].thumbs;
                                    values[position].thumbs = -1;
                                    likesTextView.setText(getNumLikesString(values[position].likes));

                                    recordCommentThumbs(values[position].action_id, "DOWN");
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
     * Gets a list of comments for the current post as a JSON.
     * Updates the listview to show all of them.
     */
    public void makeRequestGetComments() {
        RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_comments.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        System.out.println("Response:\n" + response);
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(context, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();

                            if (responseJSON.has("num_rows")) {
                                int numRows = responseJSON.getInt("num_rows");
                                final PokeGoComment[] items = new PokeGoComment[numRows];
                                for (int i = 0; i < numRows; ++i) {
                                    items[i] = new PokeGoComment(responseJSON.getJSONObject("" + i));

                                    if (commentThumbsMap.containsKey(items[i].action_id)) {
                                        if (commentThumbsMap.get(items[i].action_id).equals("UP")) {
                                            items[i].thumbs = 1;
                                        } else if (commentThumbsMap.get(items[i].action_id).equals("DOWN")) {
                                            items[i].thumbs = -1;
                                        }
                                    }
                                }

                                list.setAdapter(new CommentsListArrayAdapter(context, commentThumbsMap, username, password, list, items, post_id, false));
                                MapsActivity.setListViewHeightBasedOnChildren(list);
                            } else {
                                values[0].content = "No comments to display.";
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(context, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
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
                        // Get the JSON Response
                        System.out.println("Response:\n" + response);
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(context, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(context, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
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

    /**
     * Records if a comment has been thumbed up or down, so that repeated voting is stopped.
     * @param action_id the comment that is liked
     * @param upOrDown "UP" or "DOWN", based on thumb
     */
    private void recordCommentThumbs(int action_id, String upOrDown) {
        if (commentThumbsMap.containsKey(action_id)) {
            commentThumbsMap.remove(action_id);
        }
        commentThumbsMap.put(action_id, upOrDown);

        FileOutputStream outputStream;
        String data = action_id + " " + upOrDown + "\n";
        try {
            outputStream = context.openFileOutput("comments_thumbs.txt", Activity.MODE_APPEND);
            outputStream.write(data.getBytes());
            outputStream.close();
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String getNumLikesString(int numLikes) {
        return numLikes > 0 ? "+" + numLikes : String.valueOf(numLikes);
    }

}
