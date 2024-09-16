package com.example.vibesyncapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;


    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.raw_users,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String userimage=userList.get(position).getImage();
        String username=userList.get(position).getName();
        String useremail=userList.get(position).getEmail();

        holder.mnametv.setText(username);
        holder.memailtv.setText(useremail);

        try {
            Picasso.get().load(userimage).placeholder(R.drawable.user_circle_svgrepo_com).into(holder.mavatartv);
        }catch (Exception e){

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,""+useremail,Toast.LENGTH_SHORT ).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mavatartv;
        TextView mnametv,memailtv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mavatartv=itemView.findViewById(R.id.mavatartv);
            mnametv=itemView.findViewById(R.id.mnametv);
            memailtv=itemView.findViewById(R.id.memailtv);


        }
    }
}
