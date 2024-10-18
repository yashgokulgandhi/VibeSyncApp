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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fuser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.raw_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.raw_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        // Set message text
        holder.messageTv.setText(message);

        // Check if timeStamp is not null or empty before parsing
        if (timeStamp != null && !timeStamp.isEmpty()) {
            try {
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(Long.parseLong(timeStamp));
                String dateTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                holder.timeTv.setText(dateTime);
            } catch (NumberFormatException e) {
                // In case of parsing failure, set a default or error message for the timestamp
                holder.timeTv.setText("Invalid date");
            }
        } else {
            // Handle null or empty timestamp with a default value
            holder.timeTv.setText("Unknown time");
        }

        // Set profile image
        try {
            Picasso.get().load(imageUrl).placeholder(R.drawable.user_circle_svgrepo_com).into(holder.profileIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.user_circle_svgrepo_com).into(holder.profileIv);
        }

        // Handle 'seen' status
        if (position == chatList.size() - 1) {
            if (chatList.get(position).isSeen()) {
                holder.isSeenTv.setText("Seen");
            } else {
                holder.isSeenTv.setText("Delivered");
            }
        } else {
            holder.isSeenTv.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        // Return the size of the chatList
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    // ViewHolder class
    static class MyHolder extends RecyclerView.ViewHolder {
        ImageView profileIv;
        TextView messageTv, timeTv, isSeenTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize the views
            profileIv = itemView.findViewById(R.id.profileiv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timetv);
            isSeenTv = itemView.findViewById(R.id.isseentv);
        }
    }
}
