package com.paulina.chatclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MessageHolder> {
    private String mUserId;
    private LayoutInflater inflater;
    private Context context;

    List<Message> data = Collections.emptyList();

    public ChatListAdapter(Context context, String userId, List<Message> messages) {
        //super(context, 0, messages);
        this.context = context;
        this.mUserId = userId;
        inflater = LayoutInflater.from(context);
        this.data = messages;
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.chat_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.imageLeft = (ImageView)convertView.findViewById(R.id.ivProfileLeft);
            holder.imageRight = (ImageView)convertView.findViewById(R.id.ivProfileRight);
            holder.body = (TextView)convertView.findViewById(R.id.tvBody);
            convertView.setTag(holder);
        }
        final Message message = (Message)getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        final boolean isMe = message.getUserId().equals(mUserId);
        // Show-hide image based on the logged-in user.
        // Display the profile image to the right for our user, left for other users.
        if (isMe) {
            holder.imageRight.setVisibility(View.VISIBLE);
            holder.imageLeft.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else {
            holder.imageLeft.setVisibility(View.VISIBLE);
            holder.imageRight.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }
        final ImageView profileView = isMe ? holder.imageRight : holder.imageLeft;
        Picasso.with(getContext())
                .load(getProfileUrl(message.getUserId()))
                .into(profileView);
        holder.body.setText(message.getBody());
        return convertView;
    }*/

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
        View view = inflater.inflate(R.layout.chat_item, viewGroup, false);
        MessageHolder holder = new MessageHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MessageHolder messageHolder, int i) {
        final Message message = data.get(i);
        final boolean isMe = message.getUserId().equals(mUserId);
        // Show-hide image based on the logged-in user.
        // Display the profile image to the right for our user, left for other users.
        if (isMe) {
            messageHolder.imgRight.setVisibility(View.VISIBLE);
            messageHolder.imgLeft.setVisibility(View.GONE);
            messageHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else {
            messageHolder.imgLeft.setVisibility(View.VISIBLE);
            messageHolder.imgRight.setVisibility(View.GONE);
            messageHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }
        final ImageView profileView = isMe ? messageHolder.imgRight : messageHolder.imgLeft;
        Picasso.with(context)
                .load(getProfileUrl(message.getUserId()))
                .into(profileView);

        messageHolder.body.setText(message.getBody());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /*final class ViewHolder {

        public ImageView imageLeft;
        public ImageView imageRight;
        public TextView body;
    }*/

    public static class MessageHolder extends RecyclerView.ViewHolder {

        private ImageView imgLeft;
        private ImageView imgRight;
        private TextView body;

        public MessageHolder(View itemView) {
            super(itemView);
            imgLeft = (ImageView) itemView.findViewById(R.id.ivProfileLeft);
            imgRight = (ImageView) itemView.findViewById(R.id.ivProfileRight);
            body = (TextView) itemView.findViewById(R.id.tvBody);
        }
    }

}
