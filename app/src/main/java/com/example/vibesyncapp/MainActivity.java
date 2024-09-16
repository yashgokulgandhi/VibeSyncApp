package com.example.vibesyncapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {



    FirebaseAuth auth;
    FirebaseUser user;

    GoogleSignInClient googleSignInClient;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth=FirebaseAuth.getInstance();
        user= auth.getCurrentUser();

        actionBar=getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher_foreground);
        actionBar.setTitle("Home");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        HomeFragment fragment1=new HomeFragment();
        FragmentTransaction fr1=getSupportFragmentManager().beginTransaction();
        fr1.replace(R.id.content,fragment1,"");
        fr1.commit();

        BottomNavigationView navigationView=findViewById(R.id.bottom_nav);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               int i=0;
                if(item.getItemId()==R.id.nav_home)
                {
                    i=1;
                } else if (item.getItemId()==R.id.nav_profile) {
                    i=2;

                }else if (item.getItemId()==R.id.nav_users) {
                    i=3;

                }

                switch (i)
                {
                    case 1:
                        actionBar.setTitle("Home");
                        HomeFragment fragment1=new HomeFragment();
                        FragmentTransaction fr1=getSupportFragmentManager().beginTransaction();
                        fr1.replace(R.id.content,fragment1,"");
                        fr1.commit();
                        return true;
                    case 2:
                        actionBar.setTitle("Profile");
                        ProfileFragment fragment2=new ProfileFragment();
                        FragmentTransaction fr2=getSupportFragmentManager().beginTransaction();
                        fr2.replace(R.id.content,fragment2,"");
                        fr2.commit();
                        return true;
                    case 3:
                        actionBar.setTitle("Users");
                        UsersFragment fragment3=new UsersFragment();
                        FragmentTransaction fr3=getSupportFragmentManager().beginTransaction();
                        fr3.replace(R.id.content,fragment3,"");
                        fr3.commit();
                        return true;

                }
                return false;
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if(user==null)
        {
            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }


    }
  @Override
    public boolean onCreateOptionsMenu( Menu menu ) {

        menu.clear();
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        int i=0;
        if(item.getItemId()==R.id.logoutbtn)
          i=1;
        switch (i){
            case 1:
                auth.signOut();

                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Redirect to LoginActivity after sign out
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
                        startActivity(intent);
                        finish(); // Close MainActivity
                    }
                });
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}