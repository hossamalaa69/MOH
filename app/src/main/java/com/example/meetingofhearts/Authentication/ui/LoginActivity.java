package com.example.meetingofhearts.Authentication.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetingofhearts.Entities.User;
import com.example.meetingofhearts.ui.MainActivity;
import com.example.meetingofhearts.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.meetingofhearts.Authentication.ui.SignUpActivity.isValidEmail;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;
    private static final String TAG = "LoginActivity";
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private EditText emailInput;
    private EditText inputPassword;
    private ProgressBar signin_progress;
    private SignInButton sign_in_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkIfLogged();
        initGoogleOptions();
        initViews();

    }

    private void initGoogleOptions() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void checkIfLogged() {
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput2);
        inputPassword = findViewById(R.id.inputPassword2);
        inputPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(LoginActivity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputPassword.getWindowToken(), 0);
                    checkDataLogin(inputPassword);
                    return true;
                }
                return false;
            }
        });
        signin_progress = findViewById(R.id.signin_progress);
        sign_in_button = findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }

    public void checkDataLogin(View view) {
        if(emailInput.getText().toString().isEmpty() || !isValidEmail(emailInput.getText().toString())) {
            emailInput.setError("Enter Valid Email");
            return;
        }
        if(inputPassword.getText().toString().isEmpty() || inputPassword.getText().toString().length() < 8){
            inputPassword.setError("Enter Strong Password Not Less Than 8 characters");
            return;
        }

        String email = emailInput.getText().toString();
        String password = inputPassword.getText().toString();
        signin_progress.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                signin_progress.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    //if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    //}else{
                    //    Toast.makeText(LoginActivity.this, "Please, Verify Your Email", Toast.LENGTH_SHORT).show();
                    //}
                }else{
                    Toast.makeText(LoginActivity.this, "Error ...! \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void openSignUp(View view) {
        Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(i);
        finish();
    }

    public void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if(task.isSuccessful()){
                try {
                    signin_progress.setVisibility(View.VISIBLE);
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Toast.makeText(LoginActivity.this, "Error..! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            addToDb(firebaseUser.getDisplayName(), firebaseUser.getEmail(), firebaseUser.getUid());
                        } else {
                            signin_progress.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Error..! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addToDb(String name, String email, String Uid) {
        final User user = new User();
        user.setEmail(email);
        user.setUser_name(name);
        user.setUid(Uid);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference(User.class.getSimpleName());
        userRef.orderByChild("uid").equalTo(Uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0){
                    openHome();
                }else{
                    String id = userRef.push().getKey();
                    user.setID(id);
                    userRef.child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                openHome();
                            }
                        }
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                signin_progress.setVisibility(View.GONE);
            }
        });
    }

    private void openHome() {
        signin_progress.setVisibility(View.GONE);
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void forgetPassword(View view) {
        if(emailInput.getText().toString().isEmpty() || !isValidEmail(emailInput.getText().toString())) {
            emailInput.setError("Enter Valid Email");
            return;
        }

        signin_progress.setVisibility(View.VISIBLE);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference(User.class.getSimpleName());
        userRef.orderByChild("email").equalTo(emailInput.getText().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0){
                    sendResetPassword(emailInput.getText().toString());
                }else{
                    Toast.makeText(LoginActivity.this, "Not Signed Up...!", Toast.LENGTH_SHORT).show();
                    signin_progress.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                signin_progress.setVisibility(View.GONE);
            }
        });
    }

    private void sendResetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                signin_progress.setVisibility(View.GONE);
                if(task.isSuccessful())
                    Toast.makeText(LoginActivity.this, "Reset Email Sent Successfully...!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(LoginActivity.this, "Failed Sending Email...!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}