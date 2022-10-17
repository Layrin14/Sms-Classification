package com.layrin.smsclassification.app

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.layrin.smsclassification.classifier.Model
import com.layrin.smsclassification.classifier.Preprocessing
import com.layrin.smsclassification.data.db.ContactDao
import com.layrin.smsclassification.data.db.ConversationDao
import com.layrin.smsclassification.data.db.MessageDao
import com.layrin.smsclassification.data.db.SmsDb
import com.layrin.smsclassification.data.provider.ContactProvider
import com.layrin.smsclassification.data.provider.SmsManager
import com.layrin.smsclassification.data.provider.SmsProvider
import com.layrin.smsclassification.ui.common.SelectionManager
import com.layrin.smsclassification.util.Constant.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSmsDb(app: Application): SmsDb {
        return Room.databaseBuilder(
            app.applicationContext,
            SmsDb::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideContactDao(db: SmsDb): ContactDao = db.contactDao()

    @Provides
    @Singleton
    fun provideMessageDao(db: SmsDb): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun provideConversationDao(db: SmsDb): ConversationDao = db.conversationDao()

    @Provides
    @Singleton
    fun provideContactProvider(app: Application): ContactProvider =
        ContactProvider(app.applicationContext)

    @Provides
    @Singleton
    fun provideSharedPreference(app: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    @Provides
    @Singleton
    fun provideSmsProvider(app: Application): SmsProvider = SmsProvider(app.applicationContext)

    @Provides
    @Singleton
    fun providesPreprocessing(app: Application): Preprocessing =
        Preprocessing(app.applicationContext)

    @Provides
    @Singleton
    fun provideModel(app: Application, preprocessing: Preprocessing): Model =
        Model(app.applicationContext, preprocessing)

    @Provides
    @Singleton
    fun provideSelectionManager(): SelectionManager = SelectionManager()

    @Provides
    @Singleton
    fun provideSmsManager(
        contactProvider: ContactProvider,
        smsProvider: SmsProvider,
        preferences: SharedPreferences,
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        model: Model,
    ): SmsManager = SmsManager(
        contactProvider,
        smsProvider,
        preferences,
        conversationDao,
        messageDao,
        model
    )
}