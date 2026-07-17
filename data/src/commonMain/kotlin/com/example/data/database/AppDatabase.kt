package com.example.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.database.converter.DatabaseConverters
import com.example.data.database.dao.*
import com.example.data.database.entity.*

@Database(
    entities = [
        PredictionRecord::class,
        SecondRecord::class,
        CommentRecord::class,
        DiscourseRecord::class,
        SubmissionRecord::class,
        UserRecord::class,
        KeepTabRecord::class,
        SecondingSnapshotRecord::class,
        SecondingSnapshotOutcomeRecord::class,
        SecondingSnapshotDownsampledRecord::class,
        AppealSubmissionRecord::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao
    abstract fun secondDao(): SecondDao
    abstract fun commentDao(): CommentDao
    abstract fun discourseDao(): DiscourseDao
    abstract fun secondingSnapshotDao(): SecondingSnapshotDao
    abstract fun submissionDao(): SubmissionDao
    abstract fun userDao(): UserDao
    abstract fun keepTabDao(): KeepTabDao
    abstract fun appealSubmissionDao(): AppealSubmissionDao
}

fun RoomDatabase.Builder<AppDatabase>.configureEugeneDatabase(): RoomDatabase.Builder<AppDatabase> {
    return this
        .fallbackToDestructiveMigration(true)
        .fallbackToDestructiveMigrationOnDowngrade(true)
}
