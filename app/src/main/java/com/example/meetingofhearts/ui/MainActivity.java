package com.example.meetingofhearts.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.meetingofhearts.Authentication.ui.LoginActivity;
import com.example.meetingofhearts.Entities.Interest;
import com.example.meetingofhearts.Entities.User;
import com.example.meetingofhearts.R;
import com.example.meetingofhearts.adapters.UserAdapter;
import com.example.meetingofhearts.players.VideoPlayerActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ybs.countrypicker.CountryPicker;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    ProgressBar main_progress;
    FloatingActionButton btn_filter_users, btn_chat;
    User currentUser = null;

    RecyclerView users_recycler;
    UserAdapter userAdapter;
    UsersViewModel usersViewModel;

    List<Boolean> filters = new ArrayList<>(Arrays.asList(false, false, false, false));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkIfNotLogged();
        if(!isFinishing()){
            checkUserProfile();
        }
    }

    private void initRecyclers() {
        users_recycler = findViewById(R.id.users_recycler);
        userAdapter = new UserAdapter(this, new ArrayList<User>());
        users_recycler.setLayoutManager(new LinearLayoutManager(this));
        users_recycler.setAdapter(userAdapter);

        initLiveData();
    }

    private void initLiveData() {
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        usersViewModel.loadAllUsers();
        usersViewModel.loadAllUsers().observe(this, users -> {
            Constants.fullUsers.clear();
            Constants.fullUsers.addAll(users);
            userAdapter.setUserList(users, currentUser.getID(), currentUser.getUid());
            main_progress.setVisibility(View.GONE);
            btn_filter_users.setVisibility(View.VISIBLE);
            btn_chat.setVisibility(View.VISIBLE);
        });
    }

    private void initViews() {
        main_progress = findViewById(R.id.main_progress);
        main_progress.setVisibility(View.VISIBLE);
        btn_filter_users = findViewById(R.id.btn_filter_users);
        btn_filter_users.setVisibility(View.GONE);
        btn_chat = findViewById(R.id.btn_chat);
        btn_chat.setVisibility(View.GONE);

        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.setVisibility(View.GONE);
    }

    private void checkIfNotLogged() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void checkUserProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference(User.class.getSimpleName());
        userRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren())
                    currentUser = snap.getValue(User.class);
                if(currentUser.getPhone().isEmpty() || currentUser.getGender().isEmpty() || currentUser.getNationality().isEmpty() || currentUser.getAge()==0){
                    Toast.makeText(MainActivity.this, "Please, Complete Your Profile!", Toast.LENGTH_SHORT).show();
                    openProfile();
                }else{
                    drawerLayout.setVisibility(View.VISIBLE);
                    View headerView = navigationView.getHeaderView(0);
                    TextView navUsername = (TextView) headerView.findViewById(R.id.welcome_user);
                    navUsername.setText("Welcome, " + currentUser.getUser_name());
                    initRecyclers();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void openProfile() {
        if(currentUser != null){
            Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_serialized",currentUser);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        }else{
                            Toast.makeText(MainActivity.this, "Error ...! \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                openProfile();
                break;
            case R.id.nav_logout:
                signOut();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.BLACK);

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    public void filterResults(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.filter_users_dialog, null);

        final CheckBox checkbox_gender = v.findViewById(R.id.checkbox_gender);
        checkbox_gender.setChecked(filters.get(0));
        final CheckBox checkbox_country = v.findViewById(R.id.checkbox_country);
        checkbox_country.setChecked(filters.get(1));
        final CheckBox checkbox_age = v.findViewById(R.id.checkbox_age);
        checkbox_age.setChecked(filters.get(2));
        final CheckBox checkbox_interests = v.findViewById(R.id.checkbox_interests);
        checkbox_interests.setChecked(filters.get(3));

        builder.setView(v);

        final AlertDialog dialog = builder.create();
        dialog.show();

        FloatingActionButton btn_done_filter = v.findViewById(R.id.btn_done_filter);
        btn_done_filter.setOnClickListener(view12 -> {

            List<User> filteredList = new ArrayList<>(Constants.fullUsers);

            if(checkbox_gender.isChecked()){
                for(Iterator<User> it = filteredList.iterator(); it.hasNext();) {
                    User user = it.next();
                    if(user.getGender().equals(currentUser.getGender()))
                        it.remove();
                }
            }
            if(checkbox_country.isChecked()){
                for(Iterator<User> it = filteredList.iterator(); it.hasNext();) {
                    User user = it.next();
                    if(!user.getNationality().equals(currentUser.getNationality()))
                        it.remove();
                }
            }
            if(checkbox_age.isChecked()){
                for(Iterator<User> it = filteredList.iterator(); it.hasNext();) {
                    User user = it.next();
                    if(Math.abs(user.getAge()-currentUser.getAge()) > 3)
                        it.remove();
                }
            }

            if(checkbox_interests.isChecked()){
                Map<String, String> user_interests = currentUser.getInterests();
                if(user_interests != null) {
                    for(Iterator<User> it = filteredList.iterator(); it.hasNext(); ) {
                        User user = it.next();
                        Map<String, String> current_interests = user.getInterests();
                        if(current_interests != null) {
                            Boolean anyMatching = false;
                            for (Map.Entry<String, String> entry : current_interests.entrySet()) {
                                if(user_interests.get(entry.getKey()) != null){
                                    anyMatching = true;
                                    break;
                                }
                            }
                            if(!anyMatching)
                                it.remove();
                        } else
                            it.remove();
                    }
                }
            }
            Constants.filteredList.clear();
            Constants.filteredList.addAll(filteredList);
            userAdapter.setUserList(filteredList, currentUser.getID(), currentUser.getUid());
            filters.set(0, checkbox_gender.isChecked());
            filters.set(1, checkbox_country.isChecked());
            filters.set(2, checkbox_age.isChecked());
            filters.set(3, checkbox_interests.isChecked());
            dialog.dismiss();
        });

    }

    public void openChat(View view) {
        Toast.makeText(this, "Open Chat", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, VideoPlayerActivity.class));
    }
}