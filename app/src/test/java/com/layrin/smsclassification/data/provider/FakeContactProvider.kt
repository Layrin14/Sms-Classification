package com.layrin.smsclassification.data.provider

import com.layrin.smsclassification.data.model.Contact

class FakeContactProvider {
    fun getContactMap(): Map<String, String> {
        val contacts = mutableMapOf<String, String>()
        ('a'..'z').forEachIndexed { index, c ->
            val data = Contact(
                index + 1,
                c.toString() + (index * 2).toString(),
                c.toString(),
                c.toString() + index.toString(),
                c.toString()
            )
            contacts[data.contactPhoneNumber] = data.contactName
        }
        return contacts.toMap()
    }

    fun getContactList(): Array<Contact> {
        val contacts = mutableListOf<Contact>()
        ('a'..'z').forEachIndexed { index, c ->
            val data = Contact(
                contactId = index + 1,
                contactName = c.toString() + (index * 2).toString(),
                contactPhotoUri = c.toString(),
                contactPhoneNumber = c.toString(),
                contactLookUpKey = c.toString()
            )
            contacts.add(data)
        }
        return contacts.toTypedArray()
    }
}