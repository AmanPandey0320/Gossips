package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login<string> extends AppCompatActivity {

    ImageView imageView;
    EditText password_txt;
    EditText email_txt;
    Button login;
    Button reset;
    TextView regn;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    View view;
    int c=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        regn=findViewById(R.id.regn_page_btn);
        mAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.login_progress_bar);
        reset=findViewById(R.id.reset_btn);
        login=findViewById(R.id.login_btn);
        email_txt=findViewById(R.id.email_txt);
        password_txt=findViewById(R.id.password_txt);
        imageView=findViewById(R.id.show_password);
        view=findViewById(R.id.splash_screen);
        progressBar.setVisibility(View.INVISIBLE);

        //show and hide pass word
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c==1) {
                    password_txt.setTransformationMethod(null);
                }else{
                    password_txt.setTransformationMethod(new PasswordTransformationMethod());
                }
                c=c*-1;
            }
        });

        //login process

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String e,p;
                e=email_txt.getText().toString();
                p=password_txt.getText().toString();
                if(e.isEmpty()){
                    email_txt.setError("Required");
                    email_txt.requestFocus();
                }
                if(p.isEmpty()){
                    password_txt.setError("Required");
                    password_txt.requestFocus();
                }
                if(!(p.isEmpty()||e.isEmpty())){
                    progressBar.setVisibility(View.VISIBLE);
                    email_txt.setEnabled(false);
                    email_txt.setClickable(false);
                    password_txt.setEnabled(false);
                    password_txt.setClickable(false);
                    login.setClickable(false);
                    login.setEnabled(false);
                    reset.setEnabled(false);
                    reset.setClickable(false);
                    regn.setEnabled(false);
                    regn.setClickable(false);
                    mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            email_txt.setEnabled(true);
                            email_txt.setClickable(true);
                            password_txt.setEnabled(true);
                            password_txt.setClickable(true);
                            login.setClickable(true);
                            login.setEnabled(true);
                            reset.setEnabled(true);
                            reset.setClickable(true);
                            regn.setEnabled(true);
                            regn.setClickable(true);
                            if(task.isSuccessful()){
                                if(mAuth.getCurrentUser().isEmailVerified()){
                                    Intent intent=new Intent(Login.this,ChatActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(Login.this,"E-mail not verified!:(",Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            }else{
                                Toast.makeText(Login.this,task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(Login.this, "Please give required details.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //reset process

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e;
                e=email_txt.getText().toString();
                if(e.isEmpty()){
                    email_txt.setError("Required");
                    email_txt.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    email_txt.setEnabled(false);
                    email_txt.setClickable(false);
                    password_txt.setEnabled(false);
                    password_txt.setClickable(false);
                    login.setClickable(false);
                    login.setEnabled(false);
                    reset.setEnabled(false);
                    reset.setClickable(false);
                    mAuth.sendPasswordResetEmail(e).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            progressBar.setVisibility(View.INVISIBLE);
                            email_txt.setEnabled(true);
                            email_txt.setClickable(true);
                            password_txt.setEnabled(true);
                            password_txt.setClickable(true);
                            login.setClickable(true);
                            login.setEnabled(true);
                            reset.setEnabled(true);
                            reset.setClickable(true);

                            if(task.isSuccessful()){
                                Toast.makeText(Login.this, "Check your E-mail ID to reset password and login again.", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(Login.this,task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        //sending user for registration

        regn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Login.this,register.class);
                startActivity(intent);
            }
        });
    }
}
