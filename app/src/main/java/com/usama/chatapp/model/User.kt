package com.usama.chatapp.model

data class User(
    var uid: String? = null,
    var name: String? = null,
    var phoneNumber: String? = null,
    var profileImage: String? = null
) {
    constructor() : this(null, null, null, null)
}