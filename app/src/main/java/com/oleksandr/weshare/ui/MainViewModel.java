package com.oleksandr.weshare.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.oleksandr.weshare.Entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    MutableLiveData<List<User>> usersMutableLiveData = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);

        List<User> userList = new ArrayList<>();
        DatabaseReference usersDb = FirebaseDatabase.getInstance().getReference(User.class.getSimpleName());
        usersDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    User user = snap.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()) && user.getAge()>0)
                        userList.add(user);
                }
                usersMutableLiveData.setValue(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(getApplication(), "Cancelled ..!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public MutableLiveData<List<User>> loadAllUsers(){
        return usersMutableLiveData;
    }

}
