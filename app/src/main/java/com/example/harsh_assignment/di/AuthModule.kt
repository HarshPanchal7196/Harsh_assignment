package com.example.harsh_assignment.di

import com.example.harsh_assignment.data.repository.FakeAuthRepository
import com.example.harsh_assignment.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: FakeAuthRepository,
    ): AuthRepository
}
