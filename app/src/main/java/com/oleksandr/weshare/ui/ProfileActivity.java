package com.oleksandr.weshare.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oleksandr.weshare.Authentication.ui.LoginActivity;
import com.oleksandr.weshare.Entities.Interest;
import com.oleksandr.weshare.Entities.User;
import com.oleksandr.weshare.R;
import com.oleksandr.weshare.adapters.ProfileInterestsAdapter;
import com.oleksandr.weshare.players.ImageViewerActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ybs.countrypicker.CountryPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView user_img;
    TextView user_name, user_phone, user_gender, user_age, user_nationality;
    RecyclerView user_interests_recycler, new_interests_recycler;
    ProfileInterestsAdapter adapter_user, adapter_new;
    ProgressBar profile_progress;
    ScrollView main_scroll;

    User user = null;
    boolean allowEdit = true;

    private final static int GET_FROM_GALLERY = 1000;
    private final static int READ_MEDIA_PERMISSION_CODE = 1001;
    private Uri selectedImage = null;
    private final List<Interest> allInterestsList = new ArrayList<>();

    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initRecyclers();

        initViews();

        getUserData();

        showAdInterstitial();
    }

    private void showAdInterstitial() {

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.test_interstitial));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                //Toast.makeText(ProfileActivity.this, "Ad Loaded", Toast.LENGTH_SHORT).show();
                mInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                //Toast.makeText(ProfileActivity.this, "Failed Loading \n" + adError.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
            }
        });
    }

    private void getUserData() {
        user = (User) getIntent().getSerializableExtra("user_serialized");
        String visit = getIntent().getStringExtra("visit");
        if(visit != null){
            allowEdit = false;
            lockEdit();
        }
        main_scroll.setVisibility(View.VISIBLE);

        fillViews();
    }

    private void lockEdit() {
        FloatingActionButton logout = findViewById(R.id.logout);
        logout.setVisibility(View.GONE);
        logout.setEnabled(false);
        ImageView img_upload_gallery = findViewById(R.id.img_upload_gallery);
        img_upload_gallery.setVisibility(View.GONE);
        img_upload_gallery.setEnabled(false);
        ImageView img_edit_profile = findViewById(R.id.img_edit_profile);
        img_edit_profile.setVisibility(View.GONE);
        img_edit_profile.setEnabled(false);
        new_interests_recycler.setVisibility(View.GONE);
        new_interests_recycler.setEnabled(false);
        TextView new_interests_txt = findViewById(R.id.new_interests_txt);
        new_interests_txt.setVisibility(View.GONE);
        RelativeLayout phone_relative = findViewById(R.id.phone_relative);
        phone_relative.setVisibility(View.GONE);
    }

    private void fillViews() {
        profile_progress.setVisibility(View.GONE);

        if(!user.getImageUrl().isEmpty())
            Glide.with(getApplicationContext())
                    .load(user.getImageUrl())
                    .placeholder(R.drawable.ic_user_placeholder_blue)
                    .into(user_img);
        else
            user_img.setImageResource(R.drawable.ic_user_placeholder_blue);

        user_name.setText(user.getUser_name());
        user_phone.setText(user.getPhone());
        user_gender.setText(user.getGender());
        if(user.getAge()>0)
            user_age.setText("" + user.getAge());
        user_nationality.setText(user.getNationality());

        fillUserInterests();
        getAllInterests();
    }

    private void fillUserInterests() {
        if(user.getInterests() != null){
            List<Interest> list = new ArrayList<>();
            list.clear();
            for (Map.Entry<String, String> entry : user.getInterests().entrySet()) {
                Interest interest = new Interest(entry.getKey(), entry.getValue(), new HashMap<String, String>());
                list.add(interest);
            }
            adapter_user.setInterestList(list);
        }
    }

    private void fillNewInterests(){
        List<Interest> suggestedList = new ArrayList<>();
        suggestedList.clear();
        for(Interest interest : allInterestsList){
            if(interest.getUsers() == null || interest.getUsers().get(user.getID()) == null)
                suggestedList.add(interest);
        }
        adapter_new.setInterestList(suggestedList);
    }

    private void getAllInterests() {
        DatabaseReference interestsDb = FirebaseDatabase.getInstance().getReference(Interest.class.getSimpleName());
        interestsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                interestsDb.removeEventListener(this);
                allInterestsList.clear();
                for(DataSnapshot snap : snapshot.getChildren())
                    allInterestsList.add(snap.getValue(Interest.class));
                fillNewInterests();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initViews() {
        main_scroll = findViewById(R.id.main_scroll);
        main_scroll.setVisibility(View.GONE);
        user_img = findViewById(R.id.user_img);
        user_name = findViewById(R.id.user_name);
        user_phone = findViewById(R.id.user_phone);
        user_gender = findViewById(R.id.user_gender);
        user_age = findViewById(R.id.user_age);
        user_nationality = findViewById(R.id.user_nationality);
    }

    private void initRecyclers() {

        profile_progress = findViewById(R.id.profile_progress);

        user_interests_recycler = findViewById(R.id.user_interests_recycler);
        adapter_user = new ProfileInterestsAdapter(this, new ArrayList<Interest>());
        user_interests_recycler.setLayoutManager(new GridLayoutManager(this, 3));
        user_interests_recycler.setAdapter(adapter_user);

        new_interests_recycler = findViewById(R.id.new_interests_recycler);
        adapter_new = new ProfileInterestsAdapter(this, new ArrayList<Interest>());
        new_interests_recycler.setLayoutManager(new GridLayoutManager(this, 3));
        new_interests_recycler.setAdapter(adapter_new);

        addListeners();
    }

    private void addListeners() {
        adapter_new.setOnItemClickListener(position -> {
            if(adapter_new.getInterestList().size() > 5){
                FloatingActionButton btn_done_edit = findViewById(R.id.btn_done_edit);
                btn_done_edit.setVisibility(View.VISIBLE);

                Interest interest = adapter_new.getInterestList().get(position);
                //add user to clicked interest for all interests >> add interest to user >> update both adapters

                allInterestsList.remove(interest);
                Map<String, String> interest_users = interest.getUsers();
                if(interest_users == null)
                    interest_users = new HashMap<>();
                interest_users.put(user.getID(), user.getUser_name());
                interest.setUsers(interest_users);
                allInterestsList.add(interest);

                ///////////////////////////
                Map<String,String> user_interests = user.getInterests();
                if(user_interests == null)
                    user_interests = new HashMap<>();
                user_interests.put(interest.getID(), interest.getName());
                user.setInterests(user_interests);
                /////////////////////////
                fillUserInterests();
                fillNewInterests();

            }else{
                Toast.makeText(ProfileActivity.this, "Maximum Interests is 5 \n You Can " +
                        "Remove Old Items By Long Click On Item" , Toast.LENGTH_LONG).show();
            }
        });

        if(allowEdit){
            adapter_user.setOnItemLongClickListener(position -> {
                Interest interest = adapter_user.getInterestList().get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                //Setting message manually and performing action on button click
                builder.setMessage("Sure To Delete " + interest.getName() + "?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            FloatingActionButton btn_done_edit = findViewById(R.id.btn_done_edit);
                            btn_done_edit.setVisibility(View.VISIBLE);

                            //remove from all interests, remove from user interests, update adapters
                            allInterestsList.remove(interest);
                            Map<String, String> interest_users = interest.getUsers();
                            interest_users.remove(user.getID(), user.getUser_name());
                            interest.setUsers(interest_users);
                            allInterestsList.add(interest);
                            /////////////////////
                            Map<String,String> user_interests = user.getInterests();
                            user_interests.remove(interest.getID(), interest.getName());
                            user.setInterests(user_interests);
                            /////////////
                            fillUserInterests();
                            fillNewInterests();

                            dialog.cancel();
                        })
                        .setNegativeButton("No", (dialog, id) -> {
                            dialog.cancel();
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("Delete Interest ..!");
                alert.show();
            });
        }
    }

    public void doneUpdate(View view) {
        if(user_name.getText().toString().isEmpty() || user_phone.getText().toString().isEmpty() || user_gender.getText().toString().isEmpty()
                || user_age.getText().toString().isEmpty() || user_nationality.getText().toString().isEmpty())
            Toast.makeText(this, "Please, Fill Your Data...!", Toast.LENGTH_SHORT).show();

        else
        {
            user.setUser_name(user_name.getText().toString());
            user.setPhone(user_phone.getText().toString());
            user.setNationality(user_nationality.getText().toString());
            user.setAge(Integer.parseInt(user_age.getText().toString()));
            user.setGender(user_gender.getText().toString());
            profile_progress.setVisibility(View.VISIBLE);

            if(selectedImage == null)
                updateUser();
            else
                updateUserWithImage();
        }
    }

    private void updateUser() {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference(User.class.getSimpleName());
        userDb.child(user.getID()).setValue(user).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(ProfileActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                updateAllInterests(0);
            }
        });
    }

    private void updateAllInterests(int i) {
        if(i>allInterestsList.size()-1){
            profile_progress.setVisibility(View.GONE);
            finish();
            return;
        }
        Interest interest = allInterestsList.get(i);
        DatabaseReference dbInterests = FirebaseDatabase.getInstance().getReference(Interest.class.getSimpleName());
        dbInterests.child(interest.getID()).setValue(interest).addOnCompleteListener(task -> {
                    updateAllInterests(i+1);
        });
    }

    private void updateUserWithImage() {
        if(user.getImageUrl().isEmpty())
            uploadImageToCloud();
        else{
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.getImageUrl());
            imageRef.delete().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    uploadImageToCloud();
                }else{
                    Toast.makeText(ProfileActivity.this, "Failed Removing Old Image ..!", Toast.LENGTH_SHORT).show();
                    profile_progress.setVisibility(View.GONE);
                }
            });
        }
    }

    private void uploadImageToCloud() {
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child(User.class.getSimpleName());
        final StorageReference photoRef = mStorageReference.child(selectedImage.getLastPathSegment());
        UploadTask uploadTask = photoRef.putFile(selectedImage);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return photoRef.getDownloadUrl();
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String imageUrl = downloadUri.toString();
                    user.setImageUrl(imageUrl);
                    updateUser();
                }else{
                    Toast.makeText(ProfileActivity.this, "Failed Uploading Image ..!", Toast.LENGTH_SHORT).show();
                    profile_progress.setVisibility(View.GONE);
                }
            }
        });
    }

    public void addImage(View view) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery();
            }
            else{
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "Please, Allow this permission to be able to upload your image..!"
                            , Toast.LENGTH_SHORT).show();
                }
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, READ_MEDIA_PERMISSION_CODE);
            }
    }

    private void getImageFromGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == READ_MEDIA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery();
            } else {
                Toast.makeText(this, "Permission Denied ...!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            user_img.setImageURI(selectedImage);
            FloatingActionButton btn_done_edit = findViewById(R.id.btn_done_edit);
            btn_done_edit.setVisibility(View.VISIBLE);

        }
    }


    public void editProfile(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.edit_profile_form, null);

        final EditText name_edit = v.findViewById(R.id.name_edit);
        name_edit.setText(user_name.getText().toString());

        final EditText phone_edit = v.findViewById(R.id.phone_edit);
        if(!user_phone.getText().toString().isEmpty())
            phone_edit.setText(user_phone.getText().toString());

        final Spinner gender_spinner = v.findViewById(R.id.gender_spinner);
        if(!user_gender.getText().toString().isEmpty()){
            ArrayAdapter spinner_adapter = (ArrayAdapter) gender_spinner.getAdapter();
            gender_spinner.setSelection(spinner_adapter.getPosition(user_gender.getText().toString()));
        }

        final EditText age_edit = v.findViewById(R.id.age_edit);
        if(!user_age.getText().toString().isEmpty() && !age_edit.getText().toString().equals("0"))
            age_edit.setText(user_age.getText().toString());

        final TextView nationality_edit = v.findViewById(R.id.nationality_edit);
        if(!user_nationality.getText().toString().isEmpty())
            nationality_edit.setText(user_nationality.getText().toString());
        final ImageView pick_country = v.findViewById(R.id.pick_country);
        pick_country.setOnClickListener(view1 -> {
            final CountryPicker picker = CountryPicker.newInstance("Select Country");  // dialog title
            picker.setListener((name, code, dialCode, flagDrawableResID) -> {
                nationality_edit.setText(name);
                picker.dismiss();
            });
            picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
        });
        builder.setView(v);
        final AlertDialog dialog = builder.create();
        dialog.show();

        FloatingActionButton btn_done_edit_form = v.findViewById(R.id.btn_done_edit_form);
        btn_done_edit_form.setOnClickListener(view12 -> {
            if(name_edit.getText().toString().isEmpty() || phone_edit.getText().toString().isEmpty()
                    || age_edit.getText().toString().isEmpty() || age_edit.getText().toString().equals("0")
                    || nationality_edit.getText().toString().isEmpty() || phone_edit.getText().toString().length() < 8) {
                Toast.makeText(ProfileActivity.this, "Please, Fill All Information", Toast.LENGTH_SHORT).show();
                return;
            }
            user_name.setText(name_edit.getText().toString());
            user_phone.setText(phone_edit.getText().toString());
            user_age.setText(age_edit.getText().toString());
            user_nationality.setText(nationality_edit.getText().toString());
            user_gender.setText(gender_spinner.getSelectedItem().toString());

            FloatingActionButton btn_done_edit = findViewById(R.id.btn_done_edit);
            btn_done_edit.setVisibility(View.VISIBLE);

            dialog.dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        if((user.getPhone().isEmpty() || user.getGender().isEmpty() || user.getNationality().isEmpty() || user.getAge()==0) && user.getUid().equals(FirebaseAuth.getInstance().getUid())){
            Toast.makeText(this, "Please, Complete Your Profile!", Toast.LENGTH_SHORT).show();
        }else{
            finish();
        }
    }

    public void SignOut(View view) {

        AuthUI.getInstance()
                .signOut(ProfileActivity.this)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(user.getID());
                        Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error ...! \n" + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void enlargeImage(View view) {

        if(selectedImage != null || !user.getImageUrl().isEmpty()) {
            Fade fade = new Fade();
            View decor = getWindow().getDecorView();
            fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            Intent i = new Intent(this, ImageViewerActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(ProfileActivity.this, user_img, ViewCompat.getTransitionName(user_img));

            if(selectedImage == null)
                i.putExtra("url", user.getImageUrl());
            else
                i.putExtra("uri", selectedImage.toString());

            startActivity(i, options.toBundle());
        }
    }


}