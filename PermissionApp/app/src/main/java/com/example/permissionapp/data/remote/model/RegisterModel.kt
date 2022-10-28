package com.example.permissionapp.data.remote.model


import com.example.permissionapp.domain.model.Role
import com.google.gson.annotations.SerializedName
import java.util.*

data class RegisterModel(
    @SerializedName("nombre")val name: String,
    @SerializedName("apellido")val lastName: String,
    @SerializedName("email")val email: String,
    @SerializedName("password")val password: String,
    @SerializedName("type_documento") val typeId: String,
    @SerializedName("id_numero")val idNumber: String,
    @SerializedName("bitrthday")val birthday: Date,
    @SerializedName("roles")val role: List<String>
)
