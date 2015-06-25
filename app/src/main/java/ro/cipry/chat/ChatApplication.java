package ro.cipry.chat;

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
        Parse.initialize(this, "B4nKBopgxpQg5K6EzNwP6RXCYdSj5RH05Phx4xR2", "mAereCHU75GbQwnTYivGvgOt7A3UKF1niXmeRwO0");

        // Register your parse models here
        ParseObject.registerSubclass(Message.class);

        ParseUser.enableAutomaticUser();

        // Save the current Installation to Parse
        ParseInstallation.getCurrentInstallation().saveInBackground();

        // Test creation of object
        /*ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();*/
    }
}