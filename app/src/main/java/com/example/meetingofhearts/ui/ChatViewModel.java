package com.example.meetingofhearts.ui;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.meetingofhearts.Entities.Conversation;
import com.example.meetingofhearts.Entities.Message;
import com.example.meetingofhearts.Entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    MutableLiveData<Message> messageMutableLiveData = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);

        DatabaseReference messageDb = FirebaseDatabase.getInstance().getReference(Message.class.getSimpleName());
        Query query = messageDb.orderByChild("conversation_id").equalTo(Constants.currentConversation);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                messageMutableLiveData.setValue(snapshot.getValue(Message.class));
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public MutableLiveData<Message> loadMessage(){
        return messageMutableLiveData;
    }


}

