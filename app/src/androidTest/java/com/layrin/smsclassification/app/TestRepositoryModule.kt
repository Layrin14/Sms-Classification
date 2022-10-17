package com.layrin.smsclassification.app

import com.layrin.smsclassification.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class TestRepositoryModule {

    @Singleton
    @Binds
    abstract fun bindStartScreenRepository(
        fakeStartScreenRepository: FakeStartScreenRepositoryAndroidTest,
    ): StartScreenRepository


    @Singleton
    @Binds
    abstract fun bindConversationRepository(
        fakeConversationRepository: FakeConversationRepositoryAndroidTest,
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        fakeContactRepository: FakeContactRepositoryAndroidTest,
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        fakeMessageRepository: FakeMessageRepositoryAndroidTest,
    ): MessageRepository
}