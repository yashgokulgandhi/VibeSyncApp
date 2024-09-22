package com.example.vibesyncapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.vibesyncapp.Adapters.AdapterUsers;
import com.example.vibesyncapp.Models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference dref;
    ValueEventListener userEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userList = new ArrayList<>();
        adapterUsers = new AdapterUsers(getActivity(), userList);
        recyclerView.setAdapter(adapterUsers);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        dref = FirebaseDatabase.getInstance().getReference("Users");

        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        // Remove previous listener if exists
        if (userEventListener != null) {
            dref.removeEventListener(userEventListener);
        }

        userEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    if (modelUser != null && modelUser.getUid() != null && user != null && !modelUser.getUid().equals(user.getUid())) {
                        userList.add(modelUser);
                    }
                }
                adapterUsers.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        };

        dref.addValueEventListener(userEventListener);
    }

    private void searchUsers(String query) {
        if (TextUtils.isEmpty(query.trim())) {
            // If query is empty, show all users
            getAllUsers();
            return;
        }

        List<ModelUser> searchList = new ArrayList<>();
        for (ModelUser modelUser : userList) {
            if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                    modelUser.getEmail().toLowerCase().contains(query.toLowerCase())) {
                searchList.add(modelUser);
            }
        }

        adapterUsers = new AdapterUsers(getActivity(), searchList);
        recyclerView.setAdapter(adapterUsers);
        adapterUsers.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        setHasOptionsMenu(true);
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.searchbtn);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return false;
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logoutbtn) {
            auth.signOut();
            if (user == null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dref != null && userEventListener != null) {
            dref.removeEventListener(userEventListener);
        }
    }
}
