package ro.imihai.chat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class UsersActivity extends ActionBarActivity implements UsersAdapter.ClickListener{

    private RecyclerView recyclerView;
    private ArrayList<ParseUser> users = new ArrayList<>();
    private UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        recyclerView = (RecyclerView) findViewById(R.id.chatLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));

        adapter = new UsersAdapter(UsersActivity.this, ParseUser.getCurrentUser().getObjectId(), users);
        adapter.setClickListener(this);

        recyclerView.setAdapter(adapter);

        receiveUsers();
    }

    private void receiveUsers() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> u, ParseException e) {
                if (e == null) {
                    if (users != null) users.clear();
                    if (users != null) users.addAll(u);
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (recyclerView != null) recyclerView.invalidate();
                } else {
                    Log.d("UsersActivity", "Get users error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_users, menu);
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

    @Override
    public void itemClicked(View appIcon, int position) {

        ParseUser selectedUser = users.get(position);

        Intent privateChatIntent = new Intent(this, PrivateChatActivity.class);
        privateChatIntent.putExtra("privateChatObjId", selectedUser.getObjectId());
        privateChatIntent.putExtra("privateChatName", selectedUser.getString("name"));
        startActivity(privateChatIntent);

    }
}
