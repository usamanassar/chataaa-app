package com.usama.chatapp.model

import com.google.firebase.Timestamp


class Message{
    var messageId:String?=null
    var message:String?=null
    var senderId:String?=null
    var imageUrl:String?=null
    var timestamp:Long=0
    constructor(){}
    constructor(
        message:String?,
        senderId:String?,
        timestamp:Long
    ){
        this.message=message
        this.senderId =senderId
        this.timestamp=timestamp

    }


}
