package com.sshclient.model

import android.os.Parcel
import android.os.Parcelable

data class ConnectionConfig(
    val id: String = "",
    val name: String = "",
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val authMethod: AuthMethod = AuthMethod.PASSWORD,
    val password: String = "",
    val privateKeyPath: String = "",
    val passphrase: String = "",
    val characterSet: String = "UTF-8",
    val encoding: String = "UTF-8",
    val compress: Boolean = false,
    val keepAliveInterval: Int = 30,
    val timeout: Int = 10000
) : Parcelable {
    enum class AuthMethod { PASSWORD, PRIVATE_KEY, AGENT }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(host)
        parcel.writeInt(port)
        parcel.writeString(username)
        parcel.writeString(authMethod.name)
        parcel.writeString(password)
        parcel.writeString(privateKeyPath)
        parcel.writeString(passphrase)
        parcel.writeString(characterSet)
        parcel.writeString(encoding)
        parcel.writeValue(compress)
        parcel.writeInt(keepAliveInterval)
        parcel.writeInt(timeout)
    }

    override fun describeContents(): Int = 0

    protected constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        AuthMethod.valueOf(parcel.readString() ?: "PASSWORD"),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "UTF-8",
        parcel.readString() ?: "UTF-8",
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean ?: false,
        parcel.readInt(),
        parcel.readInt()
    )

    companion object CREATOR : Parcelable.Creator<ConnectionConfig> {
        override fun createFromParcel(parcel: Parcel): ConnectionConfig = ConnectionConfig(parcel)
        override fun newArray(size: Int): Array<ConnectionConfig?> = arrayOfNulls(size)
    }
}
