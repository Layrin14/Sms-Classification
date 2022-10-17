package com.layrin.smsclassification.app

import com.layrin.smsclassification.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        contactRepository: DefaultContactRepository
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepository: DefaultMessageRepository
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        conversationRepository: DefaultConversationRepository
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindStartScreenRepository(
        startScreenRepository: DefaultStartScreenRepository
    ): StartScreenRepository
}