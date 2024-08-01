package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserLogin extends AppCompatActivity {
    boolean showPass = false;

    EditText email, password;
    TextView signup;
    Button login;
    ImageView show;
    Intent intent;

    FirebaseUser currUser;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        ActionBar ab = getSupportActionBar();
        ab.hide();

        email = findViewById(R.id.mail);
        password = findViewById(R.id.pass);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signUp);
        show = findViewById(R.id.showPass);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        login.setOnClickListener(view -> {
            String mailStr = email.getText().toString().strip().toLowerCase();
            String passStr = password.getText().toString();

            if (mailStr.isEmpty() || passStr.isEmpty()){
                Toast.makeText(this, "Empty Field!!", Toast.LENGTH_SHORT).show();
            }
            else {
                // Login Existing User - Firebase Authentication
                mAuth.signInWithEmailAndPassword(mailStr, passStr).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currUser = mAuth.getCurrentUser();
                        // Sign In Success
                        if (currUser != null){
                            // Check user access level
                            checkUserAccess(currUser.getUid());
                        }
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(this, "Sign in Failed!!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        signup.setOnClickListener(view -> {
            Intent intent = new Intent(this, Signup.class);
            startActivity(intent);
        });

        show.setOnClickListener(view -> {
            if(!showPass) {
                show.setImageResource(R.drawable.hide);
                password.setTransformationMethod(null);
            } else {
                show.setImageResource(R.drawable.show);
                password.setTransformationMethod(new PasswordTransformationMethod());
            }
            // maintain cursor position
            password.setSelection(password.getText().length());
            showPass = !showPass; // update state
        });
    }

    private void checkUserAccess(String uid) {
        if(uid.equals("sXOXGtDPGSVTjiXE5dlkmK8fyTx2")){
            Toast.makeText(this,"Admin Recognized",Toast.LENGTH_SHORT).show();
            intent = new Intent(this, AdminHome.class);
        }
        else{
            Toast.makeText(this, "Signed In",Toast.LENGTH_SHORT).show();
            intent = new Intent(this, UserHome.class);
        }
        startActivity(intent);
        finish();
    }

    public void showPassRequirements(){
        AlertDialog.Builder req = new AlertDialog.Builder(this);
        String req1 = "-> 8+ characters.\n";
        String req2 = "-> 1+ uppercase letter.\n";
        String req3 = "-> 1+ lowercase letter.\n";
        String req4 = "-> 1+ special character\n";
        String req5 = "-> 1+ digits\n";

        StringBuilder message = new StringBuilder();

        message.append(req1).append(req2).append(req3).append(req4).append(req5);
        req.setMessage(message);
        req.setTitle("Password Requirements!");
        req.setCancelable(true);

        AlertDialog alert = req.create();
        alert.show();
    }

    @Override
    public void onStart() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        super.onStart();
        Intent intent;

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currUser = mAuth.getCurrentUser();
        if(currUser != null){
            if (currUser.getUid().equals("sXOXGtDPGSVTjiXE5dlkmK8fyTx2"))
                intent = new Intent(this, AdminHome.class);
            else {
                intent = new Intent(this, UserHome.class);
            }
            startActivity(intent);
            finish();
        }
    }
}