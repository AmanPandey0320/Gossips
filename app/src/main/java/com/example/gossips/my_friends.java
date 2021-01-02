package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class my_friends extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseStorage storage;
    StorageReference reference;
    RecyclerView recyclerView;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        //initializations
        mAuth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        recyclerView=findViewById(R.id.my_friend_recyclerview);
        db=FirebaseFirestore.getInstance();
        toolbar=findViewById(R.id.my_friend_toolbar);


        //setup
        setSupportActionBar(toolbar);

        //query
        Query query=db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).collection("friends");

        //options
        FirestoreRecyclerOptions<data_model> options=new FirestoreRecyclerOptions.Builder<data_model>().setQuery(query,data_model.class).build();

//        adapter
        adapter= new FirestoreRecyclerAdapter<data_model, data_holder>(options) {
            @NonNull
            @Override
            public data_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_friend_single_item,parent,false);
                return new data_holder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final data_holder holder, int position, @NonNull final data_model model) {
                reference=storage.getReferenceFromUrl(model.getImage());
                try {
                    final File file= File.createTempFile("image","jpg");
                    reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            holder.image.setImageURI(Uri.fromFile(file));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                holder.status.setText(model.getStatus());
                holder.name.setText(model.getName());
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(my_friends.this,messageActivity.class);
                        intent.putExtra("fid",model.getUid());
                        intent.putExtra("name",model.getName());
                        startActivity(intent);
                    }
                });

            }//end of view holder
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private class data_holder extends RecyclerView.ViewHolder {
        ConstraintLayout layout;
        CircleImageView image;
        TextView name,status;
        public data_holder(@NonNull View itemView) {
            super(itemView);
            layout=itemView.findViewById(R.id.my_friend_layout);
            image=itemView.findViewById(R.id.my_friend_image);
            name=itemView.findViewById(R.id.my_friend_name);
            status=itemView.findViewById(R.id.my_friend_status);
        }
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
}