package ro.cipry.chat;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    public String getUserId() {
        return getString("userId");
    }

    public String getBody() {
        return getString("body");
    }

    public String getUserName() {
        return getString("username");
    }

    public void setUserId(String userId) {
        put("userId", userId);
    }

    public void setBody(String body) {
        put("body", body);
    }

    public void setUserName(String userName) {
        put("username", userName);
    }
}