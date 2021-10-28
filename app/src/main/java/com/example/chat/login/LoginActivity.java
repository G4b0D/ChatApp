package com.example.chat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chat.MainActivity;
import com.example.chat.MessageActivity;
import com.example.chat.R;
import com.example.chat.common.Util;
import com.example.chat.password.ResetPasswordActivity;
import com.example.chat.signup.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private String email, password;
    private Button ingresar;
    private FirebaseAuth mAuth;
    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPswd);

        progressBar =findViewById(R.id.progressBar2);
    }

    public void btnLoginClick(View view) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_pswd));
        } else {
            if (Util.conectionAvailable(this)) {
                progressBar.setVisibility(View.VISIBLE);
                mAuth = FirebaseAuth.getInstance();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Intente de nuevo :" +
                                    task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            else{
                startActivity(new Intent(LoginActivity.this, MessageActivity.class));
            }
        }

    }
    public void openRegister(View view){
        startActivity(new Intent(this, SignUpActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }
    }


    public void tvResetPasswordClick(View view)
    {
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
    }

}