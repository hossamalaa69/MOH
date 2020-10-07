package com.oleksandr.weshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.oleksandr.weshare.Entities.Conversation;
import com.oleksandr.weshare.Entities.Interest;
import com.oleksandr.weshare.Entities.User;
import com.oleksandr.weshare.R;
import com.oleksandr.weshare.ui.ChatActivity;
import com.oleksandr.weshare.Constants;
import com.oleksandr.weshare.ui.ProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH> implements Filterable {

    private static final String TAG = "UserAdapter";
    List<User> userList;
    String current_user_ID = "";
    String current_uid = "";
    Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserAdapter.UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_user, parent, false);
        return new UserAdapter.UserVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserVH holder, int position) {

        User user = userList.get(position);

        holder.user_name.setText(user.getUser_name());
        if(user.getAge() > 0)
            holder.user_age.setText("" + user.getAge()+" Years");
        else
            holder.user_age.setText("");

        holder.user_nationality.setText(user.getNationality());
        holder.user_gender.setText(user.getGender());

        if(user.getImageUrl().isEmpty()){
            holder.user_image.setImageResource(R.drawable.ic_user_placeholder);
        }else{
            Glide.with(context)
                    .load(user.getImageUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(holder.user_image);
        }

        boolean isExpanded = userList.get(position).isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.drop_button.setImageResource(isExpanded? R.drawable.ic_collapse : R.drawable.ic_expand);

        List<Interest> interests = new ArrayList<>();
        if(user.getInterests() != null){
            for (Map.Entry<String, String> entry : user.getInterests().entrySet()) {
                Interest interest = new Interest(entry.getKey(), entry.getValue(), new HashMap<String, String>());
                interests.add(interest);
            }
        }
        holder.interestsAdapter = new ProfileInterestsAdapter(context, interests);
        holder.interests_recycler.setLayoutManager(new GridLayoutManager(context, 3));
        holder.interests_recycler.setAdapter(holder.interestsAdapter);
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setUserList(List<User> users, String id, String uid) {
        userList = users;
        current_user_ID = id;
        current_uid = uid;
        notifyDataSetChanged();
    }

    public List<User> getUserList(){
        return userList;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<User> filteredList = new ArrayList<>();
            List<User> fullList;
            if(Constants.filteredList.isEmpty())
                fullList = new ArrayList<>(Constants.fullUsers);
            else
                fullList = new ArrayList<>(Constants.filteredList);

            if (constraint.toString().isEmpty()) {
                filteredList.addAll(fullList);
            } else {
                String filterPattern = constraint.toString().toLowerCase();
                for (User user : fullList) {
                    if (user.getUser_name().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList.clear();
            userList.addAll((Collection<? extends User>) results.values);
            notifyDataSetChanged();
        }
    };

    class UserVH extends RecyclerView.ViewHolder {

        private static final String TAG = "UserVH";

        ConstraintLayout expandableLayout;
        TextView user_name, user_age, user_nationality, user_gender;
        CircleImageView user_image;
        ImageView drop_button, ic_start_chat;

        RelativeLayout main_view;

        RecyclerView interests_recycler;
        ProfileInterestsAdapter interestsAdapter;

        public UserVH(@NonNull final View itemView) {
            super(itemView);

            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            user_name = itemView.findViewById(R.id.user_name);
            user_age = itemView.findViewById(R.id.user_age);
            user_nationality = itemView.findViewById(R.id.user_nationality);
            user_gender = itemView.findViewById(R.id.user_gender);
            user_image = itemView.findViewById(R.id.user_image);
            drop_button = itemView.findViewById(R.id.drop_button);
            ic_start_chat = itemView.findViewById(R.id.ic_start_chat);
            main_view = itemView.findViewById(R.id.main_view);
            interests_recycler = itemView.findViewById(R.id.interests_recycler);


            main_view.setOnClickListener(view -> {
                User user = userList.get(getAdapterPosition());
                user.setExpanded(!user.isExpanded());
                notifyItemChanged(getAdapterPosition());
            });

            expandableLayout.setOnClickListener(view -> {
                User user = userList.get(getAdapterPosition());
                Intent intent = new Intent(context, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_serialized",user);
                bundle.putString("visit", "yes");
                intent.putExtras(bundle);
                context.startActivity(intent);
            });

            ic_start_chat.setOnClickListener(view -> {
                User user = userList.get(getAdapterPosition());
                Map<String, Boolean> users_pair = new HashMap<>();
                users_pair.put(current_user_ID, true);
                users_pair.put(user.getID(), true);

                DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference(Conversation.class.getSimpleName());
                conversationRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap : snapshot.getChildren()){
                            Conversation conversation = snap.getValue(Conversation.class);
                            Map<String, Boolean> map = conversation.getUsers();
                            if(map.keySet().equals(users_pair.keySet())){
                                openConversation(current_user_ID, user.getID(), conversation.getID(), conversation.getLast_message_date());
                                conversationRef.removeEventListener(this);
                                return;
                            }
                        }
                        conversationRef.removeEventListener(this);
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Conversation.class.getSimpleName());
                        String id = db.push().getKey();
                        Conversation conversation1 = new Conversation(id, "", 0, users_pair, "", "");
                        db.child(id).setValue(conversation1).addOnCompleteListener(task -> {
                            if(task.isSuccessful())
                                openConversation(current_user_ID, user.getID(), conversation1.getID(), 0);
                            db.removeEventListener(this);
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            });

        }

        private void openConversation(String current_user_id, String guest_user_id, String id, long lastDate) {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra("current_user_id", current_user_id);
            i.putExtra("guest_user_id", guest_user_id);
            i.putExtra("id", id);
            i.putExtra("last_date", lastDate);
            context.startActivity(i);
        }
    }
}
