package com.usama.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.usama.chatapp.R
import com.usama.chatapp.databinding.DeleteLayoutBinding
import com.usama.chatapp.databinding.ReceiveMsgBinding
import com.usama.chatapp.databinding.SendMsgBinding
import com.usama.chatapp.model.Message
 open class MessagesAdapter(
    private var context: Context,
    messages: ArrayList<Message> = ArrayList(),
    senderRoom: String,
    receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>()
{

    private lateinit var messages:ArrayList<Message>
    private val ITEM_SENT=1
    private val ITEM_RECEIVE=2
    private val senderRoom:String
    private var receiverRoom:String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == ITEM_SENT){
            val view:View =LayoutInflater.from(context).inflate(R.layout.send_msg,parent,false)
            SentMsgHolder(view)
        }
        else{
            val view=LayoutInflater.from(context).inflate(R.layout.receive_msg,parent,false)
            ReceiveMsgHolder(view)
        }
    }



    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId){
            ITEM_SENT
        }
        else{
            ITEM_RECEIVE
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java) {
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message.toString()
            viewHolder.itemView.setOnClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->

                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)


                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)

            } else {

                viewHolder.binding.message.text = message.message.toString()
                viewHolder.itemView.setOnClickListener {
                    val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                    val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                    val dialog = AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.root)
                        .create()
                    binding.everyone.setOnClickListener {
                        message.message = "This message is removed"
                        message.messageId?.let { it1 ->
                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(senderRoom)
                                .child("message")
                                .child(it1).setValue(message)
                        }
                        message.messageId.let { it1 ->

                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(receiverRoom)
                                .child("message")
                                .child(it1!!).setValue(message)


                        }
                        dialog.dismiss()
                    }
                    binding.delete.setOnClickListener {
                        message.messageId.let { it1 ->
                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(senderRoom)
                                .child("message")
                                .child(it1!!).setValue(null)
                        }
                        dialog.dismiss()
                    }
                    binding.cancel.setOnClickListener { dialog.dismiss() }
                    dialog.show()
                }
            }



        }


    }

    override fun getItemCount(): Int =messages.size


      inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }
    init {
        if (messages !=null){
           this.messages=messages
        }
        this.senderRoom =  senderRoom
        this.receiverRoom = receiverRoom

    }
}










