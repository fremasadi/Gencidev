package com.example.gencidevtest.di

import com.example.gencidevtest.data.repository.AuthRepositoryImpl
import com.example.gencidevtest.data.repository.CartRepositoryImpl
import com.example.gencidevtest.data.repository.ProductRepositoryImpl
import com.example.gencidevtest.domain.repository.AuthRepository
import com.example.gencidevtest.domain.repository.CartRepository
import com.example.gencidevtest.domain.repository.ProductRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository
}