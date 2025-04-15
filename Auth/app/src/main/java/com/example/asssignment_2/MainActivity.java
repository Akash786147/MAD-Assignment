package com.example.asssignment_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.*;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    // Firebase & Google
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleClient;

    // UI elements
    private LottieAnimationView animationView;

    private final ActivityResultLauncher<Intent> googleLoginLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                                try {
                                    GoogleSignInAccount account = task.getResult(ApiException.class);
                                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "User Logged in", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(MainActivity.this, "pls retry" + task.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        // Google sign-in setup
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, signInOptions);
        firebaseAuth = FirebaseAuth.getInstance();

        // Optional: Uncomment to always log out
        // googleClient.signOut();
        // firebaseAuth.signOut();

//        // Animation
//        animationView = findViewById(R.id.Lottie2);
//        animationView.playAnimation();

        // Sign-in button
        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleClient.getSignInIntent();
                googleLoginLauncher.launch(signInIntent);
            }
        });
    }
}