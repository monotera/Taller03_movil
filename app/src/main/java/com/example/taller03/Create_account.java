package com.example.taller03;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.taller03.model.DatabasePaths;
import com.example.taller03.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Create_account extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private EditText nameEdit, lastnameEdit, emailEdit, passEdit, numIDEdit;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        nameEdit = findViewById(R.id.name_input_create);
        lastnameEdit = findViewById(R.id.lastname_input_create);
        emailEdit = findViewById(R.id.email_input_create);
        passEdit = findViewById(R.id.pass_input_create);
        numIDEdit = findViewById(R.id.IDnum_input_create);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    @Override
    public void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser){
        if(currentUser!=null){
            Intent intent = new Intent(getBaseContext(), HomeActivity.class);
            intent.putExtra("user", currentUser.getEmail());
            startActivity(intent);
        } else {
            emailEdit.setText("");
            lastnameEdit.setText("");
            emailEdit.setText("");
            passEdit.setText("");
            numIDEdit.setText("");
        }
    }
    private void createAccount(String email, String password) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user!=null){
                                User p = new User();
                                p.setName(nameEdit.getText().toString());
                                p.setLastname(lastnameEdit.getText().toString());
                                p.setNumID(numIDEdit.getText().toString());
                                p.setAvailable(true);

                                myRef=database.getReference(DatabasePaths.USER + user.getUid());
                                myRef.setValue(p);

                                updateUI(user);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Create_account.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
        // [END create_user_with_email]
    }
    public static boolean isEmailValid(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public void onSignUpClick(View view) {
        String email = emailEdit.getText().toString();
        String pass = passEdit.getText().toString();

        //Log.w(TAG, "Entra login 11111111111111111111111111");

        if(!isEmailValid(email)){
            //Log.w(TAG, "Email no valido 22222222222222222222222222");
            Toast.makeText(Create_account.this, "Email is not a valid format",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount(email, pass);
    }
}