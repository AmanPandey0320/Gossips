package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class messageActivity extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView msg_image;
    TextView msg_name;
    EditText msg;
    ImageView send_btn,img_msg,add_img;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String fid,uid,name,image;
    RecyclerView recyclerView;
    FirestoreRecyclerAdapter adapter;
    ImageView floatingActionButton;
    Uri imageUri=null;
    FirebaseStorage firebaseStorage;
    StorageReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //initialization
        firebaseStorage=FirebaseStorage.getInstance();
        add_img=findViewById(R.id.select_img);
        img_msg=findViewById(R.id.image_msg);
        floatingActionButton=findViewById(R.id.floatingActionButton);
        recyclerView=findViewById(R.id.message_recyclerview);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        send_btn=findViewById(R.id.send_btn);
        msg=findViewById(R.id.msg);
        msg_name=findViewById(R.id.msg_name);
        msg_image=findViewById(R.id.msg_image);
        toolbar=findViewById(R.id.msg_toolbar);
        uid= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        fid=getIntent().getStringExtra("fid");
        name=getIntent().getStringExtra("name");
        msg_name.setText(name);

        //set up msg environment
        db.collection("users").document(fid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    image=documentSnapshot.getString("image");
                    Picasso.get().load(image).into(msg_image);
                    msg_name.setText(documentSnapshot.getString("name"));
                }else{
                    startActivity(new Intent(messageActivity.this,ChatActivity.class));
                    finish();
                }
            }
        });
        //selecting image

        add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(messageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(messageActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(messageActivity.this);
                    }
                }
            }
        });

        //send msg
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String m=msg.getText().toString();
                if(m.isEmpty()){
                    Toast.makeText(messageActivity.this,"no message!", Toast.LENGTH_SHORT).show();
                }else{
                    sendMsg(m);
                    msg.setText("");
                    img_msg.setImageURI(null);
                }
            }
        });

        //displaying messages
        //query
        Query query=db.collection("users").document(uid).collection("chats").document(fid).collection("chat")
                .orderBy("time", Query.Direction.ASCENDING);

        //options
        FirestoreRecyclerOptions<msg_data_model>options=new FirestoreRecyclerOptions.Builder<msg_data_model>()
                .setQuery(query,msg_data_model.class).build();

        //adapter
        adapter= new FirestoreRecyclerAdapter<msg_data_model, RecyclerView.ViewHolder>(options) {


            class send_msg extends RecyclerView.ViewHolder{

                TextView msg,time;
                ImageView img;
                ConstraintLayout layout;

                public send_msg(@NonNull View itemView) {
                    super(itemView);
                    msg=itemView.findViewById(R.id.my_msg_txt);
                    time=itemView.findViewById(R.id.msg_time);
                    img=itemView.findViewById(R.id.send_img);
                    layout=itemView.findViewById(R.id.send_msg_horizontal);
                }
            }

            class rec_msg extends RecyclerView.ViewHolder{

                TextView msg,time;
                ImageView img;
                ConstraintLayout layout;

                public rec_msg(@NonNull View itemView) {
                    super(itemView);
                    msg=itemView.findViewById(R.id.his_msg_txt);
                    time=itemView.findViewById(R.id.msg_time_rec);
                    img=itemView.findViewById(R.id.rec_img);
                    layout=itemView.findViewById(R.id.rec_msg_horizontal);
                }
            }

            @Override
            public int getItemViewType(int position) {
                String id=getSnapshots().getSnapshot(position).getReference().getId();
                if(id.charAt(0)=='M'){
                    return 1;
                }else{
                    return 2;
                }
            }


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if(viewType==1){
                    view=LayoutInflater.from(parent.getContext()).inflate(R.layout.send_msg_layout,parent,false);
                    return new send_msg(view);
                }else{
                    view=LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_msg_layout,parent,false);
                    return  new rec_msg(view);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull msg_data_model model) {
                if(holder.getItemViewType()==1){
                    final send_msg data=(send_msg)holder;
                    data.msg.setText(model.getMsg());
                    if(!"def".equals(model.getImage())){
                        Picasso.get().load(model.getImage()).into(data.img);
                    }
                    data.time.setText(DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(model.getTime()))));
                    
                    data.layout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            data.layout.setBackgroundColor(Color.parseColor("#410277BD"));
                            return true;
                        }
                    });
                }else if(holder.getItemViewType()==2){
                    final rec_msg data=(rec_msg)holder;
                    data.msg.setText(model.getMsg());
                    if(!"def".equals(model.getImage())){
                        Picasso.get().load(model.getImage()).into(data.img);
                    }
                    data.time.setText(DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(model.getTime()))));
                    data.layout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            data.layout.setBackgroundColor(Color.parseColor("#6DFE5E01"));
                            return true;
                        }
                    });
                }
            }

        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).setStackFromEnd(true);

        //goto down
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                floatingActionButton.setVisibility(View.INVISIBLE);
            }
        });
        //scroll detection
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0){
                    floatingActionButton.setVisibility(View.INVISIBLE);
                }else{
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    //sending message
    public  void sendMsg(final String s){
        if(imageUri!=null){
            reference=firebaseStorage.getReference().child("chats").child(mAuth.getCurrentUser().getUid()+String.valueOf(System.currentTimeMillis())+".jpg");
            UploadTask uploadTask=reference.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String str=String.valueOf(uri);
                            HashMap<String,String>mp=new HashMap<>();
                            mp.put("sender",uid);
                            mp.put("receiver",fid);
                            mp.put("msg",s);
                            mp.put("image",str);
                            mp.put("time",String.valueOf(System.currentTimeMillis()));
                            db.collection("users").document(uid).collection("chats").document(fid).collection("chat")
                                    .document("M"+String.valueOf(System.currentTimeMillis())).set(mp);
                            db.collection("users").document(fid).collection("chats").document(uid).collection("chat")
                                    .document("H"+String.valueOf(System.currentTimeMillis())).set(mp).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
                                }
                            });
                            db.collection("users").document(uid).collection("chats").document(fid).set(mp);
                            db.collection("users").document(fid).collection("chats").document(uid).set(mp);
                        }
                    });
                }
            });
        }else{
            HashMap<String,String>mp=new HashMap<>();
            mp.put("sender",uid);
            mp.put("receiver",fid);
            mp.put("msg",s);
            mp.put("image","def");
            mp.put("time",String.valueOf(System.currentTimeMillis()));
            db.collection("users").document(uid).collection("chats").document(fid).collection("chat")
                    .document("M"+String.valueOf(System.currentTimeMillis())).set(mp);
            db.collection("users").document(fid).collection("chats").document(uid).collection("chat")
                    .document("H"+String.valueOf(System.currentTimeMillis())).set(mp).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
                }
            });
            db.collection("users").document(uid).collection("chats").document(fid).set(mp);
            db.collection("users").document(fid).collection("chats").document(uid).set(mp);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri resultUri = result.getUri();
                imageUri=resultUri;
                img_msg.setImageURI(imageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
            }
        }
    }
}