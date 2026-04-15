package com.cgana.trmsownerapp.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cgana.trmsownerapp.data.local.dao.AlertDao;
import com.cgana.trmsownerapp.data.local.dao.DashboardDao;
import com.cgana.trmsownerapp.data.local.dao.DestinationDao;
import com.cgana.trmsownerapp.data.local.dao.JourneyDao;
import com.cgana.trmsownerapp.data.local.entity.AlertEntity;
import com.cgana.trmsownerapp.data.local.entity.DashboardEntity;
import com.cgana.trmsownerapp.data.local.entity.DestinationEntity;
import com.cgana.trmsownerapp.data.local.entity.JourneyEntity;

@Database(
        entities = {
                DashboardEntity.class,
                JourneyEntity.class,
                DestinationEntity.class,
                AlertEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class TRMSDatabase extends RoomDatabase {

    private static TRMSDatabase instance;

    public abstract DashboardDao dashboardDao();

    public abstract JourneyDao journeyDao();

    public abstract DestinationDao destinationDao();

    public abstract AlertDao alertDao();

    public static synchronized TRMSDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TRMSDatabase.class,
                            "trms_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

