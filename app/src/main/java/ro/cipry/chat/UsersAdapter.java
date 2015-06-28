package ro.cipry.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MessageHolder> {
    private String mUserId;
    private LayoutInflater inflater;
    private Context context;
    private ClickListener clickListener;

    List<ParseUser> users = Collections.emptyList();

    public UsersAdapter(Context context, String userId, List<ParseUser> users) {
        this.context = context;
        this.mUserId = userId;
        inflater = LayoutInflater.from(context);
        this.users = users;
    }

    // Create a gravatar image based on the hash value obtained from userId
    private static String getProfileUrl(final String userId) {
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "http://www.gravatar.com/avatar/" + hex + "?d=identicon";
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.user_item, viewGroup, false);
        MessageHolder holder = new MessageHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, int i) {
        final ParseUser user = users.get(i);
        // final boolean isMe = user.getUserId().equals(mUserId);

        final ImageView profileView = holder.imgLeft;
        Picasso.with(context)
                .load(getProfileUrl(user.getObjectId()))
                .transform(new CircleTransform())
                .into(profileView);

        if (ParseUser.getCurrentUser().getObjectId().equals(user.getObjectId())) {
            holder.nameLeft.setText(user.getString("name") + " (me)");
        } else {
            holder.nameLeft.setText(user.getString("name"));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {

        private ImageView imgLeft;
        private TextView nameLeft;

        public MessageHolder(final View itemView) {
            super(itemView);
            imgLeft = (ImageView) itemView.findViewById(R.id.ivImgLeft);
            nameLeft = (TextView) itemView.findViewById(R.id.name);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(clickListener != null){
                        clickListener.itemClicked(itemView, getPosition());
                    }
                }
            });
        }
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        public void itemClicked(View appIcon, int position);
    }

}
