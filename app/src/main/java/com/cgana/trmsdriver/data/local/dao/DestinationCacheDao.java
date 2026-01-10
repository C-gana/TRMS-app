package com.cgana.trmsdriver.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgana.trmsdriver.data.local.entity.DestinationCacheEntity;

import java.util.List;

/**
 * Destinations Cache DAO (Module 6 Part 1)
 */
@Dao
public interface DestinationCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDestinations(List<DestinationCacheEntity> destinations);

    @Query("SELECT * FROM destinations_cache WHERE vehicle_id = :vehicleId AND status = 'active' ORDER BY name ASC")
    List<DestinationCacheEntity> getDestinations(String vehicleId);

    @Query("SELECT * FROM destinations_cache WHERE vehicle_id = :vehicleId AND status = 'active' ORDER BY name ASC")
    LiveData<List<DestinationCacheEntity>> getDestinationsLive(String vehicleId);

    @Query("DELETE FROM destinations_cache WHERE vehicle_id = :vehicleId")
    void clearDestinations(String vehicleId);

    @Query("DELETE FROM destinations_cache WHERE cached_at < :expiryTime")
    void deleteExpiredCache(long expiryTime);
}

