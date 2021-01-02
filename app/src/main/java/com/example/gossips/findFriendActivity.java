package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class findFriendActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText friendEmail;
    ImageView search;
    CircleImageView profile_image;
    TextView friendName,friendStatus;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser user=mAuth.getCurrentUser();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    FirebaseStorage storage=FirebaseStorage.getInstance();
    StorageReference reference;
    ConstraintLayout friendProfile;
    ProgressBar progressBar;
    String user_id,friend_id,e;
    String s,n,i;
    String Uname,Ustatus,Uimage;
    Uri image_uri=null;
    Button request;
    boolean state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        friendEmail=findViewById(R.id.friend_email_txt);
        search=findViewById(R.id.friend_search_btn);
        profile_image=findViewById(R.id.friend_image);
        friendName=findViewById(R.id.friend_name);
        friendStatus=findViewById(R.id.friend_status);
        toolbar=findViewById(R.id.find_friend_toolbar);
        friendProfile=findViewById(R.id.friend_profile);
        request=findViewById(R.id.request_btn);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Gossips");
        getSupportActionBar().setSubtitle("Say Hello!");
        user_id=user.getEmail();
        progressBar=findViewById(R.id.friend_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);
        friendProfile.setVisibility(View.INVISIBLE);
        friendProfile.setEnabled(false);

        //fetching user data
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Ustatus=documentSnapshot.getString("status");
                    Uname=documentSnapshot.getString("name");
                    Uimage=documentSnapshot.getString("image");
                }else{
                    Uname=null;
                    Ustatus=null;
                    Uimage=null;
                }
            }
        });


        //searching friend
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e=friendEmail.getText().toString();
                if(e.isEmpty()){
                    Toast.makeText(findFriendActivity.this, "Please give e-mail ID.", Toast.LENGTH_LONG).show();
                }
                if(e.equals(user_id)){
                    Toast.makeText(findFriendActivity.this,"Abe Saale", Toast.LENGTH_SHORT).show();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    db.collection("all-users").document(e).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(!documentSnapshot.exists()){
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(findFriendActivity.this, "No such user exist", Toast.LENGTH_SHORT).show();
                            }else{
                                friend_id=documentSnapshot.getString("UID");
                                if(isFriend(friend_id)){

                                }else{
                                    request.setText("Send Request");
                                }
                                setUserProfile(friend_id);
                            }
                        }
                    });
                }
            }
        });

        //sending /cancelling friend request
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("users").document(friend_id).collection("requests").document(user.getUid()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    db.collection("users").document(friend_id).collection("requests").document(user.getUid())
                                            .delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        state=false;
                                                        request.setText("Send request");
                                                        Toast.makeText(findFriendActivity.this, "Friend request cancelled!", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(findFriendActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }else{
                                    HashMap<String,String> mp=new HashMap<>();
                                    mp.put("name",Uname);
                                    mp.put("image",Uimage);
                                    mp.put("status",Ustatus);
                                    mp.put("uid",mAuth.getCurrentUser().getUid());
                                    db.collection("users").document(friend_id).collection("requests").document(user.getUid()).set(mp)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        state=true;
                                                        request.setText("Cancel request");
                                                    }else{
                                                        Toast.makeText(findFriendActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });

    }

    private boolean isFriend(final String friend_id) {

        db.collection("users").document(friend_id).collection("requests").document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    request.setText("Cancel request");
                }else{
                    db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).collection("friends")
                            .document(friend_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                request.setText("Remove");
                            }else {
                                request.setText("Send request");    
                            }
                        }
                    });
                    //request.setText("Send request");
                }
            }
        });
        return state;
    }

    private void setUserProfile(String id) {
        db.collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    s=documentSnapshot.getString("status");
                    n=documentSnapshot.getString("name");
                    i=documentSnapshot.getString("image");
                    friendStatus.setText(s);
                    friendName.setText(n);

                    // fetching image

                    Picasso.get().load(i).into(profile_image);
                    progressBar.setVisibility(View.INVISIBLE);

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    friendProfile.setVisibility(View.VISIBLE);
                    friendProfile.setEnabled(true);

                    /*reference=storage.getReferenceFromUrl(i);

                    try {
                        final File file=File.createTempFile("image","jpg");
                        reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                image_uri =Uri.fromFile(file);
                                profile_image.setImageURI(image_uri);
                                progressBar.setVisibility(View.INVISIBLE);

                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                friendProfile.setVisibility(View.VISIBLE);
                                friendProfile.setEnabled(true);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                }else{
                    Toast.makeText(findFriendActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                }
            }//end of onSuccess
        });
    }//end of set user profile



}