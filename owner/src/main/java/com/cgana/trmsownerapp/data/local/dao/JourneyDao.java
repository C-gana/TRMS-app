package com.cgana.trmsownerapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgana.trmsownerapp.data.local.entity.JourneyEntity;

import java.util.List;

@Dao
public interface JourneyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<JourneyEntity> journeys);

    @Query("SELECT * FROM journeys_cache WHERE vehicle_id = :vehicleId ORDER BY boarding_time DESC")
    List<JourneyEntity> getJourneys(String vehicleId);

    @Query("DELETE FROM journeys_cache WHERE vehicle_id = :vehicleId")
    void deleteJourneys(String vehicleId);

    @Query("DELETE FROM journeys_cache")
    void deleteAll();
}

