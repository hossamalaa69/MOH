package com.sufnatech.meetingofhearts;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application implements Application.ActivityLifecycleCallbacks{

        private int activityReferences = 0;
        private boolean isActivityChangingConfigurations = false;

        @Override
        public void onCreate() {
            super.onCreate();
            registerActivityLifecycleCallbacks(this);
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {}

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (++activityReferences == 1 && !isActivityChangingConfigurations && FirebaseAuth.getInstance().getCurrentUser()!=null) {
                Toast.makeText(getApplicationContext(), "Online", Toast.LENGTH_SHORT).show();
                String id = getSharedPreferences("LoginPref", 0).getString("id","");
                if(id != null && !id.isEmpty())
                    FirebaseDatabase.getInstance().getReference("User/" + id + "/status").setValue("Online");
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {}

        @Override
        public void onActivityPaused(@NonNull Activity activity) {}

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            isActivityChangingConfigurations = activity.isChangingConfigurations();
            if (--activityReferences == 0 && !isActivityChangingConfigurations && FirebaseAuth.getInstance().getCurrentUser()!=null) {
                Toast.makeText(getApplicationContext(), "Offline", Toast.LENGTH_SHORT).show();
                String id = getSharedPreferences("LoginPref", 0).getString("id","");
                if(id != null && !id.isEmpty())
                    FirebaseDatabase.getInstance().getReference("User/" + id + "/status").setValue("Offline");
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {}

}