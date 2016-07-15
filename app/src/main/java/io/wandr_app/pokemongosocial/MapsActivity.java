package io.wandr_app.pokemongosocial;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MyLocationListener locationListener;
    private LocationManager locationManager;

    private ImageLoader mImageLoader;
    private NetworkImageView mNetworkImageViewProfilePic;
    private TextView usernameTextView;
    private TextView teamTextView;
    private TextView timeJoinedTextView;
    private TextView reputationTextView;
    private FloatingActionButton fab;

    // Current user stuff
    private String username;
    private String password;
    private String team;
    private String profileImagePath;

    private double currRange = 1;

    private String newPostCaption;
    private String newPostTitle;
    private double newPostLatitude;
    private double newPostLongitude;
    private boolean newPostOnlyVisibleTeam;

    public static final String IMAGE_URL_BASE = "http://wandr-app.io/pokemon/images/";

    public HashMap<Marker, PokeGoPost> postHashMap;
    private Marker selectedMarker;
    private Marker currLocationMarker;
    private ArrayList<PokeGoPost> loadedPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            username = extras.getString("username");
            password = extras.getString("password");
        } else {
            Toast.makeText(MapsActivity.this, "Something went wrong when initializing.",
                    Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(MapsActivity.this, LoginActivity.class);
            MapsActivity.this.startActivity(myIntent);
        }

        // Get the NetworkImageView that will display the image.
        mNetworkImageViewProfilePic = (NetworkImageView) findViewById(R.id.imageViewProfilePic);

        // Get the ImageLoader through your singleton class.
        mImageLoader = VolleySingleton.getInstance(this).getImageLoader();

        // Get the text views
        usernameTextView = (TextView) findViewById(R.id.textViewUsername);
        teamTextView = (TextView) findViewById(R.id.textViewTeam);
        timeJoinedTextView = (TextView) findViewById(R.id.textViewTimeJoined);
        reputationTextView = (TextView) findViewById(R.id.textViewReputation);

        fab = (FloatingActionButton) findViewById(R.id.fabMaps);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewPostDialog();
            }
        });

        loadedPosts = new ArrayList<>();
        postHashMap = new HashMap<>();

        makeRequestGetUserInfo(username);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (isGPSEnabled()) {
            locationListener = new MyLocationListener(this);
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                //makeRequestGetNearbyPosts();
            } catch (SecurityException e) {
                Toast.makeText(MapsActivity.this, "Security Exception! Enable GPS!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MapsActivity.this, "Enable your GPS!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isGPSEnabled() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);

        return gpsStatus;
    }

    /**
     * Gets the current user's info, changing the text fields to the correct text.
     * Also changes the profile pic thumbnail
     * @param user user to examine
     */
    private void makeRequestGetUserInfo(final String user) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_user_info.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            System.out.println(responseJSON.toString());
                            usernameTextView.setText(responseJSON.getString("username"));
                            teamTextView.setText(responseJSON.getString("team") + " Team");
                            timeJoinedTextView.setText("Joined " + responseJSON.getString("time_joined").split(" ")[0]);
                            reputationTextView.setText("Reputation: " + responseJSON.getString("reputation"));

                            username = responseJSON.getString("username");
                            team = responseJSON.getString("team");
                            profileImagePath = responseJSON.getString("profile_image_path");
                            mNetworkImageViewProfilePic.setImageUrl(IMAGE_URL_BASE + profileImagePath, mImageLoader);

                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(MapsActivity.this, "Something went wrong.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("username", user);
                return map;
            }
        };
        requestQueue.add(request);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        addPostMarkers();

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                if (!m.getTitle().equals("You Are Here")) {
                    selectedMarker = m;
                    showViewPostDialog(postHashMap.get(selectedMarker));
                }
                return false; //false so that default behavior is done too
            }
        });
    }

    private void addPostMarkers() {
        if (mMap != null) {
            for (PokeGoPost p : loadedPosts) {

                boolean shouldAdd = true;
                for (PokeGoPost existingPost : postHashMap.values()) {
                    if (existingPost.post_id == p.post_id) {
                        // Already exists, don't add again
                        shouldAdd = false;
                        break;
                    }
                }

                if (shouldAdd) {
                    LatLng postCoord = new LatLng(p.latitude, p.longitude);
                    final Marker marker = mMap.addMarker(new MarkerOptions().position(postCoord).title(p.title));

                    // Get the right image for the marker
                    ImageRequest request = new ImageRequest(IMAGE_URL_BASE + p.getImageURL(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    Bitmap resized = PokeGoPost.getResizedBitmap(bitmap, bitmap.getWidth() * 2, bitmap.getHeight() * 2);
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(resized));
                                    marker.setAnchor(0.5f, 0.5f);
                                    marker.setInfoWindowAnchor(0.5f, 0.0f);
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    // Do nothing, default marker
                                }
                            });
                    RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
                    requestQueue.add(request);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postCoord, 15));
                    postHashMap.put(marker, p);
                }
            }
            loadedPosts.clear();
        }
    }

    /**
     * Gets the current user's info, changing the text fields to the correct text.
     * Also changes the profile pic thumbnail
     * @param user user to examine
     */
    private void makeRequestGetAllPostsFromUser(final String user) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_posts_from_user.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            System.out.println(responseJSON.toString());
                            for (int i = 0; i < responseJSON.getInt("num_rows"); ++i) {
                                loadedPosts.add(new PokeGoPost(responseJSON.getJSONObject("" + i)));
                            }
                            addPostMarkers();
                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(MapsActivity.this, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("username", user);
                return map;
            }
        };
        requestQueue.add(request);
    }

    /**
     * Gets the current user's info, changing the text fields to the correct text.
     * Also changes the profile pic thumbnail
     */
    public void makeRequestGetNearbyPosts() {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_nearby_posts.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            System.out.println(responseJSON.toString());
                            for (int i = 0; i < responseJSON.getInt("num_rows"); ++i) {
                                loadedPosts.add(new PokeGoPost(responseJSON.getJSONObject("" + i)));
                            }
                            addPostMarkers();
                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(MapsActivity.this, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("latitude", "" + locationListener.getCurrLocation().getLatitude());
                map.put("longitude", "" + locationListener.getCurrLocation().getLongitude());
                map.put("range", "" + currRange);
                map.put("team", team);
                return map;
            }
        };
        requestQueue.add(request);

        // Add marker for current location
        LatLng postCoord = new LatLng(locationListener.getCurrLocation().getLatitude(),
                locationListener.getCurrLocation().getLongitude());
        if (currLocationMarker != null) currLocationMarker.remove();
        currLocationMarker = mMap.addMarker(
                new MarkerOptions()
                    .position(postCoord)
                    .title("You Are Here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postCoord, 15));
    }

    public void showViewPostDialog(final PokeGoPost post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.view_post_dialog, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();

        final TextView usernameTextView = (TextView) dialog.findViewById(R.id.textViewUsername);
        final TextView teamTextView = (TextView) dialog.findViewById(R.id.textViewTeam);
        final TextView postTimeTextView = (TextView) dialog.findViewById(R.id.textViewPostTime);

        final ImageView postImageView = (ImageView) dialog.findViewById(R.id.imageViewPost);

        final TextView postPlaceTextView = (TextView) dialog.findViewById(R.id.textViewPostPlace);
        final TextView postCaptionTextView = (TextView) dialog.findViewById(R.id.textViewPostCaption);

        final TextView numLikes = (TextView) dialog.findViewById(R.id.viewNumLikes);
        final ImageButton thumbUpButton = (ImageButton) dialog.findViewById(R.id.buttonThumbUp);
        final ImageButton thumbDownButton = (ImageButton) dialog.findViewById(R.id.buttonThumbDown);
        final Button commentButton = (Button) dialog.findViewById(R.id.buttonComment);

        usernameTextView.setText(username);
        teamTextView.setText(post.user_team);
        if (post.time > 1) {
            postTimeTextView.setText(post.time + " min ago");
        } else {
            postTimeTextView.setText("Just now");
        }


        //postImageView.setImageUrl(IMAGE_URL_BASE + post.getImageURL(), mImageLoader);
        // Get the right image for the post
        ImageRequest request = new ImageRequest(IMAGE_URL_BASE + post.getImageURL(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Bitmap resized = PokeGoPost.getResizedBitmap(bitmap, bitmap.getWidth() * 5, bitmap.getHeight() * 5);
                        postImageView.setImageBitmap(resized);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        // Do nothing, default marker
                        //postImageView.setImageUrl(IMAGE_URL_BASE + post.user_team + "_pic.jpg" , mImageLoader);
                    }
                });
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        requestQueue.add(request);

        postPlaceTextView.setText(post.title);
        postCaptionTextView.setText(post.caption);

        numLikes.setText(getNumLikesString(post.likes));

        final ListView commentsListView = (ListView) dialog.findViewById(R.id.listViewComments);
        PokeGoComment[] comments = new PokeGoComment[1];
        comments[0] = new PokeGoComment();
        commentsListView.setAdapter(new CommentsListArrayAdapter(this, commentsListView, comments, post.post_id, true));

        if (post.hasLiked) {
            thumbUpButton.setEnabled(false);
            thumbDownButton.setEnabled(false);
        }

        thumbUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                makeRequestLikePost(post.post_id, post.user_id, "UP");
                post.likes++;
                post.hasLiked = true;
                thumbUpButton.setEnabled(false);
                thumbDownButton.setEnabled(false);
                numLikes.setText(getNumLikesString(post.likes));
            }
        });

        thumbDownButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                makeRequestLikePost(post.post_id, post.user_id, "DOWN");
                post.likes--;
                post.hasLiked = true;
                thumbUpButton.setEnabled(false);
                thumbDownButton.setEnabled(false);
                numLikes.setText(getNumLikesString(post.likes));
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                final EditText edittext = new EditText(MapsActivity.this);
                alert.setTitle("Comment on post");
                alert.setView(edittext);

                alert.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String content = edittext.getText().toString();
                        makeRequestActionPost(post.post_id, content, commentsListView);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }
        });

        // Make sure it shows the correct number of likes in case that changed since log in
        makeRequestNumLikes(post.post_id, numLikes);

    }

    private String getNumLikesString(int numLikes) {
        return numLikes > 0 ? "+ " + numLikes : String.valueOf(numLikes);
    }

    public void showNewPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Post")
                .setView(getLayoutInflater().inflate(R.layout.new_post_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        final EditText captionEditText = (EditText) dialog.findViewById(R.id.editTextCaption);

        final Spinner pokemonSpinner = (Spinner) dialog.findViewById(R.id.spinnerPokemon);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pokemon_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pokemonSpinner.setAdapter(adapter);

        final Button postButton = (Button) dialog.findViewById(R.id.buttonPost);

        final CheckBox onlyVisibleTeamCheckBox = (CheckBox) dialog.findViewById(R.id.checkBoxOnlyVisibleTeam);
        onlyVisibleTeamCheckBox.setText("Only visible to " + team + " team");

        postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                newPostTitle = pokemonSpinner.getSelectedItem().toString();
                System.out.println("Selected Item: " + pokemonSpinner.getSelectedItem().toString());
                newPostCaption = captionEditText.getText().toString();
                newPostLatitude = locationListener.getCurrLocation().getLatitude();//51.6 - Math.random()*0.2;
                newPostLongitude = locationListener.getCurrLocation().getLongitude();//0.15 - Math.random()*0.1;
                newPostOnlyVisibleTeam = onlyVisibleTeamCheckBox.isChecked();
                makeRequestNewPost();
                dialog.dismiss();
            }
        });
    }

    private void makeRequestNewPost() {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/new_post.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        System.out.println("Response:\n" + response);
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();

                            // Create a PokeGoPost object and add it to the map
                            PokeGoPost newPost = new PokeGoPost(responseJSON.getInt("post_id"), username, team, newPostTitle, newPostCaption,
                                    System.currentTimeMillis(), newPostLatitude, newPostLongitude, newPostOnlyVisibleTeam);
                            newPost.hasLiked = true;
                            loadedPosts.add(newPost);
                            addPostMarkers();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MapsActivity.this, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("username", username);
                map.put("password", password);
                map.put("title", newPostTitle);
                map.put("team", team);
                map.put("caption", newPostCaption);
                map.put("latitude", "" + newPostLatitude);
                map.put("longitude", "" + newPostLongitude);
                map.put("only_visible_team", "" + (newPostOnlyVisibleTeam ? 1 : 0));
                return map;
            }
        };
        requestQueue.add(request);
    }

    private void makeRequestNumLikes(final int post_id, final TextView numLikesText) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_num_likes.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        System.out.println("Response:\n" + response);
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();

                            numLikesText.setText(getNumLikesString(responseJSON.getInt("likes")));

                            for (PokeGoPost post : postHashMap.values()) {
                                if (post.post_id == post_id) {
                                    post.likes = responseJSON.getInt("likes");
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MapsActivity.this, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("post_id", "" + post_id);
                map.put("username", username);
                return map;
            }
        };
        requestQueue.add(request);
    }

    private void makeRequestActionPost(final int post_id, final String content, final ListView commentsList) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/action_post.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        System.out.println("Response:\n" + response);
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();

                            // Need to refresh comment list
                            CommentsListArrayAdapter commentsAdapter = (CommentsListArrayAdapter) commentsList.getAdapter();
                            commentsAdapter.makeRequestGetComments();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MapsActivity.this, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("post_id", "" + post_id);
                map.put("username", username);
                map.put("password", password);
                map.put("content", content);
                return map;
            }
        };
        requestQueue.add(request);
    }

    private void makeRequestLikePost(final int post_id, final String post_user_id, final String isUpDown) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/like_post.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        System.out.println("Response:\n" + response);
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MapsActivity.this, "Something went wrong with Volley.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("post_id", "" + post_id);
                map.put("post_user_id", post_user_id);
                map.put("username", username);
                map.put("password", password);
                map.put("isUpDown", isUpDown);
                return map;
            }
        };
        requestQueue.add(request);
    }


    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount())) + 100;
        listView.setLayoutParams(params);
    }
}
