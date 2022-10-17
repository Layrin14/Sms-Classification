package com.layrin.smsclassification.data.db

import androidx.paging.PagingSource
import androidx.room.*
import com.layrin.smsclassification.data.model.Contact

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(vararg contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContacts(contacts: Array<Contact>)

    @Query("SELECT * FROM contacts " +
            "WHERE LOWER(contact_name) LIKE :searchQuery " +
            "OR number LIKE :searchQuery " +
            "OR LOWER(contact_name) LIKE :altQuery " +
            "OR number LIKE :altQuery")
    fun getContacts(searchQuery: String, altQuery: String): PagingSource<Int, Contact>

    @Query("SELECT * FROM contacts")
    suspend fun getAllContactFromDb(): Array<Contact>

    @Delete
    suspend fun deleteContact(contact: Contact)
}