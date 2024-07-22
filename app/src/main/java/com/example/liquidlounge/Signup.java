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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {
    boolean showPass, showPass2, validated;

    EditText name, phone, mail, password, confirm;
    Button signup;
    TextView login;
    ImageView show, show2, quest;

    FirebaseUser currUser;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ActionBar ab = getSupportActionBar();
        ab.hide();

        showPass = false;
        showPass2 = false;
        validated = false;

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        mail = findViewById(R.id.mail);
        password = findViewById(R.id.pass);
        confirm = findViewById(R.id.confirm);
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.login);
        show = findViewById(R.id.showPass);
        show2 = findViewById(R.id.showPass2);
        quest = findViewById(R.id.question);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        signup.setOnClickListener(view -> {
            String nameStr = name.getText().toString().strip().toUpperCase();
            String phoneStr = phone.getText().toString();
            String mailStr = mail.getText().toString().strip().toLowerCase();
            String passStr = password.getText().toString();
            String confirmStr = confirm.getText().toString();

            fieldValidation(nameStr, mailStr, passStr, confirmStr);

            if(validated){
                // Create User - Firebase Authentication
                mAuth.createUserWithEmailAndPassword(mailStr, passStr).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Set display name and phone number
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nameStr).build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("STATUS", "User display name updated.");
                                        }
                                    });

                            // Create a reference to the user's document in Firestore
                            DocumentReference userRef = fStore.collection("users").document(user.getUid());

                            // Create data object in Firestore
                            Map<String, Object> data = new HashMap<>();
                            data.put("ID", user.getUid());
                            if(user.getDisplayName() != null)
                                data.put("Name", user.getDisplayName());
                            else data.put("Name", nameStr);
                            data.put("Email", user.getEmail());
                            data.put("Phone Number", phoneStr);
                            data.put("isUser", "1");

                            // Add a new document with a generated ID
                            userRef.set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Registration", "User data added successfully");
                                    }).addOnFailureListener(e -> {
                                        Log.w("Error adding", "Error adding document", e);
                                    });

                        } else {
                            // If user object not found
                            Toast.makeText(this, "User object not found", Toast.LENGTH_SHORT).show();
                        }
                        // If sign up fails, display a message to the user.
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        // Go to home page
                        Intent intent = new Intent(this, UserHome.class);
                        startActivity(intent);
                        finish();
                    } else{
                        // If sign up fails, display a message to the user.
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        login.setOnClickListener(view -> {
            Intent intent = new Intent(this, UserLogin.class);
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

        show2.setOnClickListener(view -> {
            if(!showPass2) {
                show2.setImageResource(R.drawable.hide);
                confirm.setTransformationMethod(null);
            } else {
                show2.setImageResource(R.drawable.show);
                confirm.setTransformationMethod(new PasswordTransformationMethod());
            }
            // maintain cursor position
            confirm.setSelection(confirm.getText().length());
            showPass2 = !showPass2; // update state
        });

        quest.setOnClickListener(view -> {
            showPassRequirements();
        });
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

    public void fieldValidation(String name, String mail, String pass, String confirm){
        if(name.isEmpty()|mail.isEmpty()|pass.isEmpty()|confirm.isEmpty()){
            Toast.makeText(this,"Empty Field!", Toast.LENGTH_SHORT).show();
        }
        else if (!validName(name)){
            Toast.makeText(this, "Invalid Name!!", Toast.LENGTH_SHORT).show();
            this.name.requestFocus();
        }
        else if (!validEmail(mail)){
            Toast.makeText(this, "Invalid Email!!", Toast.LENGTH_SHORT).show();
            this.mail.requestFocus();
        }
        else if (!validPassword(pass)){
            Toast.makeText(this, "Password Fails Requirements!!", Toast.LENGTH_SHORT).show();
            this.password.requestFocus();
        }
        else if (!validConfirm(pass, confirm)) {
            Toast.makeText(this, "Passwords Do Not Match!!", Toast.LENGTH_SHORT).show();
            this.confirm.requestFocus();
        }
        else {
            validated = true;
            Log.d("Validation", "Passed");
        }
    }

    public boolean validName(String nm){
        for(char ch : nm.toCharArray()){
            if(Character.isDigit(ch)){
                return false;
            }
            else if(isSpecialChar(ch)) {
                if (ch == ' ') {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    public boolean validConfirm(String pass, String confirm){
        // check if password in confirmed correctly
        return pass.equals(confirm);
    }

    public boolean validEmail(String mail) {
        // regular expression for validating email address
        String emailRegEx = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
        Pattern pattern = Pattern.compile(emailRegEx);
        Matcher matcher = pattern.matcher(mail);
        return matcher.matches();
    }

    public boolean validPassword(String pass) {
        // check password length
        if (pass.length() < 8) {
            return false;
        }

        // check for other conditions
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasSpecialChar = false;
        boolean hasDigit = false;

        for(char ch : pass.toCharArray()){
            if(Character.isUpperCase(ch)){
                hasUppercase = true;
            }
            else if (Character.isLowerCase(ch)){
                hasLowercase = true;
            }
            else if (Character.isDigit(ch)){
                hasDigit = true;
            }
            else if (isSpecialChar(ch)){
                hasSpecialChar = true;
            }
        }
        // all conditions must hold true
        return hasUppercase && hasLowercase && hasSpecialChar && hasDigit;
    }

    public boolean isSpecialChar(char ch){
        //Define set of special characters
        String special = "!@#$%^&*()-_=+[]{};:'\"\\|,.<>/? ";
        return special.contains(String.valueOf(ch));
    }
}