package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class register extends AppCompatActivity implements dialogue.DialogueListener {

    EditText email,password,name;
    TextView status;
    ImageView show_password_btn,edit_status_btn;
    CircleImageView display_img;
    FloatingActionButton add_img;
    Button regn_btn;
    ProgressBar progressBar;
    Uri image_uri=null;
    String s=null;
    String uid;
    String image_url="default";
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    FirebaseUser user;
    StorageReference firebaseStorage=FirebaseStorage.getInstance().getReference();
    int c=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email=findViewById(R.id.regn_email_txt);
        password=findViewById(R.id.regn_password_txt);
        name=findViewById(R.id.regn_name_txt);
        status=findViewById(R.id.status_txt);
        show_password_btn=findViewById(R.id.show_regn_password);
        edit_status_btn=findViewById(R.id.status_edit);
        display_img=findViewById(R.id.regn_image);
        add_img=findViewById(R.id.add_img_regn);
        regn_btn=findViewById(R.id.regn_btn);
        progressBar=findViewById(R.id.regn_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);
        s="Hi! Let's start a gossip.";

        //status fetching process

        edit_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogue();
            }
        });

        //registration process
        regn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String e,p,n;
                n=name.getText().toString();
                e=email.getText().toString();
                p=password.getText().toString();
                if(e.isEmpty()){
                    email.setError("Required");
                    email.requestFocus();
                }
                if(p.isEmpty()){
                    password.setError("Required");
                    password.requestFocus();
                }
                if(n.isEmpty()){
                    name.setError("Required");
                    name.requestFocus();
                }
                if(image_uri==null){
                    Toast.makeText(register.this, "Profile image required.", Toast.LENGTH_LONG).show();
                }
                if(!(image_uri==null||n.isEmpty()||p.isEmpty()||e.isEmpty())){
                    progressBar.setVisibility(View.VISIBLE);
                    email.setEnabled(false);
                    email.setClickable(false);
                    name.setEnabled(false);
                    name.setClickable(false);
                    password.setEnabled(false);
                    password.setClickable(false);
                    regn_btn.setEnabled(false);
                    regn_btn.setClickable(false);
                    mAuth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            //updating all users
                                            uid=mAuth.getCurrentUser().getUid();
                                            HashMap<String,String>dt=new HashMap<>();
                                            dt.put("UID",uid);
                                            db.collection("all-users").document(e).set(dt);

                                            //making user's profile

                                            final StorageReference ref=firebaseStorage.child("profile_images").child(mAuth.getCurrentUser().getUid()+".jpg");
                                            UploadTask uploadTask=ref.putFile(image_uri);
                                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            image_url=String.valueOf(uri);

                                                            HashMap<String ,String>mp=new HashMap<>();
                                                            mp.put("name",n);
                                                            mp.put("status",s);
                                                            mp.put("image",image_url);
                                                            uid= mAuth.getCurrentUser().getUid();

                                                            db.collection("users").document(uid).set(mp).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        email.setEnabled(true);
                                                                        email.setClickable(true);
                                                                        name.setEnabled(true);
                                                                        name.setClickable(true);
                                                                        password.setEnabled(true);
                                                                        password.setClickable(true);
                                                                        regn_btn.setEnabled(true);
                                                                        regn_btn.setClickable(true);
                                                                        Toast.makeText(register.this, "Please verify your email and login again.", Toast.LENGTH_LONG).show();
                                                                        Handler handler=new Handler();
                                                                        handler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                Intent intent=new Intent(register.this,Login.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        },1500);
                                                                    }else{
                                                                        Toast.makeText(register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }else{
                                            Toast.makeText(register.this,task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else{
                                progressBar.setVisibility(View.INVISIBLE);
                                email.setEnabled(true);
                                email.setClickable(true);
                                name.setEnabled(true);
                                name.setClickable(true);
                                password.setEnabled(true);
                                password.setClickable(true);
                                regn_btn.setEnabled(true);
                                regn_btn.setClickable(true);
                                Toast.makeText(register.this,task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        //show and hide password
        show_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c==1) {
                    password.setTransformationMethod(null);
                }else{
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }
                c=c*-1;
            }
        });

        //cropping image for profile
        add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(register.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(register.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(register.this);
                    }
                }
            }
        });
    }

    private void openDialogue() {
        dialogue dialogue=new dialogue();
        dialogue.show(getSupportFragmentManager(),"dialogue");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                image_uri=resultUri;
                display_img.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void applyText(String st) {
        status.setText(st);
        s=st;
    }
}