package io.wandr_app.pokemongosocial;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private Button newUserButton;
    private Button loginButton;
    private EditText usernameEditText;
    private EditText passwordEditText;

    String newUsername;
    String newPassword;
    String newTeam;

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,15}$";
    private Pattern pattern;
    private Matcher matcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usernameEditText = (EditText) findViewById(R.id.editTextUsername);
        passwordEditText = (EditText) findViewById(R.id.editTextPassword);

        newUserButton = (Button) findViewById(R.id.buttonNewUser);
        newUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                showNewUserDialog();
            }
        });

        loginButton = (Button) findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                makeRequestLogin(username, password);
            }
        });

        pattern = Pattern.compile(USERNAME_PATTERN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeRequestLogin(final String username, final String password) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, "https://wandr-app.io/pokemon/login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        // Get the JSON Response
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(LoginActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                            if (responseJSON.getInt("success") == 1) {
                                // Start maps activity
                                Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
                                myIntent.putExtra("username", username);
                                myIntent.putExtra("password", password);
                                LoginActivity.this.startActivity(myIntent);
                            }
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "Something went wrong.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String,String> map = new HashMap<>();
                map.put("username", username);
                map.put("password", password);
                return map;
            }
        };
        requestQueue.add(request);
    }

    public void showNewUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New User")
                .setView(getLayoutInflater().inflate(R.layout.new_user_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        final EditText userNameEditText = (EditText) dialog.findViewById(R.id.editTextUsername);
        final EditText passwordEditText = (EditText) dialog.findViewById(R.id.editTextPassword);
        final EditText passwordConfirmEditText = (EditText) dialog.findViewById(R.id.editTextConfirmPassword);

        Button doneButton = (Button) dialog.findViewById(R.id.buttonDone);

        final Spinner teamSpinner = (Spinner) dialog.findViewById(R.id.spinnerTeam);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.team_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);

        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                newUsername = userNameEditText.getText().toString();
                newPassword = passwordEditText.getText().toString();
                newTeam = teamSpinner.getSelectedItem().toString();
                String confirmPassword = passwordConfirmEditText.getText().toString();
                if (newPassword.equals(confirmPassword)) {
                    if (isValidName(newUsername)) {
                        makeRequestNewUser(dialog);
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid username! Only letters, numbers, dashes and underscores are allowed.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Passwords don't match!",
                            Toast.LENGTH_SHORT).show();
                    passwordEditText.setText("");
                    passwordConfirmEditText.setText("");
                }
            }
        });
    }

    private void makeRequestNewUser(final AlertDialog dialog) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, "https://wandr-app.io/pokemon/new_user.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get the JSON Response
                        System.out.println("New user response: " + response);
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            Toast.makeText(LoginActivity.this, responseJSON.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                            if (responseJSON.getInt("success") == 1) {
                                dialog.dismiss();
                                // Start Maps Activity with that new user
                                Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
                                myIntent.putExtra("username", newUsername);
                                myIntent.putExtra("password", newPassword);
                                LoginActivity.this.startActivity(myIntent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Something went wrong.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(LoginActivity.this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String,String> map = new HashMap<>();
                map.put("profile_image_path", newTeam + "_pic.jpg");
                map.put("username", newUsername);
                map.put("password", newPassword);
                map.put("team", newTeam);
                return map;
            }
        };
        requestQueue.add(request);
    }

    /**
     * Checks for valid username using regex
     * Allowed are letters, numbers, dashes and underscores.
     * @param name the desired username
     * @return true if valid, false if not
     */
    boolean isValidName(String name) {
        matcher = pattern.matcher(name);
        return matcher.matches();
    }
}
