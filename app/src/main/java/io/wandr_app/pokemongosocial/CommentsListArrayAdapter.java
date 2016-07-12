package io.wandr_app.pokemongosocial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

/**
 * List adapter to show comments on a post.
 * Created by Achi Jones on 7/2/2016.
 */
public class CommentsListArrayAdapter extends ArrayAdapter<PokeGoComment> {

    private final Context context;
    private PokeGoComment[] values;
    private int post_id;
    private ListView list;

    public CommentsListArrayAdapter(Context context, ListView list, PokeGoComment[] values, int post_id, boolean makeRequest) {
        super(context, R.layout.comment_list_item, values);
        this.context = context;
        this.values = values;
        this.post_id = post_id;
        this.list = list;
        if (makeRequest) {
            makeRequestGetComments();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_list_item, parent, false);
        }

        TextView teamTextView = (TextView) convertView.findViewById(R.id.textViewTeam);
        TextView usernameTextView = (TextView) convertView.findViewById(R.id.textViewUsername);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.textViewTime);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.textViewContent);

        teamTextView.setText(values[position].team);
        if (values[position].username.equals("")) {
            usernameTextView.setText("");
        } else {
            usernameTextView.setText(values[position].username);
        }
        timeTextView.setText(values[position].time);
        contentTextView.setText(values[position].content);

        return convertView;
    }

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
                                }

                                list.setAdapter(new CommentsListArrayAdapter(context, list, items, post_id, false));
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

}
