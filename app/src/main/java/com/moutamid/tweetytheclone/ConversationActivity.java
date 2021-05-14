package com.moutamid.tweetytheclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = "ConversationActivity";

    private String otherUserImageUrl, otherUserName, otherUserUid;
    private boolean isFirst = false;

    private ArrayList<ChatMessage> currentMessagesArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private CircleImageView profileImageView;
    private TextView usernameTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        profileImageView = findViewById(R.id.profile_image_view_conversation_activity);
        usernameTextView = findViewById(R.id.user_name_conversation);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        setBackBtnClickListener();

        setAddMessageBtnCLickListener();

        if (getIntent().hasExtra("first")) {

            if (getIntent().getBooleanExtra("first", false)) {

                isFirst = true;
            }

        }

        otherUserName = getIntent().getStringExtra("name");
        otherUserImageUrl = getIntent().getStringExtra("url");
        otherUserUid = getIntent().getStringExtra("uid");

        usernameTextView.setText(otherUserName);

        Glide.with(ConversationActivity.this)
                .load(otherUserImageUrl)
                .apply(new RequestOptions()
                        .placeholder(R.color.grey)
                        .error(R.color.grey)
                )
                .into(profileImageView);

        initRecyclerView();

        databaseReference.child("chats").child(mAuth.getCurrentUser().getUid())
                .child(otherUserUid)
                .child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
//                    Toast.makeText(ConversationActivity.this, "No chat exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentMessagesArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                    currentMessagesArrayList.add(chatMessage);

                }

                initRecyclerView();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
                Toast.makeText(ConversationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAddMessageBtnCLickListener() {
        EditText editText = findViewById(R.id.reply_edit_text_activity_conversation);

        findViewById(R.id.send_reply_btn_activity_conversation).setOnClickListener(view -> {

            String replyText = editText.getText().toString();

            if (TextUtils.isEmpty(replyText)) {
                editText.setError("Please enter a message!");
                editText.requestFocus();
                return;
            }

            if (isFirst) {
                uploadOtherUserDetails();
            }

            ChatMessage message = new ChatMessage();
            message.setMsgText(replyText);
            message.setMsgUser(mAuth.getCurrentUser().getUid());

            editText.setText("");

            adapter.addMessage(message);

            uploadMessage(message);

        });
    }

    private void uploadMessage(ChatMessage message) {

        databaseReference.child("chats").child(mAuth.getCurrentUser().getUid())
                .child(otherUserUid)
                .child("messages")
                .push()
                .setValue(message);

        databaseReference.child("chats").child(otherUserUid)
                .child(mAuth.getCurrentUser().getUid())
                .child("messages")
                .push()
                .setValue(message).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                adapter.removeMessage();
                Toast.makeText(ConversationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // SETTING LAST MESSAGE ON BOTH ACCOUNTS

        databaseReference.child("chats").child(mAuth.getCurrentUser().getUid())
                .child(otherUserUid)
                .child("lastMcg").setValue(message.getMsgText());

        databaseReference.child("chats").child(otherUserUid)
                .child(mAuth.getCurrentUser().getUid())
                .child("lastMcg").setValue(message.getMsgText());

    }

    private void uploadOtherUserDetails() {
        String myName = utils.getStoredString(ConversationActivity.this,
                "usernameStr");
        String myUrl = utils.getStoredString(ConversationActivity.this, "profileUrl");

        databaseReference.child("chats").child(otherUserUid)
                .child(mAuth.getCurrentUser().getUid())
                .child("name").setValue(myName);

        databaseReference.child("chats").child(otherUserUid)
                .child(mAuth.getCurrentUser().getUid())
                .child("imageUrl").setValue(myUrl);

        databaseReference.child("chats").child(mAuth.getCurrentUser().getUid())
                .child(otherUserUid)
                .child("name").setValue(otherUserName);

        databaseReference.child("chats").child(mAuth.getCurrentUser().getUid())
                .child(otherUserUid)
                .child("imageUrl").setValue(otherUserImageUrl);

    }

    private void setBackBtnClickListener() {
        findViewById(R.id.backbtn_conversation_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: ");

        conversationRecyclerView = findViewById(R.id.conversation_recyclerview);
        adapter = new RecyclerViewAdapterMessages();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setStackFromEnd(true);

        conversationRecyclerView.setLayoutManager(linearLayoutManager);
//        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        scrollRecyclerViewToEnd();

    }

    private void scrollRecyclerViewToEnd() {
        Log.d(TAG, "scrollRecyclerViewToEnd: ");
        conversationRecyclerView.scrollToPosition(conversationRecyclerView.getAdapter().getItemCount() - 1);

    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerView.ViewHolder> {

//        @NonNull
//        @Override
//        public ViewHolderMessages onCreateViewHolder(@NonNull ViewGroup parent, int i) {
//            Log.d(TAG, "onCreateViewHolder: ");
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg_left, parent, false);
//            return new ViewHolderMessages(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolderMessages holder, int position) {
//            Log.d(TAG, "onBindViewHolder: " + position);
//
//            if (currentMessagesArrayList.get(holder.getAdapterPosition()).getMsgUser().equals("me")) {
//
//                holder.rightText.setText(currentMessagesArrayList.get(holder.getAdapterPosition()).getMsgText());
//
//                holder.leftText.setVisibility(View.GONE);
//
//            } else {
//
//                holder.leftText.setText(currentMessagesArrayList.get(holder.getAdapterPosition()).getMsgText());
//
//                holder.rightText.setVisibility(View.GONE);
//            }
//
//        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            if (viewType == 1) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg_left, parent, false);
                return new ViewHolderLeftMessage(view);

            } else {
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg_right, parent, false);
                return new ViewHolderRightMessage(view1);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (holder.getItemViewType() == 1) {
                ViewHolderLeftMessage holderLeftMessage = (ViewHolderLeftMessage) holder;
                holderLeftMessage.leftText.setText(currentMessagesArrayList.get(holder.getAdapterPosition()).getMsgText());
            } else {
                ViewHolderRightMessage holderRightMessage = (ViewHolderRightMessage) holder;
                holderRightMessage.rightText.setText(currentMessagesArrayList.get(holder.getAdapterPosition()).getMsgText());
            }

        }

        @Override
        public int getItemCount() {
            if (currentMessagesArrayList == null)
                return 0;
            return currentMessagesArrayList.size();
        }

        String uid = mAuth.getCurrentUser().getUid();

        @Override
        public int getItemViewType(int position) {

            /*

             * 1 Left Message
             * 2 Right Message
             */

            if (currentMessagesArrayList.get(position).getMsgUser().equals(uid)) {
                return 2;
            } else {
                return 1;
            }
        }

        public class ViewHolderLeftMessage extends RecyclerView.ViewHolder {

            TextView leftText;

            public ViewHolderLeftMessage(@NonNull View v) {
                super(v);
                leftText = v.findViewById(R.id.leftText);
            }
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView rightText;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                rightText = v.findViewById(R.id.rightText);
            }
        }

        public void addMessage(ChatMessage c) {
            Log.d(TAG, "addMessage: ");

            currentMessagesArrayList.add(c);

            notifyItemInserted(currentMessagesArrayList.size() - 1);

            scrollRecyclerViewToEnd();

        }

        public void removeMessage() {
            currentMessagesArrayList.remove(currentMessagesArrayList.size() - 1);
            notifyItemRemoved(currentMessagesArrayList.size() - 1);
            notifyItemRangeChanged(currentMessagesArrayList.size() - 1, getItemCount());
        }

    }

    private static class ChatMessage {
        private String msgText;
        private String msgUser;

        public void setMsgText(String msgText) {
            this.msgText = msgText;
        }

        public void setMsgUser(String msgUser) {
            this.msgUser = msgUser;
        }

        public ChatMessage(String msgText, String msgUser) {
            this.msgText = msgText;
            this.msgUser = msgUser;

        }

        public ChatMessage() {
        }

        public String getMsgText() {
            return msgText;
        }

        public String getMsgUser() {
            return msgUser;
        }

    }


}