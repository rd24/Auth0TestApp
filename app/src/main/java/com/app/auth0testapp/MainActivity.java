package com.app.auth0testapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView credentialsView = findViewById(R.id.credentials);
        Button logoutButton = findViewById(R.id.logout);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        //Obtain the token from the Intent's extras
        String accessToken = getIntent().getStringExtra(LogInActivity.EXTRA_ACCESS_TOKEN);
        credentialsView.setText(accessToken);

    }

    private void logout() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.putExtra(LogInActivity.KEY_CLEAR_CREDENTIALS, true);
        startActivity(intent);
        finish();
    }
}
