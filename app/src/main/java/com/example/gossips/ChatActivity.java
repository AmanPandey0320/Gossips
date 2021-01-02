package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser mCurrrentuser=mAuth.getCurrentUser();
    RecyclerView recyclerView;
    FirestoreRecyclerAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //initializations
        db=FirebaseFirestore.getInstance();
        recyclerView=findViewById(R.id.chat_recyclerview);

        toolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gossips");
        getSupportActionBar().setSubtitle("Say Hello!");

        if(!mCurrrentuser.isEmailVerified()){
            Toast.makeText(this, "Please, verify your Email-ID.", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ChatActivity.this,Login.class);
            startActivity(intent);
            finish();
        }

        //query
        Query query=db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("chats")
                .orderBy("time",Query.Direction.DESCENDING);

        //options
        FirestoreRecyclerOptions<msg_data_model>options=new FirestoreRecyclerOptions.Builder<msg_data_model>()
                .setQuery(query,msg_data_model.class).build();

        //adapter
        adapter= new FirestoreRecyclerAdapter<msg_data_model, chat_view_holder>(options) {
            @NonNull
            @Override
            public chat_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_single_item,parent,false);
                return new chat_view_holder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final chat_view_holder holder, final int position, @NonNull final msg_data_model model) {
                db.collection("users").document(getSnapshots().getSnapshot(position).getReference().getId()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Picasso.get().load(documentSnapshot.getString("image")).into(holder.imageView);
                                holder.name.setText(documentSnapshot.getString("name"));
                                holder.msg.setText(model.getMsg());
                                holder.time.setText(DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(model.getTime()))));
                            }
                        });
                holder.chat_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(ChatActivity.this,messageActivity.class);
                        intent.putExtra("fid",getSnapshots().getSnapshot(position).getReference().getId());
                        intent.putExtra("name",holder.name.getText());
                        startActivity(intent);
                    }
                });
            }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_toolbar_item,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.chat_logout_btn:
                mAuth.signOut();
                startActivity(new Intent(ChatActivity.this, Login.class));
                finish();
                break;
            case R.id.accnt_btn:
                startActivity(new Intent(ChatActivity.this,Accnt_setup.class));
                break;
            case R.id.find_friend_btn:
                startActivity(new Intent(ChatActivity.this,findFriendActivity.class));
                break;
            case R.id.my_request_btn:
                startActivity(new Intent(ChatActivity.this,myRequests.class));
                break;
            case R.id.my_friend_btn:
                startActivity(new Intent(ChatActivity.this,my_friends.class));
                break;
        }
        return  true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mCurrrentuser.isEmailVerified()){
            Toast.makeText(this, "Please, verify your Email-ID.", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ChatActivity.this,Login.class);
            startActivity(intent);
            finish();
        }else{
            adapter.startListening();
        }
    }

    private class chat_view_holder extends RecyclerView.ViewHolder {

        CircleImageView imageView;
        TextView name,msg,time;
        ConstraintLayout chat_layout;

        public chat_view_holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.chat_img);
            name=itemView.findViewById(R.id.chat_user_name);
            msg=itemView.findViewById(R.id.char_msg_txt);
            time=itemView.findViewById(R.id.chat_time_txt);
            chat_layout=itemView.findViewById(R.id.chat_layout);
        }
    }
}