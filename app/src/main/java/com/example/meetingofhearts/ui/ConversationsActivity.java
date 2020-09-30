package com.example.meetingofhearts.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.meetingofhearts.Entities.Conversation;
import com.example.meetingofhearts.R;
import com.example.meetingofhearts.adapters.ConversationAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConversationsActivity extends AppCompatActivity {

    ProgressBar progress_conversations;
    RecyclerView recycler_conversations;
    ConversationAdapter conversationAdapter;

    String current_id = "";

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        receiveID();

        initViews();

        initDatabase();

        showAdBanner();
    }

    private void showAdBanner() {
        mAdView = findViewById(R.id.adView_banner_conv);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Toast.makeText(ConversationsActivity.this, "Ad Loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Toast.makeText(ConversationsActivity.this, "Failed Loading \n" + adError.toString(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Toast.makeText(ConversationsActivity.this, "Ad Opened", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Toast.makeText(ConversationsActivity.this, "Ad Clicked", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Toast.makeText(ConversationsActivity.this, "Ad Left App", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Toast.makeText(ConversationsActivity.this, "Ad Closed", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void initDatabase() {
        List<Conversation> conversationList = new ArrayList<>();
        DatabaseReference conversationsDb = FirebaseDatabase.getInstance().getReference().child(Conversation.class.getSimpleName());
        conversationsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                conversationList.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Conversation conversation = snap.getValue(Conversation.class);
                    if(conversation.getUsers().get(current_id) != null && conversation.getLast_message_date() != 0)
                        conversationList.add(conversation);
                }
                Collections.sort(conversationList);
                conversationAdapter.setList(conversationList);
                progress_conversations.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void initViews() {
        progress_conversations = findViewById(R.id.progress_conversations);
        progress_conversations.setVisibility(View.VISIBLE);
        recycler_conversations = findViewById(R.id.recycler_conversations);

        conversationAdapter = new ConversationAdapter(new ArrayList<Conversation>(), this, current_id);
        recycler_conversations.setLayoutManager(new LinearLayoutManager(this));
        recycler_conversations.setAdapter(conversationAdapter);

        conversationAdapter.setOnItemClickListener(position -> {
            Conversation conversation = conversationAdapter.getList().get(position);
            String guest_id = "";
            for (Map.Entry<String, String> entry : conversation.getUsers().entrySet())
                if(!entry.getKey().equals(current_id))
                    guest_id = entry.getKey();

            Intent i = new Intent(ConversationsActivity.this, ChatActivity.class);
            i.putExtra("current_user_id", current_id);
            i.putExtra("guest_user_id", guest_id);
            i.putExtra("id", conversation.getID());
            i.putExtra("last_date", conversation.getLast_message_date());
            startActivity(i);
        });
    }

    private void receiveID() {
        current_id = getIntent().getStringExtra("current_id");
    }
}