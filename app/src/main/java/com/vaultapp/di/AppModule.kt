package com.vaultapp.di

import android.content.Context
import androidx.room.Room
import com.vaultapp.data.local.*
import com.vaultapp.data.local.VaultDatabase
import com.vaultapp.data.remote.GitHubService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): VaultDatabase =
        Room.databaseBuilder(ctx, VaultDatabase::class.java, "vault.db")
            .fallbackToDestructiveMigration().build()

    @Provides fun provideNoteDao(db: VaultDatabase): NoteDao         = db.noteDao()
    @Provides fun providePasswordDao(db: VaultDatabase): PasswordDao = db.passwordDao()

    @Provides @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideGitHubService(retrofit: Retrofit): GitHubService =
        retrofit.create(GitHubService::class.java)
}
