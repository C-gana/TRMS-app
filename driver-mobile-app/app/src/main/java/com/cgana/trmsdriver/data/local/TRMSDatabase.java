package com.cgana.trmsdriver.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cgana.trmsdriver.data.local.dao.DashboardCacheDao;
import com.cgana.trmsdriver.data.local.dao.DestinationCacheDao;
import com.cgana.trmsdriver.data.local.dao.SyncQueueDao;
import com.cgana.trmsdriver.data.local.entity.DashboardCacheEntity;
import com.cgana.trmsdriver.data.local.entity.DestinationCacheEntity;
import com.cgana.trmsdriver.data.local.entity.SyncQueueEntity;

/**
 * TRMS Room Database (Module 6 Part 1)
 * Local database for offline caching and sync queue
 */
@Database(
    entities = {
        DashboardCacheEntity.class,
        DestinationCacheEntity.class,
        SyncQueueEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class TRMSDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "trms_driver_db";
    private static volatile TRMSDatabase INSTANCE;

    // Abstract DAOs
    public abstract DashboardCacheDao dashboardCacheDao();
    public abstract DestinationCacheDao destinationCacheDao();
    public abstract SyncQueueDao syncQueueDao();

    // Singleton instance
    public static TRMSDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TRMSDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TRMSDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

