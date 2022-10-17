package com.layrin.smsclassification.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.layrin.smsclassification.data.common.ModelComparator

@Entity(tableName = "contacts")
data class Contact(
    @ColumnInfo(name = "contact_id")
    val contactId: Int,
    @ColumnInfo(name = "contact_name")
    val contactName: String,
    @ColumnInfo(name = "contact_photo")
    val contactPhotoUri: String? = null,
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "number")
    val contactPhoneNumber: String,
    @ColumnInfo(name = "lookup_key")
    val contactLookUpKey: String
): ModelComparator {
    override val id: Int
        get() = contactId
}
