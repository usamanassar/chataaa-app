package com.usama.chatapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.usama.chatapp.databinding.ActivityChatAcativityBinding
import com.usama.chatapp.model.Message
import java.util.Calendar
import com.usama.chatapp.adapter.ConcreteMessagesAdapter
import com.usama.chatapp.adapter.MessagesAdapter
import java.util.Date


class ChatActivity : AppCompatActivity() {
     var binding: ActivityChatAcativityBinding? = null
     var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>? = null
     private var senderRoom: String? = null
     private var receiverRoom: String? = null
     var database: FirebaseDatabase? = null
     private var storage: FirebaseStorage? = null
     private var dialog: ProgressDialog? = null
     var senderUid: String? = null
     private var receiverUid: String? = null
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatAcativityBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text=name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.placeholder)
            .into(binding!!.profile01)
        binding!!.ImageView2.setOnClickListener{finish()}
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if (status=="offLine"){
                            binding!!.status.visibility = View.GONE
                        }
                        else{
                            binding!!.status.setText(status)
                            binding!!.status.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) { }
            })
        senderRoom = senderUid+receiverUid
        receiverRoom = receiverUid+senderUid
        adapter = MessagesAdapter(this@ChatActivity, messages!!, senderRoom!!, receiverRoom!!)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.recyclerView.adapter=adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object :ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children){
                        val message:Message?= snapshot1.getValue(Message::class.java)
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
        binding!!.sendbtn.setOnClickListener {
            val messageTxt: String = binding!!.messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt, senderUid, date.time)
            binding!!.messageBox.setText("")
            val randomKey = database!!.reference.push().key

            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            if (message.message != null) {
                lastMsgObj["lastMsg"] = message.message!!
            } else {
                lastMsgObj["lastMsg"] = "No message available usama jee"
            }
            lastMsgObj["lastMsgTime"] = date.time
           database!!.reference.child("chats").child(senderRoom!!)
               .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages").child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("message").child(randomKey)
                        .setValue(message).addOnSuccessListener {
                            database!!.reference.child("chats")
                                .child(receiverRoom!!)
                                .child("message")
                                .child(randomKey)
                                .setValue(message)
                                .addOnSuccessListener {  }
                        }
                }
        }
        binding!!.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }
        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.postDelayed(userStoppedTyping,1000)

            }
            var userStoppedTyping= Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")

            }


        })
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25 && resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data
            val calendar = Calendar.getInstance()
            val reference = storage!!.reference.child("chats")
                .child(calendar.timeInMillis.toString() + "")
            dialog!!.show()
            reference.putFile(selectedImage!!)
                .addOnCompleteListener { task ->
                    dialog!!.dismiss()
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val filePath = uri.toString()
                            val messageTxt: String = binding!!.messageBox.text.toString()
                            val date = java.util.Date()
                            val message = Message(messageTxt, senderUid, date.time)
                            message.message = "photo"
                            message.imageUrl = filePath
                            binding!!.messageBox.setText("")
                            val randomKey = database!!.reference.push().key

                            // Update last message and time for both sender and receiver rooms
                            val lastMsgObj = HashMap<String, Any>()
                            lastMsgObj["lastMsg"] = message.message!!
                            lastMsgObj["lastMsgTime"] = date.time
                            database!!.reference.child("chats")
                                .child(senderRoom!!)
                                .child("messages")
                                .child(randomKey!!)
                                .setValue(message).addOnSuccessListener {
                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message)
                                        .addOnSuccessListener {  }
                                }
                            database!!.reference.child("chats")
                                .child(receiverRoom!!).updateChildren(lastMsgObj)

                            // Store the message in sender's and receiver's message collections
                            database!!.reference.child("chats")
                                .child(senderRoom!!).child("messages")
                                .child(randomKey!!).setValue(message)
                                .addOnSuccessListener {
                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!).child("message")
                                        .child(randomKey).setValue(message)
                                        .addOnSuccessListener {}
                                }
                        }
                    }
                }
        }
    }


    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Offline")
    }

}


