package com.cgana.trmsdriver.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgana.trmsdriver.data.local.entity.DashboardCacheEntity;

/**
 * Dashboard Cache DAO (Module 6 Part 1)
 */
@Dao
public interface DashboardCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDashboard(DashboardCacheEntity dashboard);

    @Query("SELECT * FROM dashboard_cache WHERE vehicle_id = :vehicleId ORDER BY cached_at DESC LIMIT 1")
    DashboardCacheEntity getLatestDashboard(String vehicleId);

    @Query("SELECT * FROM dashboard_cache WHERE vehicle_id = :vehicleId ORDER BY cached_at DESC LIMIT 1")
    LiveData<DashboardCacheEntity> getLatestDashboardLive(String vehicleId);

    @Query("DELETE FROM dashboard_cache WHERE vehicle_id = :vehicleId")
    void clearDashboard(String vehicleId);

    @Query("DELETE FROM dashboard_cache WHERE cached_at < :expiryTime")
    void deleteExpiredCache(long expiryTime);
}

