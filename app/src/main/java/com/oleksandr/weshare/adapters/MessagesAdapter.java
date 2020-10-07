package com.oleksandr.weshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.oleksandr.weshare.Entities.Message;
import com.oleksandr.weshare.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesVH> {

    private static final String TAG = "MessagesAdapter";
    List<Message> messageList;
    String host_id;
    Context context;

    private MessagesAdapter.OnItemClickListener mListener;
    private MessagesAdapter.OnItemLongClickListener mListener2;

    /**
     * interface that handles clicking on items
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int position);
    }

    /**
     * handles item clicking
     * @param listener object of listener
     */
    public void setOnItemClickListener(MessagesAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
    public void setOnItemLongClickListener(MessagesAdapter.OnItemLongClickListener listener) {
        mListener2 = listener;
    }

    public MessagesAdapter(Context context, List<Message> messageList, String host_id) {
        this.context = context;
        this.messageList = messageList;
        this.host_id = host_id;
    }

    @NonNull
    @Override
    public MessagesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_message, parent, false);
        return new MessagesVH(view, mListener, mListener2);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesVH holder, int position) {
        Message message = messageList.get(position);

        /*
        ImageView photoImageView, photoImageView2;
        TextView messageTextView, messageTextView2, dateTextView, dateTextView2;
         */
        if(message.getSender_ID().equals(host_id)){
            holder.guest_layout.setVisibility(View.GONE);
            holder.host_layout.setVisibility(View.VISIBLE);
            if(message.getMessage_text().length()>0){
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.media_layout.setVisibility(View.GONE);
                holder.messageTextView.setText(message.getMessage_text());
            }
            else if(!message.getImageUrl().isEmpty()){
                holder.messageTextView.setVisibility(View.GONE);
                holder.media_layout.setVisibility(View.VISIBLE);
                holder.ic_play_video.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.ic_image_white).into(holder.photoImageView);
            }else{
                holder.messageTextView.setVisibility(View.GONE);
                holder.media_layout.setVisibility(View.VISIBLE);
                holder.ic_play_video.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getVideoUrl()).placeholder(R.drawable.ic_play_white).into(holder.photoImageView);
            }
            holder.dateTextView.setText(new SimpleDateFormat("E, dd MMM hh:mm a")
                    .format(new Date(message.getMsg_date())));
        }
        else{
            holder.host_layout.setVisibility(View.GONE);
            holder.guest_layout.setVisibility(View.VISIBLE);
            if(message.getMessage_text().length()>0){
                holder.messageTextView2.setVisibility(View.VISIBLE);
                holder.media_layout2.setVisibility(View.GONE);
                holder.messageTextView2.setText(message.getMessage_text());
            }
            else if(!message.getImageUrl().isEmpty()){
                holder.messageTextView2.setVisibility(View.GONE);
                holder.media_layout2.setVisibility(View.VISIBLE);
                holder.ic_play_video2.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.ic_image_white).into(holder.photoImageView2);
            }else{
                holder.messageTextView2.setVisibility(View.GONE);
                holder.media_layout2.setVisibility(View.VISIBLE);
                holder.ic_play_video2.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getVideoUrl()).placeholder(R.drawable.ic_play_white).into(holder.photoImageView2);
            }
            holder.dateTextView2.setText(new SimpleDateFormat("E, dd MMM hh:mm a")
                    .format(new Date(message.getMsg_date())));
        }

    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setMessageList(List<Message> messages) {
        messageList = messages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message){
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
        //notifyDataSetChanged();
    }


    public void updateMessage(Message message, int position) {
        messageList.set(position, message);
        notifyItemChanged(position);
    }


    public List<Message> getMessages(){
        return messageList;
    }

    public void clearData() {
        messageList.clear(); // clear list
        notifyDataSetChanged(); // let your adapter know about the changes and reload view.
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessagesVH extends RecyclerView.ViewHolder {

        private static final String TAG = "MessagesVH";

        LinearLayout host_layout, guest_layout;
        RelativeLayout media_layout, media_layout2;
        ImageView photoImageView, photoImageView2, ic_play_video, ic_play_video2;
        TextView messageTextView, messageTextView2, dateTextView, dateTextView2;


        public MessagesVH(@NonNull final View itemView, final MessagesAdapter.OnItemClickListener mListener, final MessagesAdapter.OnItemLongClickListener mListener2) {
            super(itemView);

            host_layout = itemView.findViewById(R.id.host_layout);
            guest_layout = itemView.findViewById(R.id.guest_layout);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            photoImageView2 = itemView.findViewById(R.id.photoImageView2);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageTextView2 = itemView.findViewById(R.id.messageTextView2);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            dateTextView2 = itemView.findViewById(R.id.dateTextView2);

            media_layout = itemView.findViewById(R.id.media_layout);
            media_layout2 = itemView.findViewById(R.id.media_layout2);
            ic_play_video = itemView.findViewById(R.id.ic_play_video);
            ic_play_video2 = itemView.findViewById(R.id.ic_play_video2);

            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position);
                    }
                }
            });

            itemView.setOnLongClickListener(view -> {
                if (mListener2 != null) {
                    //gets position of clicked item
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener2.onItemLongClick(position);
                        return true;
                    }
                }
                return false;
            });
        }
    }
}

