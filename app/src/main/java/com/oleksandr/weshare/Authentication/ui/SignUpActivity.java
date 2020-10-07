package com.oleksandr.weshare.Authentication.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.oleksandr.weshare.Entities.User;
import com.oleksandr.weshare.ui.MainActivity;
import com.oleksandr.weshare.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText emailInput;
    private EditText inputPassword;
    private EditText nameInput;
    private ProgressBar signup_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        checkIfLogged();
        initViews();
    }

    private void getPermissions() {
        ActivityCompat.requestPermissions(SignUpActivity.this
                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
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
        emailInput = findViewById(R.id.emailInput);
        inputPassword = findViewById(R.id.inputPassword);
        nameInput = findViewById(R.id.nameInput);
        nameInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(LoginActivity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputPassword.getWindowToken(), 0);
                    checkData(inputPassword);
                    return true;
                }
                return false;
            }
        });
        signup_progress = findViewById(R.id.signup_progress);
    }

    public static boolean isValidEmail(CharSequence target) {
        //checks if text is empty
        if (target == null) return false;
        //return validity of text as email matching with email's pattern(built-in library)
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void checkData(View view) {
            if(emailInput.getText().toString().isEmpty() || !isValidEmail(emailInput.getText().toString())) {
                emailInput.setError("Enter Valid Email");
                return;
            }
            if(inputPassword.getText().toString().isEmpty() || inputPassword.getText().toString().length() < 8){
                inputPassword.setError("Enter Strong Password Not Less Than 8 characters");
                return;
            }
            if(nameInput.getText().toString().isEmpty()){
                nameInput.setError("Enter Your Name");
                return;
            }

            final String name = nameInput.getText().toString();
            final String email = emailInput.getText().toString();
            final String password = inputPassword.getText().toString();

            signup_progress.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        addUserToDb(name, email, firebaseAuth.getCurrentUser().getUid());
                    }else{
                        signup_progress.setVisibility(View.GONE);
                        Toast.makeText(SignUpActivity.this, "Error ...! \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void addUserToDb(String name, String email, String Uid) {
        User user = new User();
        user.setEmail(email);
        user.setUser_name(name);
        user.setUid(Uid);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference(User.class.getSimpleName());
        String id = userRef.push().getKey();
        user.setID(id);
        userRef.child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    signup_progress.setVisibility(View.GONE);
                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(SignUpActivity.this, "Verification email was sent, Please Verify!", Toast.LENGTH_SHORT).show();
                            AuthUI.getInstance()
                                    .signOut(SignUpActivity.this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                                                startActivity(i);
                                                finish();
                                            }else{
                                                Toast.makeText(SignUpActivity.this, "Error ...! \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
                }
            }
        });
    }

    public void openLogin(View view) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

}