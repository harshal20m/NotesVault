package com.vaultapp.di

import android.content.Context
import androidx.room.Room
import com.vaultapp.data.local.*
import com.vaultapp.data.local.VaultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
}
