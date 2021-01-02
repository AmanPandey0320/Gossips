package com.example.gossips;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user=FirebaseAuth.getInstance().getCurrentUser();
        Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(user==null){
                    Toast.makeText(MainActivity.this, "Please login or register.", Toast.LENGTH_SHORT).show();
                    Handler handler1=new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent=new Intent(MainActivity.this,Login.class);
                            startActivity(intent);
                            finish();
                        }
                    },1000);
                }else{
                    Intent intent=new Intent(MainActivity.this,ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },2000);
    }
}
