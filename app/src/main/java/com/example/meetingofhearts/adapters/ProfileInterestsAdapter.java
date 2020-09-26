package com.example.meetingofhearts.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetingofhearts.Entities.Interest;
import com.example.meetingofhearts.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProfileInterestsAdapter extends RecyclerView.Adapter<ProfileInterestsAdapter.profileInterestsVH> {

    private static final String TAG = "profileInterestsAdapter";
    List<Interest> interestList;
    Context context;
    private ProfileInterestsAdapter.OnItemClickListener mListener;
    private ProfileInterestsAdapter.OnItemLongClickListener mListener2;


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
    public void setOnItemClickListener(ProfileInterestsAdapter.OnItemClickListener listener) {
        mListener = listener;
    }


    /**
     * interface that handles clicking on items
     */
    public interface OnItemLongClickListener{
        void OnItemLongClick(int position);
    }

    /**
     * handles item clicking
     * @param listener object of listener
     */
    public void setOnItemLongClickListener(ProfileInterestsAdapter.OnItemLongClickListener listener) {
        mListener2 = listener;
    }

    public ProfileInterestsAdapter(Context context, List<Interest> interestList) {
        this.context = context;
        this.interestList = interestList;
    }

    @NonNull
    @Override
    public profileInterestsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_interest, parent, false);
        return new profileInterestsVH(view, mListener, mListener2);
    }

    @Override
    public void onBindViewHolder(@NonNull profileInterestsVH holder, int position) {

        Interest interest = interestList.get(position);
        holder.nameTextView.setText(interest.getName());
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setInterestList(List<Interest> interests) {
        interestList = interests;
        notifyDataSetChanged();
    }

    public List<Interest> getInterestList(){
        return interestList;
    }

    @Override
    public int getItemCount() {
        return interestList.size();
    }

    class profileInterestsVH extends RecyclerView.ViewHolder {

        private static final String TAG = "profileInterestsVH";

        TextView nameTextView;
        CardView card_interest;

        public profileInterestsVH(@NonNull final View itemView, final ProfileInterestsAdapter.OnItemClickListener listener
                , final ProfileInterestsAdapter.OnItemLongClickListener listener2) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.interest_name);
            card_interest = itemView.findViewById(R.id.card_interest);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        //gets position of clicked item
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener2 != null) {
                        //gets position of clicked item
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener2.OnItemLongClick(position);
                            return true;
                        }
                    }
                    return false;
                }
            });

        }
    }
}