package io.wandr_app.pokemongosocial;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.wandr_app.pokemongosocial.adapters.CommentsListArrayAdapter;
import io.wandr_app.pokemongosocial.adapters.MenuListArrayAdapter;
import io.wandr_app.pokemongosocial.adapters.PostsListArrayAdapter;
import io.wandr_app.pokemongosocial.model.PokeGoComment;
import io.wandr_app.pokemongosocial.model.PokeGoPost;
import io.wandr_app.pokemongosocial.model.User;
import io.wandr_app.pokemongosocial.model.ViewPostDialog;
import io.wandr_app.pokemongosocial.util.CommonUtils;
import io.wandr_app.pokemongosocial.util.ThumbsMapWorker;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 10;
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private CurrentLocationListener locationListener;
    private boolean hasZoomed = false; // when we first get location, zoom to it.

    private ImageLoader mImageLoader;
    private NetworkImageView mNetworkImageViewProfilePic;

    private FloatingActionButton fab;

    // The user using the map
    private User user;

    // This value is not true lat/long range, it is squared when passed through
    // This allows a high range without "neighborhood" range being like 0.0001
    private double currRange = 1;
    private static final double MAX_RANGE = 1;

    public static final String IMAGE_URL_BASE = "http://wandr-app.io/pokemon/images/";

    public HashMap<Marker, PokeGoPost> postHashMap;
    private Marker selectedMarker;
    private Marker currLocationMarker;
    private ArrayList<PokeGoPost> loadedPosts;
    private Map<Integer, Integer> postThumbsMap;
    private Map<Integer, Integer> commentThumbsMap;

    private ThumbsMapWorker worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = new User(extras.getString("username"), extras.getString("password"),
                    (TextView) findViewById(R.id.textViewUsername), (TextView) findViewById(R.id
                    .textViewTeam), (TextView) findViewById(R.id.textViewTimeJoined), (TextView)
                    findViewById(R.id.textViewReputation));
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

        // Set up the fab menu
        fab = (FloatingActionButton) findViewById(R.id.fabMaps);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFabMenuDialog();
            }
        });

        loadedPosts = new ArrayList<>();
        postHashMap = new HashMap<>();

        worker = new ThumbsMapWorker(this);
        postThumbsMap = worker.loadPostThumbsMap();
        commentThumbsMap = worker.loadCommentThumbsMap();

        makeRequestGetUserInfo(user.getUsername());

        locationListener = new CurrentLocationListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                    }
                }
            }
        }
    }

    /**
     * Move to current location, if available
     */
    public void moveCamera() {
        if (isCurrentLocationAvailable()) {
            Location currentLocation = locationListener.getCurrLocation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation
                    .getLatitude(), currentLocation.getLongitude()), 18));
        }
    }

    /**
     * True if current location available. Display message if not
     */
    private boolean isCurrentLocationAvailable() {
        if (locationListener.getCurrLocation() != null) {
            return true;
        } else {
            Toast.makeText(this, "Could not retrieve your location!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Gets the current user's info, changing the text fields to the correct text.
     * Also changes the profile pic thumbnail
     */
    private void makeRequestGetUserInfo(final String username) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_user_info.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            user.updateFields(responseJSON);

                            mNetworkImageViewProfilePic.setImageUrl(IMAGE_URL_BASE +
                                    user.profileImagePath, mImageLoader);

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
                HashMap<String, String> map = new HashMap<>();
                map.put("username", username);
                return map;
            }
        };
        requestQueue.add(request);
    }

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

                // Check if it already exists
                boolean shouldAdd = true;
                for (PokeGoPost existingPost : postHashMap.values()) {
                    if (existingPost.post_id == p.post_id) {
                        // Already exists, don't add again
                        shouldAdd = false;
                        break;
                    }
                }

                // Check if user has voted on it already
                if (postThumbsMap.containsKey(p.post_id)) {
                    if (postThumbsMap.get(p.post_id).equals("UP")) {
                        p.thumbs = 1;
                    } else if (postThumbsMap.get(p.post_id).equals("DOWN")) {
                        p.thumbs = -1;
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

                    postHashMap.put(marker, p);
                }
            }
            loadedPosts.clear();
        }
    }

    /**
     * Gets all posts from the current user, and brings up a list dialog to display them.
     */
    private void makeRequestGetRecentPosts() {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_posts_from_user.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "makeRequestGetRecentPosts JSON response: " + response);
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);

                            PokeGoPost[] posts = new PokeGoPost[responseJSON.getInt("num_rows")];
                            for (int i = 0; i < posts.length; ++i) {
                                posts[i] = new PokeGoPost(responseJSON.getJSONObject("" + i));
                            }

                            showRecentPostsDialog(posts);

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
                HashMap<String, String> map = new HashMap<>();
                map.put("username", user.getUsername());
                return map;
            }
        };
        requestQueue.add(request);
    }

    /**
     * Show list of recent posts by the user.
     */
    public void showRecentPostsDialog(final PokeGoPost[] posts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing?
                dialog.dismiss();
            }
        })
                .setView(getLayoutInflater().inflate(R.layout.simple_list_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        ListView postList = (ListView) dialog.findViewById(R.id.listView);

        final PostsListArrayAdapter listAdapter = new PostsListArrayAdapter(MapsActivity.this, posts);
        postList.setAdapter(listAdapter);

        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                showViewPostDialog(posts[position]);
            }
        });
    }

    /**
     * Gets posts within range
     */
    public void makeRequestGetNearbyPosts() {
        if (!isCurrentLocationAvailable()) {
            return;
        }
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "http://wandr-app.io/pokemon/get_nearby_posts.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response);
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Log.i(TAG, responseJSON.toString());
                            if (responseJSON.has("num_rows")) {
                                for (int i = 0; i < responseJSON.getInt("num_rows"); ++i) {
                                    loadedPosts.add(new PokeGoPost(responseJSON.getJSONObject("" + i)));
                                }
                                addPostMarkers();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Exception retrieving nearby posts", e);
                            Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
                HashMap<String, String> map = new HashMap<>();
                map.put("latitude", "" + locationListener.getCurrLocation().getLatitude());
                map.put("longitude", "" + locationListener.getCurrLocation().getLongitude());
                map.put("range", "" + Math.pow(currRange,2));
                map.put("team", user.team.toString());
                return map;
            }
        };
        requestQueue.add(request);

        LatLng postCoord = new LatLng(locationListener.getCurrLocation().getLatitude(),
                locationListener.getCurrLocation().getLongitude());
        if (currLocationMarker != null) currLocationMarker.remove();
        currLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(postCoord)
                        .title("You Are Here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // When we first get location, zoom to it
        if (!hasZoomed) {
            moveCamera();
            hasZoomed = true;
        }
    }

    public void showViewPostDialog(final PokeGoPost post) {
        final int initialLikes = post.likes;
        final int initialThumbs = post.thumbs;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(ViewPostDialog.layout, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // update reputation
                makeRequestGetUserInfo(user.getUsername());
                if (initialLikes - post.likes != 0) {
                    makeRequestChangePostLikes(post.post_id, post.user_id, post.likes - initialLikes);
                }
                if (initialThumbs != post.thumbs) {
                    switch (post.thumbs) {
                        case -1:
                            worker.recordPostThumbs(post.post_id, "DOWN", postThumbsMap);
                            break;
                        case 0:
                            worker.recordPostThumbs(post.post_id, "NONE", postThumbsMap);
                        case 1:
                            worker.recordPostThumbs(post.post_id, "UP", postThumbsMap);
                            break;
                    }
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        final ViewPostDialog viewPostDialog = new ViewPostDialog(dialog, post, getResources());

        // Get the right image for the post
        ImageRequest request = new ImageRequest(IMAGE_URL_BASE + post.getImageURL(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Bitmap resized = PokeGoPost.getResizedBitmap(bitmap, bitmap.getWidth() * 5, bitmap.getHeight() * 5);
                        viewPostDialog.postImageView.setImageBitmap(resized);
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

        final ListView commentsListView = (ListView) dialog.findViewById(R.id.listViewComments);
        PokeGoComment[] comments = new PokeGoComment[1];
        comments[0] = new PokeGoComment();
        commentsListView.setAdapter(new CommentsListArrayAdapter(this, commentThumbsMap, user.getUsername(),
                user.getPassword(), commentsListView, comments, post.post_id, true));

        viewPostDialog.thumbUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (post.thumbs == 1) {
                    post.likes--;
                    post.thumbs = 0;
                    viewPostDialog.makeThumbsNeutral(post.likes);
                } else {
                    post.likes += 1 - post.thumbs;
                    post.thumbs = 1;
                    viewPostDialog.makeThumbsUp(post.likes);
                }
            }
        });

        viewPostDialog.thumbDownButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (post.thumbs == -1) {
                    post.likes++;
                    post.thumbs = 0;
                    viewPostDialog.makeThumbsNeutral(post.likes);
                } else {
                    post.likes += -1 - post.thumbs;
                    post.thumbs = -1;
                    viewPostDialog.makeThumbsDown(post.likes);
                }
            }
        });

        viewPostDialog.commentButton.setOnClickListener(new View.OnClickListener() {
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
        makeRequestNumLikes(post.post_id, viewPostDialog.numLikes);
    }

    /**
     * Show menu of options when users click the fab.
     */
    public void showFabMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing?
                dialog.dismiss();
            }
        })
                .setView(getLayoutInflater().inflate(R.layout.simple_list_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        ListView fabMenu = (ListView) dialog.findViewById(R.id.listView);

        String[] menuArr = {getString(R.string.newPost), getString(R.string.myLocation),
                getString(R.string.changeMapRange), getString(R.string.refreshMap), getString(R.string.recentPosts)};
        final MenuListArrayAdapter menuAdapter = new MenuListArrayAdapter(MapsActivity.this, menuArr);
        fabMenu.setAdapter(menuAdapter);

        fabMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        showNewPostDialog();
                        break;
                    case 1:
                        moveCamera();
                        break;
                    case 2:
                        showMapRangeDialog();
                        break;
                    case 3:
                        clearMap();
                        makeRequestGetNearbyPosts();
                        break;
                    case 4:
                        makeRequestGetRecentPosts();
                        break;

                }
                dialog.dismiss();
            }
        });
    }

    /**
     * Prompt user to enter new range for the map.
     * On complete, get nearby posts using the new range.
     */
    public void showMapRangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setView(getLayoutInflater().inflate(R.layout.seekbar_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        final SeekBar rangeSeekBar = (SeekBar) dialog.findViewById(R.id.seekBar);
        rangeSeekBar.setMax((int)(MAX_RANGE * 100));
        rangeSeekBar.setProgress((int)(currRange * 100));

        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "currRange = " + currRange);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "currRange = " + currRange);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currRange = (double)rangeSeekBar.getProgress()/100;
            }
        });

        Button doneButton = (Button) dialog.findViewById(R.id.buttonDone);

        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(TAG, "currRange = " + currRange);

                clearMap();
                makeRequestGetNearbyPosts();
                dialog.dismiss();
            }
        });
    }

    public void clearMap() {
        postHashMap.clear();
        mMap.clear();
    }

    public void showNewPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.new_post_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        final ImageView pokemonImageView = (ImageView) dialog.findViewById(R.id.imageViewNewPost);

        final EditText captionEditText = (EditText) dialog.findViewById(R.id.editTextCaption);

        final Spinner pokemonSpinner = (Spinner) dialog.findViewById(R.id.spinnerPokemon);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pokemon_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pokemonSpinner.setAdapter(adapter);

        // Change the pokemon image every time they select an item from the spinner
        pokemonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the right image for the post
                ImageRequest request = new ImageRequest(IMAGE_URL_BASE + (pokemonSpinner.getSelectedItemPosition() + 1) + ".png",
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                Bitmap resized = PokeGoPost.getResizedBitmap(bitmap, bitmap.getWidth() * 3, bitmap.getHeight() * 3);
                                pokemonImageView.setImageBitmap(resized);
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                // Do nothing
                            }
                        });
                RequestQueue requestQueue = VolleySingleton.getInstance(MapsActivity.this).getRequestQueue();
                requestQueue.add(request);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        final Button postButton = (Button) dialog.findViewById(R.id.buttonPost);

        final CheckBox onlyVisibleTeamCheckBox = (CheckBox) dialog.findViewById(R.id.checkBoxOnlyVisibleTeam);
        onlyVisibleTeamCheckBox.setText("Only visible to " + user.team + " team");

        postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isCurrentLocationAvailable()) {
                    String newPostTitle = "I found a " + pokemonSpinner.getSelectedItem().toString() + "!";
                    Log.i(TAG, "Selected Item: " + pokemonSpinner.getSelectedItem().toString());
                    String newPostCaption = captionEditText.getText().toString();
                    double newPostLatitude = locationListener.getCurrLocation().getLatitude();
                    double newPostLongitude = locationListener.getCurrLocation().getLongitude();
                    boolean newPostOnlyVisibleTeam = onlyVisibleTeamCheckBox.isChecked();
                    makeRequestNewPost(newPostTitle, newPostCaption, newPostLatitude, newPostLongitude, newPostOnlyVisibleTeam);
                    dialog.dismiss();
                }
            }
        });
    }

    private void makeRequestNewPost(final String title, final String caption, final double latitude, final double longitude, final boolean onlyVisibleTeam) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "https://wandr-app.io/pokemon/new_post.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);

                            int postId = responseJSON.getInt("post_id");
                            worker.recordPostThumbs(postId, "UP", postThumbsMap);
                            // Create a PokeGoPost object and add it to the map
                            PokeGoPost newPost = new PokeGoPost(postId,
                                    user.getUsername(), user.team,
                                    title, caption, latitude, longitude, onlyVisibleTeam);
                            loadedPosts.add(newPost);
                            addPostMarkers();
                            moveCamera();
                        } catch (Exception e) {
                            Log.e(TAG, "Error making new post", e);
                            Toast.makeText(MapsActivity.this, "Something went wrong.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MapsActivity.this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("username", user.getUsername());
                map.put("password", user.getPassword());
                map.put("title", title);
                map.put("team", user.team.toString());
                map.put("caption", caption);
                map.put("latitude", "" + latitude);
                map.put("longitude", "" + longitude);
                map.put("only_visible_team", "" + (onlyVisibleTeam ? 1 : 0));
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
                        try {
                            JSONObject responseJSON = new JSONObject(response);

                            numLikesText.setText(CommonUtils.getNumLikesString(responseJSON
                                    .getInt("likes")));

                            for (PokeGoPost post : postHashMap.values()) {
                                if (post.post_id == post_id) {
                                    post.likes = responseJSON.getInt("likes");
                                }
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error getting num likes", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error getting num likes", error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("post_id", "" + post_id);
                map.put("username", user.getUsername());
                return map;
            }
        };
        requestQueue.add(request);
    }

    private void makeRequestActionPost(final int post_id, final String content, final ListView commentsList) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "https://wandr-app.io/pokemon/action_post.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);

                            // Need to refresh comment list
                            CommentsListArrayAdapter commentsAdapter = (CommentsListArrayAdapter) commentsList.getAdapter();
                            commentsAdapter.makeRequestGetComments(content);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Something went wrong.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MapsActivity.this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("post_id", "" + post_id);
                map.put("username", user.getUsername());
                map.put("password", user.getPassword());
                map.put("content", content);
                return map;
            }
        };
        requestQueue.add(request);
    }

    private void makeRequestChangePostLikes(final int post_id, final String post_user_id, final int change) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, "https://wandr-app.io/pokemon/like_post.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Should we even tell user that it failed?
                        /*try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(MapsActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Something went wrong on response.",
                                    Toast.LENGTH_SHORT).show();
                        }*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MapsActivity.this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("post_id", "" + post_id);
                map.put("post_user_id", post_user_id);
                map.put("username", user.getUsername());
                map.put("password", user.getPassword());
                map.put("change", "" + change);
                return map;
            }
        };
        requestQueue.add(request);
    }
}
