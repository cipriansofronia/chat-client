package ro.cipry.chat;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends ActionBarActivity {

    private static final String TAG = ChatActivity.class.getName();
    private static String sUserId;
    public static final String USER_ID_KEY = "userId";
    private EditText etMessage;
    private ImageButton btSend;
    private ListView lvChat;
    private RecyclerView recyclerView;
    private int initialSize = 0;

    private ArrayList<Message> mMessages = null;
    private ChatListAdapter mAdapter;
    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    // Create a handler which can run code periodically
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // User login
        if (ParseUser.getCurrentUser() != null) { // start with existing user
            startWithCurrentUser();
        } else { // If not logged in, login as a new anonymous user
            login();
        }
        // Run the runnable object defined every 100ms
        handler.postDelayed(runnable, 100);
    }

    // Get the userId from the cached currentUser object
    private void startWithCurrentUser() {
        sUserId = ParseUser.getCurrentUser().getObjectId();
        setupMessagePosting();
    }

    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    private void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.d(TAG, "Anonymous login failed.");
                } else {
                    startWithCurrentUser();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Setup message field and posting
    private void setupMessagePosting() {
        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (ImageButton) findViewById(R.id.btSend);

        recyclerView = (RecyclerView) findViewById(R.id.lvChat);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        mMessages = new ArrayList<Message>();
        mAdapter = new ChatListAdapter(ChatActivity.this, sUserId, mMessages);

        recyclerView.setAdapter(mAdapter);

        btSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (etMessage.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Type some text!", Toast.LENGTH_SHORT).show();
                } else {
                    String body = etMessage.getText().toString();
                    // Use Message model to create new messages now
                    Message message = new Message();
                    message.setUserId(sUserId);
                    message.setBody(body);
                    message.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            receiveMessage();
                        }

                    });
                    etMessage.setText("");
                }
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    private void receiveMessage() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class); // tells Parse what type of object you want to query
        // query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
        query.orderByAscending("createdAt");
        // Execute query for messages asynchronously
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) { // returns list of messages
                if (e == null) {
                    if (mMessages != null) mMessages.clear();
                    if (mMessages != null) mMessages.addAll(messages);
                    mAdapter.notifyDataSetChanged();
                    recyclerView.invalidate(); // TODO force refresh ??
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }

    // Defines a runnable which is run every 100ms
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            handler.postDelayed(this, 100);
        }
    };

    private void refreshMessages() {
        receiveMessage();
        if (mAdapter != null) {
            if (initialSize < mAdapter.getItemCount()) {
                initialSize = mAdapter.getItemCount();
                recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        }
    }

}
