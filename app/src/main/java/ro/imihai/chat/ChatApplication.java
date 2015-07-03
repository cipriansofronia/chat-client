package ro.imihai.chat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class ChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

        // Register your parse models here
        ParseObject.registerSubclass(Message.class);

        ParseUser.enableAutomaticUser();

        // Save the current Installation to Parse
        ParseInstallation.getCurrentInstallation().saveInBackground();

    }
}