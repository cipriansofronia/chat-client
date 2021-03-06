package ro.cipry.chat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;


public class PrivateChatActivity extends ActionBarActivity {

    private static final String TAG = ChatActivity.class.getName();
    private static final int LOGIN_ACTIVITY_CODE = 100;
    public static final String USER_ID_KEY = "userId";
    private EditText etMessage;
    private ImageButton btSend;
    private ListView lvChat;
    private RecyclerView recyclerView;
    private String initialObjId = "";
    private String newName = "";

    private ArrayList<Message> mMessages = null;
    private ChatListAdapter mAdapter;
    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 40;
    private Handler handler = new Handler();
    private ParseUser currentUser;
    private String privateChatObjId = "";
    private String privateChatName = "";
    private ArrayList<ParseUser> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        privateChatObjId = getIntent().getStringExtra("privateChatObjId");
        privateChatName = getIntent().getStringExtra("privateChatName");
        setTitle(privateChatName);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            // User login
            if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) { // start with existing user
                Log.i("PrivateChat", "User is defined: " + ParseUser.getCurrentUser());
                startWithCurrentUser();
            } else { // If not logged in, login as a new anonymous user
                Log.i("PrivateChat", "User is NOT defined! ");
                login();
            }

            // Run the runnable object defined every 100ms
            handler.postDelayed(runnable, 500);
        } else {
            Toast.makeText(getApplicationContext(), "Connect to internet!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable, 500);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    // Get the userId from the cached currentUser object
    private void startWithCurrentUser() {
        currentUser = ParseUser.getCurrentUser();
        setupMessagePosting();
    }

    private void login() {
        ParseLoginBuilder builder = new ParseLoginBuilder(this);
        startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == LOGIN_ACTIVITY_CODE) {
                startWithCurrentUser();
            }
        } else {
            finish();
        }
    }

    // Setup message field and posting
    private void setupMessagePosting() {
        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (ImageButton) findViewById(R.id.btSend);

        recyclerView = (RecyclerView) findViewById(R.id.lvChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));

        mMessages = new ArrayList<>();
        users = new ArrayList<>();
        mAdapter = new ChatListAdapter(PrivateChatActivity.this, currentUser.getObjectId(), mMessages, users);

        recyclerView.setAdapter(mAdapter);

        btSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (etMessage.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Type some text!", Toast.LENGTH_SHORT).show();
                } else {
                    final String body = etMessage.getText().toString();
                    // Use Message model to create new messages now
                    Message message = new Message();
                    message.setUserId(currentUser.getObjectId());
                    message.setBody(body);
                    message.setPrivateChat(currentUser.getObjectId() + privateChatObjId);
                    message.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            receiveMessage();
                        }

                    });
                    etMessage.setText("");

                    /*ParsePush push = new ParsePush();
                    push.setMessage(currentUser.getString("name") + ": " + body);
                    push.setChannel("chat");
                    push.sendInBackground(new SendCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d("PrivateChat", currentUser.getString("name") + ": " + body);
                            } else {
                                Log.e("PrivateChat", "Message not sent: " + e.getMessage());
                            }
                        }
                    });*/
                }
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    private void receiveMessage() {
        // TODO clear prev notifications
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class); // tells Parse what type of object you want to query
        query.orderByDescending("createdAt");
        ArrayList<String> list = new ArrayList<>();
        list.add(privateChatObjId + currentUser.getObjectId());
        list.add(currentUser.getObjectId() + privateChatObjId);
        query.whereContainedIn("privateChat", list);
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) { // returns list of messages
                if (e == null) {
                    if (mMessages != null) mMessages.clear();

                    TreeMap<Integer, Message> treeMap = new TreeMap<>(Collections.reverseOrder());
                    int i = 1;
                    for (Message msg : messages) {
                        treeMap.put(i++, msg);
                    }

                    if (mMessages != null) mMessages.addAll(treeMap.values());
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    if (recyclerView != null) recyclerView.invalidate();
                } else {
                    Log.e("PrivateChat", "Get messages error: " + e.getMessage());
                }
            }
        });

        ParseQuery<ParseUser> query2 = ParseUser.getQuery();
        query2.orderByAscending("createdAt");
        query2.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> u, ParseException e) {
                if (e == null) {
                    if (users != null) users.clear();
                    if (users != null) users.addAll(u);
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    if (recyclerView != null) recyclerView.invalidate();
                } else {
                    Log.d("UsersActivity", "Get users error: " + e.getMessage());
                }
            }
        });
    }

    // Defines a runnable which is run every 500ms
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            handler.postDelayed(this, 500);
        }
    };

    private void refreshMessages() {
        receiveMessage();
        if (mMessages != null && !mMessages.isEmpty()) {
            if (!mMessages.get(mMessages.size()-1).getObjectId().equals(initialObjId)) {
                initialObjId = mMessages.get(mMessages.size()-1).getObjectId();
                recyclerView.smoothScrollToPosition(mMessages.size());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_private_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_updateName) {
            changeUserName();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeUserName() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(PrivateChatActivity.this);
            alertDialog.setTitle("New name!");
            alertDialog.setMessage("Enter new name: ");

            final EditText input = new EditText(PrivateChatActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            // alertDialog.setIcon(R.drawable.key);

            alertDialog.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String newUserName = input.getText().toString();
                            if (newUserName.equals("")) {
                                Toast.makeText(getApplicationContext(),
                                        "Type an user name!", Toast.LENGTH_SHORT).show();
                            } else {
                                newName = newUserName;
                                new updateUserName().execute((Void[]) null);
                            }
                        }
                    });

            alertDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), "Connect to internet!", Toast.LENGTH_SHORT).show();
        }
    }

    private class updateUserName extends AsyncTask<Void,Void,Void > {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(PrivateChatActivity.this);
            dialog.setMessage("Updating...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            /* // Create a pointer to an object of class Point with objectId dlkj83d
            ParseObject point = ParseObject.createWithoutData("ParseUser", ParseUser.getCurrentUser().getObjectId());

            // Set a new value on quantity
            point.put("username", newName);
            ParseACL acl = new ParseACL();
            acl.setReadAccess(ParseUser.getCurrentUser(), true);
            acl.setWriteAccess(ParseUser.getCurrentUser(), true);
            point.setACL(acl); */

            ParseUser.getCurrentUser().put("name", newName);

            // Save
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Success!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.e("UpdateUser", e.getMessage());
                    }
                }
            });

            try {
                currentUser = ParseUser.getCurrentUser().fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            dialog.dismiss();
            refreshMessages();
        }
    }
}
