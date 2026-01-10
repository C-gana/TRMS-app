package com.cgana.trmsdriver.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cgana.trmsdriver.data.local.entity.SyncQueueEntity;

import java.util.List;

/**
 * Sync Queue DAO (Module 6 Part 1)
 */
@Dao
public interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOperation(SyncQueueEntity operation);

    @Update
    void updateOperation(SyncQueueEntity operation);

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC")
    List<SyncQueueEntity> getPendingOperations();

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC")
    LiveData<List<SyncQueueEntity>> getPendingOperationsLive();

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    LiveData<Integer> getPendingCount();

    @Query("DELETE FROM sync_queue WHERE id = :id")
    void deleteOperation(int id);

    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    void deleteCompleted();

    @Query("DELETE FROM sync_queue")
    void clearAll();
}

