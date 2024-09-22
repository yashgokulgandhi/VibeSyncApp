package com.example.vibesyncapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileiv;
    TextView nametv, userstatustv;
    EditText messageet;
    ImageButton sendbtn;
    FirebaseAuth auth;
    FirebaseUser user;
    String hisUid;
    String myUid;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.chatrecyclerview);
        profileiv = findViewById(R.id.profileiv);
        nametv = findViewById(R.id.nametv);
        messageet = findViewById(R.id.messageEd);
        sendbtn = findViewById(R.id.msgBtn);

        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");
        user = auth.getCurrentUser();

        // Ensure user is not null before proceeding
        if (user != null) {
            myUid = user.getUid();
        } else {
            // If user is not authenticated, redirect to login screen
            startActivity(new Intent(ChatActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Get the UID of the person we are chatting with from the Intent
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        loadUserDetails();

        // Set onClick listener for the send button
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageet.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), "Cannot Send Empty Message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(msg);
                }
            }
        });
    }

    private void loadUserDetails() {
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = ds.child("name").getValue(String.class);
                    String image = ds.child("image").getValue(String.class);

                    // Check for null or empty name and image values
                    if (name != null && !name.isEmpty()) {
                        nametv.setText(name);
                    } else {
                        nametv.setText("User");
                    }

                    if (image != null && !image.isEmpty()) {
                        Picasso.get().load(image).placeholder(R.drawable.user_circle_svgrepo_com).into(profileiv);
                    } else {
                        Picasso.get().load(R.drawable.user_circle_svgrepo_com).into(profileiv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
            }
        });
    }

    private void sendMessage(String msg) {
        // Send the message to the Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", msg);
        databaseReference.push().setValue(hashMap);

        // Clear the message EditText after sending
        messageet.setText("");
    }

    public void checkuserstatus() {
        if (user != null) {
            myUid = user.getUid();
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.searchbtn).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logoutbtn) {
            auth.signOut();
            user = null;
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
