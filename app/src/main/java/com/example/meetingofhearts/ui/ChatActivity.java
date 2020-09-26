package com.example.meetingofhearts.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.meetingofhearts.Entities.Conversation;
import com.example.meetingofhearts.Entities.Message;
import com.example.meetingofhearts.Entities.User;
import com.example.meetingofhearts.R;
import com.example.meetingofhearts.adapters.MessagesAdapter;
import com.example.meetingofhearts.players.ImageViewerActivity;
import com.example.meetingofhearts.players.VideoPlayerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity{

    private final static int GET_FROM_GALLERY = 1500;
    private static final int PICK_VIDEO_REQUEST = 1502;
    private final static int READ_MEDIA_PERMISSION_CODE = 1501;
    private Uri selectedImage = null;
    private Uri selectedVideo = null;

    static String conversation_id;
    static String user_id;
    static String guest_user_id;
    static long last_date;

    ProgressBar chat_progress;
    TextView empty_chat;
    RecyclerView recycler_messages;
    MessagesAdapter messageAdapter;
    EditText msg_input;
    ImageView send_msg_icon;

    //whole database (db name)
    private FirebaseDatabase mFirebaseDatabase;
    //part of database (entity/table)
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mMessagesChildListener;
    private Query mMessagesQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();

        receiveParams();

        initToolbar();

        initRecycler();

        initDatabase();
    }

    private void initDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        //init reference for entity "Messages"
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(Message.class.getSimpleName());
        //set listener for any change in /database/messages path
        mMessagesQuery = mMessagesDatabaseReference.orderByChild("conversation_id").equalTo(conversation_id);
        if(mMessagesChildListener == null) {
            mMessagesChildListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Message message = snapshot.getValue(Message.class);
                    messageAdapter.addMessage(message);
                    recycler_messages.scrollToPosition(messageAdapter.getItemCount()-1);
                    chat_progress.setVisibility(View.GONE);
                    empty_chat.setVisibility(View.GONE);
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            };
            mMessagesQuery.addChildEventListener(mMessagesChildListener);
        }
    }




    private void initViews() {
        chat_progress = findViewById(R.id.chat_progress);
        chat_progress.setVisibility(View.VISIBLE);
        empty_chat = findViewById(R.id.empty_chat);
        empty_chat.setVisibility(View.VISIBLE);
        recycler_messages = findViewById(R.id.recycler_messages);
        send_msg_icon = findViewById(R.id.send_msg_icon);
        msg_input = findViewById(R.id.msg_input);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_chat);
    }

    private void receiveParams() {
        conversation_id = getIntent().getStringExtra("id");
        //Constants.currentConversation = conversation_id;
        user_id = getIntent().getStringExtra("current_user_id");
        guest_user_id = getIntent().getStringExtra("guest_user_id");
        last_date = getIntent().getLongExtra("last_date", 0);
    }

    private void initRecycler() {
        messageAdapter = new MessagesAdapter(this, new ArrayList<Message>(), user_id);
        recycler_messages.setLayoutManager(new LinearLayoutManager(this));
        recycler_messages.setAdapter(messageAdapter);
        recycler_messages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if(messageAdapter.getItemCount() > 0){
                if (bottom < oldBottom) {
                    recycler_messages.postDelayed(() -> recycler_messages.smoothScrollToPosition(
                            recycler_messages.getAdapter().getItemCount() - 1), 100);
                }
            }
        });

        messageAdapter.setOnItemClickListener(position -> {
            Message message = messageAdapter.getMessages().get(position);
            if(!message.getMessage_text().isEmpty())
                return;
            else if(!message.getImageUrl().isEmpty()){
                Intent i = new Intent(this, ImageViewerActivity.class);
                i.putExtra("url", message.getImageUrl());
                startActivity(i);
            }
            else{
                Intent i = new Intent(this, VideoPlayerActivity.class);
                i.putExtra("video_url", message.getVideoUrl());
                startActivity(i);
            }
        });

        if(last_date == 0){
            chat_progress.setVisibility(View.GONE);
            empty_chat.setVisibility(View.VISIBLE);
        }
    }

    private void initToolbar() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(User.class.getSimpleName());
        databaseReference.child(guest_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView textView = getSupportActionBar().getCustomView().findViewById(R.id.guest_name);
                CircleImageView circleImageView = getSupportActionBar().getCustomView().findViewById(R.id.guest_img);
                User user = snapshot.getValue(User.class);
                textView.setText(user.getUser_name());
                Glide.with(ChatActivity.this).load(user.getImageUrl()).placeholder(R.drawable.ic_user_placeholder).into(circleImageView);
                databaseReference.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void sendMessage(View view) {
        if(!msg_input.getText().toString().isEmpty()){
            Message message = new Message("", msg_input.getText().toString(), new Date().getTime(), conversation_id, user_id, "", "");
            insertMessageDb(message);
            msg_input.setText("");
        }
    }

    private void insertMessageDb(Message message){
        String id = mMessagesDatabaseReference.push().getKey();
        message.setID(id);
        updateConversation(message);
        mMessagesDatabaseReference.child(id).setValue(message).addOnCompleteListener(task -> {
            if(task.isSuccessful())
                Toast.makeText(ChatActivity.this, "Send", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateConversation(Message message) {
        DatabaseReference convDb = FirebaseDatabase.getInstance().getReference().child(Conversation.class.getSimpleName());
        Query convQuery = convDb.child(conversation_id);
        convQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Conversation conversation = snapshot.getValue(Conversation.class);
                conversation.setLast_message(message.getMessage_text());
                conversation.setLast_message_date(message.getMsg_date());
                convQuery.removeEventListener(this);
                convDb.child(conversation_id).setValue(conversation);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendImage(View view) {
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

    private void getImageFromGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            UploadToCloudAndDb();
        }else if(requestCode==PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK){
            selectedVideo = data.getData();
            UploadVideoToCloud();
        }
    }

    private void UploadToCloudAndDb() {
        chat_progress.setVisibility(View.VISIBLE);
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child(Message.class.getSimpleName());
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
                    Toast.makeText(ChatActivity.this, "Uploaded Successfully ..!", Toast.LENGTH_SHORT).show();
                    Uri downloadUri = task.getResult();
                    String imageUrl = downloadUri.toString();
                    Message message = new Message("", "", new Date().getTime(), conversation_id, user_id, imageUrl, "");
                    insertMessageDb(message);
                }else{
                    Toast.makeText(ChatActivity.this, "Failed Uploading Image ..!", Toast.LENGTH_SHORT).show();
                    chat_progress.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    public void sendVideo(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private String getFileExtension(Uri videoUri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(videoUri));
    }

    private void UploadVideoToCloud() {
            chat_progress.setVisibility(View.VISIBLE);
            String extension = getFileExtension(selectedVideo);
            StorageReference reference = FirebaseStorage.getInstance().getReference("videos").child(System.currentTimeMillis() +
                    "." + extension);

            UploadTask uploadTask = reference.putFile(selectedVideo);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return reference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(ChatActivity.this, "Uploaded Successfully ..!", Toast.LENGTH_SHORT).show();
                    Uri downloadUri = task.getResult();
                    String videoUrl = downloadUri.toString();
                    Message message = new Message("", "", new Date().getTime(), conversation_id, user_id, "", videoUrl);
                    insertMessageDb(message);
                }else{
                    Toast.makeText(ChatActivity.this, "Failed Uploading Image ..!", Toast.LENGTH_SHORT).show();
                    chat_progress.setVisibility(View.GONE);
                }
            });
    }


}