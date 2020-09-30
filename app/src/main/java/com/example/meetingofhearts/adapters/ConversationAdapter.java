package com.example.meetingofhearts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meetingofhearts.Entities.Conversation;
import com.example.meetingofhearts.Entities.User;
import com.example.meetingofhearts.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Reference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    /**
     * holds list of objects of notifications
     */
    private List<Conversation> conversations;
    /**
     * holds instance from item click listener
     */
    private OnItemClickListener mListener;
    /**
     * holds context requires this adapter
     */
    private Context mContext;

    private String current_user_id;
    /**
     * Constructor of Notifications Adapter
     * @param mItems holds list of items of notification
     * @param mContext holds context of page
     */
    public ConversationAdapter(ArrayList<Conversation> mItems, Context mContext, String current_user_id) {
        this.conversations = mItems;
        this.mContext = mContext;
        this.current_user_id = current_user_id;
    }

    public void setList(List<Conversation> conversationList) {
        this.conversations = conversationList;
        notifyDataSetChanged();
    }

    public List<Conversation> getList(){
        return conversations;
    }

    /**
     * interface that handles clicking on items
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    /**
     * handles item clicking
     * @param listener object of listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * function that handles initializing view holders
     * @param parent holds root view
     * @param viewType view type of the item at position in recycler view
     * @return holder of item in recycler view
     */

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //sets layout for each item in recycler
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_item_conversaion, parent, false);

        return new Holder(view, mListener);
    }

    /**
     * Updates the contents with the item at the given position
     * @param holder represent the contents of the item at the given position
     * @param position position of current item
     */
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(position);
    }

    /**
     * holds total number of items in adapter
     * @return return size of array of item
     */
    @Override
    public int getItemCount() {
        return conversations.size();
    }

    /**
     * Handles an item view and metadata about its place within the RecyclerView.
     */
    public class Holder extends RecyclerView.ViewHolder {

        /**
         * holds view of image of notification
         */
        CircleImageView profile_img;
        /**
         * holds view of title text of notification
         */
        TextView user_name_cv;
        /**
         * holds view of body text of notification
         */
        TextView message_cv;
        /**
         * holds view of date of notification
         */
        TextView date_cv;

        /**
         * Constructor for Holder Class to initialize parameters
         * @param itemView view that contains current item
         */
        public Holder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            //gets text views that holds texts and image
            profile_img = itemView.findViewById(R.id.profile_img);
            user_name_cv = itemView.findViewById(R.id.user_name_cv);
            message_cv = itemView.findViewById(R.id.message_cv);
            date_cv = itemView.findViewById(R.id.date_cv);

            /**
             * sets listener to items
             */
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    //gets position of clicked item
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        /**
         * Fill each item in the view with its data
         * @param position index of item in recycler view
         */
        public void bind(int position) {

            //gets each item from list, then sets it in text, image views
            Conversation conversation = conversations.get(position);
            String second_id = "";
            Map<String, String> users = conversation.getUsers();
            for (Map.Entry<String, String> entry : users.entrySet()) {
                if(!entry.getKey().equals(current_user_id))
                    second_id = entry.getKey();
            }

            DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child(User.class.getSimpleName());
            Query query = userDb.child(second_id);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    user_name_cv.setText(user.getUser_name());
                    if(conversation.getLast_message().isEmpty())
                        message_cv.setText("(Attachment)");
                    else
                        message_cv.setText(conversation.getLast_message());
                    date_cv.setText(new SimpleDateFormat("E, dd MMM hh:mm a")
                            .format(new Date(conversation.getLast_message_date())));
                    Glide.with(mContext).load(user.getImageUrl()).placeholder(R.drawable.ic_user_placeholder).into(profile_img);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }

    }

}

