package com.cgana.trmsownerapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cgana.trmsownerapp.data.local.entity.AlertEntity;

import java.util.List;

@Dao
public interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AlertEntity> alerts);

    @Update
    void update(AlertEntity alert);

    @Query("SELECT * FROM alerts_cache WHERE vehicle_id = :vehicleId ORDER BY created_at DESC")
    List<AlertEntity> getAlerts(String vehicleId);

    @Query("SELECT * FROM alerts_cache WHERE alert_id = :alertId LIMIT 1")
    AlertEntity getAlert(int alertId);

    @Query("DELETE FROM alerts_cache WHERE vehicle_id = :vehicleId")
    void deleteAlerts(String vehicleId);

    @Query("DELETE FROM alerts_cache")
    void deleteAll();
}

