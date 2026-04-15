package com.cgana.trmsownerapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgana.trmsownerapp.data.local.entity.DashboardEntity;

@Dao
public interface DashboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DashboardEntity dashboard);

    @Query("SELECT * FROM dashboard_cache WHERE vehicle_id = :vehicleId LIMIT 1")
    DashboardEntity getDashboard(String vehicleId);

    @Query("DELETE FROM dashboard_cache WHERE vehicle_id = :vehicleId")
    void deleteDashboard(String vehicleId);

    @Query("DELETE FROM dashboard_cache")
    void deleteAll();
}

