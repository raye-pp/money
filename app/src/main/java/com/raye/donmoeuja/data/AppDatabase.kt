package com.raye.donmoeuja.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Category::class, Entry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        /** 처음 설치할 때 넣어 두는 기본 카테고리 */
        val DEFAULT_CATEGORIES = listOf("식비", "간식", "교통", "문구", "용돈", "저축")

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "don_moeuja.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        DEFAULT_CATEGORIES.forEachIndexed { index, name ->
                            db.execSQL(
                                "INSERT INTO categories (name, sortOrder) VALUES (?, ?)",
                                arrayOf<Any>(name, index),
                            )
                        }
                    }
                })
                .build()
    }
}
