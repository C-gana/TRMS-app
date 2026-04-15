package com.cgana.trmsownerapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgana.trmsownerapp.data.local.entity.DestinationEntity;

import java.util.List;

@Dao
public interface DestinationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DestinationEntity> destinations);

    @Query("SELECT * FROM destinations_cache WHERE vehicle_id = :vehicleId")
    List<DestinationEntity> getDestinations(String vehicleId);

    @Query("DELETE FROM destinations_cache WHERE destination_id = :destinationId")
    void deleteDestination(int destinationId);

    @Query("DELETE FROM destinations_cache WHERE vehicle_id = :vehicleId")
    void deleteDestinations(String vehicleId);

    @Query("DELETE FROM destinations_cache")
    void deleteAll();
}

