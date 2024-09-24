package com.example.vibesyncapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibesyncapp.Models.ModelChat;
import com.example.vibesyncapp.R;

import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {
    private static final int msg_type_left=0;
    private static final int msg_type_right=1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i ){
        if(i==msg_type_right)
        {
            View view= LayoutInflater.from(context).inflate(R.layout.raw_chat_right,parent,false);
            return new MyHolder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.raw_chat_left,parent,false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv;
        TextView messagetv,timetv,isseen;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv=itemView.findViewById(R.id.profileiv);
            messagetv=itemView.findViewById(R.id.messageTv);
            timetv=itemView.findViewById(R.id.timetv);
            isseen=itemView.findViewById(R.id.isseentv);
        }


    }
}
