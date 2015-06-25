package ro.cipry.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseException;
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
        this.context = context;
        this.mUserId = userId;
        inflater = LayoutInflater.from(context);
        this.data = messages;
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
            messageHolder.nameLeft.setVisibility(View.GONE);
            messageHolder.nameRight.setVisibility(View.VISIBLE);
            messageHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams)messageHolder.body.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            messageHolder.body.setLayoutParams(params);
        } else {
            messageHolder.imgLeft.setVisibility(View.VISIBLE);
            messageHolder.imgRight.setVisibility(View.GONE);
            messageHolder.nameLeft.setVisibility(View.VISIBLE);
            messageHolder.nameRight.setVisibility(View.GONE);
            messageHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams)messageHolder.body.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            messageHolder.body.setLayoutParams(params);

        }
        final ImageView profileView = isMe ? messageHolder.imgRight : messageHolder.imgLeft;
        Picasso.with(context)
                .load(getProfileUrl(message.getUserId()))
                .transform(new CircleTransform())
                .into(profileView);

        messageHolder.body.setText(message.getBody());
        messageHolder.nameLeft.setText(message.getUserName());
        messageHolder.nameRight.setText(message.getUserName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {

        private ImageView imgLeft;
        private ImageView imgRight;
        private TextView nameLeft;
        private TextView nameRight;
        private TextView body;

        public MessageHolder(View itemView) {
            super(itemView);
            imgLeft = (ImageView) itemView.findViewById(R.id.ivProfileLeft);
            imgRight = (ImageView) itemView.findViewById(R.id.ivProfileRight);
            nameLeft = (TextView) itemView.findViewById(R.id.numeLeft);
            nameRight = (TextView) itemView.findViewById(R.id.numeRight);
            body = (TextView) itemView.findViewById(R.id.tvBody);
        }
    }

}
