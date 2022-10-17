package com.layrin.smsclassification.data.provider

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.layrin.smsclassification.data.model.Contact
import com.layrin.smsclassification.data.util.getIntValue
import com.layrin.smsclassification.data.util.getStringValue
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.*

class ContactProvider(
    private val context: Context,
) {

    fun getContactMap(): Map<String, String> {
        val uri = ContactsContract.Contacts.CONTENT_URI
        val query = context.contentResolver.query(
            uri, projection(), null, null, null
        )
        val data = mutableMapOf<String, String>()

        query.use { cursor ->
            if (cursor?.moveToFirst() == true) {
                do {
                    val id = cursor.getStringValue(ContactsContract.Contacts._ID)
                    val name = cursor.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
                    if (cursor.getIntValue(ContactsContract.Contacts.HAS_PHONE_NUMBER) != 0) {
                        val phoneQuery = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        phoneQuery.use { phoneCursor ->
                            if (phoneCursor?.moveToFirst() == true) {
                                do {
                                    val phoneNumber =
                                        phoneCursor.getStringValue(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    data[phoneNumber] = name
                                } while (phoneCursor.moveToNext())
                            }
                            phoneCursor?.close()
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor?.close()
        }
        return data.toMap()
    }

    @SuppressLint("Range")
    fun getContactList(): Array<Contact> {
        val phoneNumberUri = ContactsContract.Contacts.CONTENT_URI
        val phoneQuery = context.contentResolver.query(
            phoneNumberUri, projection(), null, null, null
        )
        val contactList = mutableSetOf<Contact>()

        phoneQuery.use { cursor ->
            if (cursor?.moveToFirst() == true) {
                do {
                    val contactId =
                        cursor.getStringValue(ContactsContract.Contacts._ID)
                    val photoUri =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val name =
                        cursor.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
                    val lookupKey = cursor.getStringValue(ContactsContract.Contacts.LOOKUP_KEY)
                    if (cursor.getIntValue(ContactsContract.Contacts.HAS_PHONE_NUMBER) > 0) {
                        val numberQuery = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )
                        numberQuery.use { numberCursor ->
                            if (numberCursor?.moveToFirst() == true) {
                                do {
                                    val number =
                                        numberCursor.getStringValue(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    contactList.add(
                                        Contact(contactId.toInt(),
                                            name,
                                            photoUri,
                                            getCleanNumber(number, context),
                                            lookupKey)
                                    )
                                } while (numberCursor.moveToNext())
                            }
                            numberCursor?.close()
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor?.close()
        }
        val array = contactList.toTypedArray()
        array.sortBy { it.contactName.lowercase(Locale.ROOT) }

        return array
    }

    private fun projection() = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
        ContactsContract.Contacts.HAS_PHONE_NUMBER,
        ContactsContract.Contacts.LOOKUP_KEY
    )

    companion object {
        fun getCleanNumber(number: String, context: Context): String {
            val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
            return try {
                val countryIso =
                    (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso
                val proto = phoneNumberUtil.parse(
                    number,
                    countryIso.uppercase(Locale.ROOT)
                )
                if (proto.nationalNumber.toString().length < 7) proto.nationalNumber.toString()
                else "+${proto.countryCode} ${proto.nationalNumber}"
            } catch (e: NumberParseException) {
                number.filter {
                    it.isLetterOrDigit()
                }
            }
        }

        fun getContactPhoto(id: Long, context: Context): Bitmap? {
            return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
                val inputStream =
                    ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver,
                        uri)
                BitmapFactory.decodeStream(inputStream)
            } else null

        }
    }
}