package com.layrin.smsclassification.app

import android.app.Application
import androidx.room.Room
import com.layrin.smsclassification.data.db.SmsDb
import com.layrin.smsclassification.data.provider.FakeContactProvider
import com.layrin.smsclassification.data.provider.FakeSmsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    @Named("test_db")
    fun provideSmsDb(app: Application): SmsDb {
        return Room.inMemoryDatabaseBuilder(
            app,
            SmsDb::class.java
        ).allowMainThreadQueries()
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    @Provides
    @Singleton
    fun provideFakeSmsProvider() = FakeSmsProvider()

    @Provides
    @Singleton
    fun provideFakeContactProvider() = FakeContactProvider()
}