package com.example.lyricmemo.di

import android.content.Context
import androidx.room.Room
import com.example.lyricmemo.data.api.VocaDbApi
import com.example.lyricmemo.data.db.AppDatabase
import com.example.lyricmemo.data.db.SavedSongDao
import com.example.lyricmemo.data.repository.VocaDbRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://vocadb.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideVocaDbApi(retrofit: Retrofit): VocaDbApi {
        return retrofit.create(VocaDbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lyric_memo_db"
        )
        .fallbackToDestructiveMigration() // スキーマ変更時にDBを再構築
        .build()
    }

    @Provides
    fun provideSavedSongDao(db: AppDatabase): SavedSongDao {
        return db.savedSongDao()
    }
}