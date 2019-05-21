package com.app.auth0testapp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.authentication.storage.CredentialsManagerException;
import com.auth0.android.authentication.storage.SecureCredentialsManager;
import com.auth0.android.authentication.storage.SharedPreferencesStorage;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;

public class LogInActivity extends AppCompatActivity {

    private Auth0 auth0;
    private SecureCredentialsManager credentialsManager;
    public static final String KEY_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS";
    public static final String EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN";
    public static final String EXTRA_ID_TOKEN = "com.auth0.ID_TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button logInButton = findViewById(R.id.loginButton);

        //Setup CredentialsManager
        auth0 = new Auth0(this);
        auth0.setLoggingEnabled(true);
        auth0.setOIDCConformant(true);
        credentialsManager = new SecureCredentialsManager(this, new AuthenticationAPIClient(auth0), new SharedPreferencesStorage(this));

        // Check if the activity was launched after a logout
        if (getIntent().getBooleanExtra(KEY_CLEAR_CREDENTIALS, false)) {
            credentialsManager.clearCredentials();

            //Call logout url in the web browser to Logout as there's a bug in clearCredentials() for the LogOut
           String url = "https://"+getResources().getString(R.string.com_auth0_domain)+"/v2/logout?federated&returnTo=https://www.google.com/accounts/logout"+"&client_id="+getResources().getString(R.string.com_auth0_client_id);

            Log.e("LOGOUT URL",url);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        // Check if a log in button must be shown
        if (!credentialsManager.hasValidCredentials()) {
            logInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logIn();
                }
            });
            return;
        }

        // Obtain the existing credentials and move to the next activity
        credentialsManager.getCredentials(new BaseCallback<Credentials, CredentialsManagerException>() {
            @Override
            public void onSuccess(final Credentials credentials) {
                goToMainActivity(credentials);
            }

            @Override
            public void onFailure(CredentialsManagerException error) {
                //Authentication cancelled by the user. Exit the app
                finish();
            }
        });
    }

    /**
     * Override required when setting up Local Authentication in the Credential Manager
     * Refer to SecureCredentialsManager#requireAuthentication method for more information.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (credentialsManager.checkAuthenticationResult(requestCode, resultCode)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void logIn() {
        WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(this, webCallback);
    }

    private final AuthCallback webCallback = new AuthCallback() {
        @Override
        public void onFailure(@NonNull final Dialog dialog) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }

        @Override
        public void onFailure(AuthenticationException exception) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LogInActivity.this, "Log In - Error Occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onSuccess(@NonNull Credentials credentials) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LogInActivity.this, "Log In - Success", Toast.LENGTH_SHORT).show();
                }
            });
            credentialsManager.saveCredentials(credentials);

            goToMainActivity(credentials);
        }
    };

    private void goToMainActivity(Credentials credentials) {
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        intent.putExtra(EXTRA_ACCESS_TOKEN, credentials.getAccessToken());
        intent.putExtra(EXTRA_ID_TOKEN, credentials.getIdToken());
        startActivity(intent);
        finish();
    }

}
