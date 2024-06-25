package com.usama.chatapp.adapter

import android.content.Context
import com.usama.chatapp.model.Message

class ConcreteMessagesAdapter(
    context: Context,
    messages: ArrayList<Message>,
    senderRoom: String,
    receiverRoom: String
) : MessagesAdapter(context, messages, senderRoom, receiverRoom)