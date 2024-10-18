package com.example.vibesyncapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vibesyncapp.Adapters.AdapterChat;
import com.example.vibesyncapp.Models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileiv;
    TextView nametv;
    EditText messageet;
    ImageButton sendbtn;
    FirebaseAuth auth;
    FirebaseUser user;
    String hisUid;
    String myUid;

    String hisImage;
    DatabaseReference userDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        auth = FirebaseAuth.getInstance();
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        user = auth.getCurrentUser();

        // Ensure user is not null before proceeding
        if (user != null) {
            myUid = user.getUid();
        } else {
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
                    hisImage = ds.child("image").getValue(String.class);

                    // Check for null or empty name and image values
                    nametv.setText(name != null && !name.isEmpty() ? name : "User");

                    if (hisImage != null && !hisImage.isEmpty()) {
                        Picasso.get().load(hisImage).placeholder(R.drawable.user_circle_svgrepo_com).into(profileiv);
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
        readMessages();
        seenMessage();
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelChat chat = snapshot.getValue(ModelChat.class);
                if (chat != null && (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                        chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid))) {

                    chatList.add(chat);

                    if (adapterChat == null) {
                        adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                        recyclerView.setAdapter(adapterChat);
                    } else {
                        adapterChat.notifyItemInserted(chatList.size() - 1);
                        recyclerView.smoothScrollToPosition(chatList.size() - 1);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelChat updatedChat = snapshot.getValue(ModelChat.class);
                for (int i = 0; i < chatList.size(); i++) {
                    if (chatList.get(i).getTimestamp().equals(updatedChat.getTimestamp())) {
                        chatList.set(i, updatedChat);
                        adapterChat.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle removed data
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle moved data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void sendMessage(String msg) {
        if (myUid == null || hisUid == null) {
            Toast.makeText(this, "User information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", msg);
        hashMap.put("timestamp", timeStamp); // Use "timestamp" as key
        hashMap.put("isSeen", false);

        databaseReference.push().setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageet.setText(""); // Clear the input field
            } else {
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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

    @Override
    protected void onPause() {
        super.onPause();
        userRefForSeen.removeEventListener(seenListener);
    }
}
