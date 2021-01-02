package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class myRequests extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference reference;
    FirestoreRecyclerAdapter adapter;
    String my_name,my_status,my_image,my_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        //initializations
        storage=FirebaseStorage.getInstance();
        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        toolbar=findViewById(R.id.request_toolbar);
        recyclerView=findViewById(R.id.request_recyclerview);

        //getting current user details
        db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        my_image=documentSnapshot.getString("image");
                        my_name=documentSnapshot.getString("name");
                        my_status=documentSnapshot.getString("status");
                        my_uid=mAuth.getCurrentUser().getUid();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(myRequests.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //setup toolbar
        setSupportActionBar(toolbar);

        //query
        Query query=db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).collection("requests");

        //options
        FirestoreRecyclerOptions<data_model> options=new FirestoreRecyclerOptions.Builder<data_model>().setQuery(query,data_model.class).build();

        //adapter
        adapter= new FirestoreRecyclerAdapter<data_model, data_holder>(options) {
            @NonNull
            @Override
            public data_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.request_single_item,parent,false);
                return  new data_holder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final data_holder holder, final int position, @NonNull final data_model model) {
                //Picasso.get().load(model.getImage()).into(holder.profile_image);
                reference=storage.getReferenceFromUrl(model.getImage());
                try {
                    final File file=File.createTempFile("image","jpg");
                    reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            holder.profile_image.setImageURI(Uri.fromFile(file));
                        }
                    });
                } catch (IOException e) {
                    Toast.makeText(myRequests.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                holder.name.setText(model.getName());
                holder.status.setText(model.getStatus());
                //accepting request
               holder.accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String friend_uid=model.getUid();
                        HashMap<String,String>me=new HashMap<>();
                        me.put("name",my_name);
                        me.put("image",my_image);
                        me.put("status",my_status);
                        me.put("uid",my_uid);
                        db.collection("users").document(model.getUid()).collection("friends").document(my_uid).set(me);
                        HashMap<String,String>he=new HashMap<>();
                        he.put("name",model.getName());
                        he.put("image",model.getImage());
                        he.put("status",model.getStatus());
                        he.put("uid",model.getUid());
                        db.collection("users").document(my_uid).collection("friends").document(model.getUid()).set(he)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            getSnapshots().getSnapshot(position).getReference().delete();
                                        }else{
                                            Toast.makeText(myRequests.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                //declining Request
                holder.decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getSnapshots().getSnapshot(position).getReference().delete();
                    }
                });
            }//end of current view holder
        };
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.request_toolbar_item,menu);
        return  true;
    }

    private class data_holder extends RecyclerView.ViewHolder {
        TextView name,status;
        CircleImageView accept,decline,profile_image;

        public data_holder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.friend_name);
            status=itemView.findViewById(R.id.friend_status);
            profile_image=itemView.findViewById(R.id.friend_image);
            accept=itemView.findViewById(R.id.accept);
            decline=itemView.findViewById(R.id.decline);
        }
    }
}