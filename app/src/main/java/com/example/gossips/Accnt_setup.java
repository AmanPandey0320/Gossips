package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Accnt_setup extends AppCompatActivity implements dialogue.DialogueListener {
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FloatingActionButton addImgBtn;
    CircleImageView accntImg;
    Uri image_uri=null;
    FirebaseUser user=mAuth.getCurrentUser();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    FirebaseStorage storage=FirebaseStorage.getInstance();
    StorageReference reference;
    String uid =user.getUid().toString();
    String s,n,i;
    HashMap<String,String>mp=new HashMap<>();
    TextView status;
    EditText name;
    ImageView edit_status;
    Button save;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accnt_setup);

        progressBar=findViewById(R.id.accnt_setup_progress);
        save=findViewById(R.id.save_btn);
        addImgBtn=findViewById(R.id.add_img);
        accntImg=findViewById(R.id.accnt_image);
        status=findViewById(R.id.status_txt);
        name=findViewById(R.id.name_txt);
        edit_status=findViewById(R.id.accnt_status_edit);
        Toolbar toolbar=findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Gossips");
        getSupportActionBar().setSubtitle("Say Hello!");

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        save.setClickable(false);
        save.setEnabled(false);




        //save profile changes

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                n=name.getText().toString();
                HashMap<String,String>mp=new HashMap<>();
                mp.put("name",n);
                mp.put("status",s);
                mp.put("image",i);
                db.collection("users").document(uid).set(mp).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            progressBar.setVisibility(View.INVISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(Accnt_setup.this, "Updated!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(Accnt_setup.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        //edit status process

        edit_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogue();
            }
        });

        //fetching data from cloud firestore

        db.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    s=documentSnapshot.getString("status");
                    n=documentSnapshot.getString("name");
                    i=documentSnapshot.getString("image");
                    status.setText(s);
                    name.setText(n);

                    // fetching image

                    //using picasso library
                    Picasso.get().load(i).into(accntImg);
                    save.setClickable(true);
                    save.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    // the normal firebase method
                   /* reference=storage.getReferenceFromUrl(i);

                    try {
                        final File file=File.createTempFile("image","jpg");
                        reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                image_uri =Uri.fromFile(file);
                                accntImg.setImageURI(image_uri);
                                save.setClickable(true);
                                save.setEnabled(true);
                                progressBar.setVisibility(View.INVISIBLE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                }else{
                    Toast.makeText(Accnt_setup.this, "Not found", Toast.LENGTH_SHORT).show();
                }
            }//end of onSuccess
        });

        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(Accnt_setup.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(Accnt_setup.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(Accnt_setup.this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_item,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.logout_btn){
            mAuth.signOut();
            Intent intent=new Intent(Accnt_setup.this,Login.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                image_uri=resultUri;
                reference=storage.getReferenceFromUrl(i);
                UploadTask uploadTask=reference.putFile(image_uri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                i=String.valueOf(uri);
                                Toast.makeText(Accnt_setup.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                accntImg.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void applyText(String st) {
        status.setText(st);
        s=st;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!user.isEmailVerified()){
            Toast.makeText(this, "Please, verify your Email-ID.", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(Accnt_setup.this,Login.class);
            startActivity(intent);
            finish();
        }
    }
}
