package com.cgana.trmsdriver.sync;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsdriver.data.api.DashboardApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TRMSDatabase;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.local.dao.SyncQueueDao;
import com.cgana.trmsdriver.data.local.entity.SyncQueueEntity;
import com.cgana.trmsdriver.data.model.AlightingRequest;
import com.cgana.trmsdriver.data.model.AlightingResponse;
import com.cgana.trmsdriver.data.model.BoardingRequest;
import com.cgana.trmsdriver.data.model.BoardingResponse;
import com.cgana.trmsdriver.data.model.SetDestinationRequest;
import com.cgana.trmsdriver.data.model.SetDestinationResponse;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Sync Manager (Module 6 Part 2)
 * Manages operation queuing and synchronization
 */
public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final int MAX_RETRIES = 3;

    private Context context;
    private SyncQueueDao syncQueueDao;
    private DashboardApiService apiService;
    private TokenManager tokenManager;
    private Gson gson;
    private ExecutorService executorService;

    private MutableLiveData<SyncStatus> syncStatus = new MutableLiveData<>(SyncStatus.IDLE);
    private MutableLiveData<Integer> pendingOperations = new MutableLiveData<>(0);

    public enum SyncStatus {
        IDLE, SYNCING, SUCCESS, ERROR
    }

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.syncQueueDao = TRMSDatabase.getInstance(context).syncQueueDao();
        this.apiService = RetrofitClient.getInstance().create(DashboardApiService.class);
        this.tokenManager = new TokenManager(context);
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();

        // Observe pending count
        syncQueueDao.getPendingCount().observeForever(count -> {
            pendingOperations.postValue(count != null ? count : 0);
        });
    }

    public LiveData<SyncStatus> getSyncStatus() {
        return syncStatus;
    }

    public LiveData<Integer> getPendingOperationsCount() {
        return pendingOperations;
    }

    /**
     * Queue an operation for later sync
     */
    public void queueOperation(String operationType, Object requestData) {
        executorService.execute(() -> {
            try {
                String jsonData = gson.toJson(requestData);
                SyncQueueEntity entity = new SyncQueueEntity(operationType, jsonData);
                long id = syncQueueDao.insertOperation(entity);
                Log.d(TAG, "Operation queued: " + operationType + " (ID: " + id + ")");
            } catch (Exception e) {
                Log.e(TAG, "Error queueing operation", e);
            }
        });
    }

    /**
     * Sync all pending operations
     */
    public void syncPendingOperations() {
        executorService.execute(() -> {
            syncStatus.postValue(SyncStatus.SYNCING);

            List<SyncQueueEntity> pendingOps = syncQueueDao.getPendingOperations();

            if (pendingOps.isEmpty()) {
                syncStatus.postValue(SyncStatus.SUCCESS);
                return;
            }

            Log.d(TAG, "Syncing " + pendingOps.size() + " pending operations");

            boolean allSuccess = true;

            for (SyncQueueEntity operation : pendingOps) {
                boolean success = processSyncOperation(operation);

                if (!success) {
                    allSuccess = false;
                    // Update retry count
                    operation.setRetry_count(operation.getRetry_count() + 1);

                    if (operation.getRetry_count() >= MAX_RETRIES) {
                        operation.setStatus("FAILED");
                        Log.e(TAG, "Operation failed after max retries: " + operation.getId());
                    } else {
                        operation.setStatus("PENDING");
                    }

                    syncQueueDao.updateOperation(operation);
                } else {
                    // Delete successful operation
                    syncQueueDao.deleteOperation(operation.getId());
                }
            }

            syncStatus.postValue(allSuccess ? SyncStatus.SUCCESS : SyncStatus.ERROR);
        });
    }

    /**
     * Process a single sync operation
     */
    private boolean processSyncOperation(SyncQueueEntity operation) {
        try {
            String token = tokenManager.getToken();
            if (token == null) {
                Log.e(TAG, "No auth token available");
                return false;
            }

            switch (operation.getOperation_type()) {
                case "BOARDING":
                    return syncBoardingOperation(operation, token);

                case "SET_DESTINATION":
                    return syncSetDestinationOperation(operation, token);

                case "ALIGHTING":
                    return syncAlightingOperation(operation, token);

                default:
                    Log.e(TAG, "Unknown operation type: " + operation.getOperation_type());
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing operation", e);
            operation.setError_message(e.getMessage());
            return false;
        }
    }

    private boolean syncBoardingOperation(SyncQueueEntity operation, String token) {
        try {
            BoardingRequest request = gson.fromJson(operation.getRequest_data(), BoardingRequest.class);

            Response<BoardingResponse> response = apiService
                .recordBoarding(request, "Bearer " + token)
                .execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Boarding synced successfully: " + response.body().getJourneyId());
                return true;
            } else {
                Log.e(TAG, "Boarding sync failed: " + response.code());
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing boarding", e);
            return false;
        }
    }

    private boolean syncSetDestinationOperation(SyncQueueEntity operation, String token) {
        try {
            SetDestinationRequest request = gson.fromJson(operation.getRequest_data(), SetDestinationRequest.class);

            Response<SetDestinationResponse> response = apiService
                .setDestination(request, "Bearer " + token)
                .execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Destination synced successfully");
                return true;
            } else {
                Log.e(TAG, "Destination sync failed: " + response.code());
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing destination", e);
            return false;
        }
    }

    private boolean syncAlightingOperation(SyncQueueEntity operation, String token) {
        try {
            AlightingRequest request = gson.fromJson(operation.getRequest_data(), AlightingRequest.class);

            Response<AlightingResponse> response = apiService
                .recordAlighting(request, "Bearer " + token)
                .execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Alighting synced successfully");
                return true;
            } else {
                Log.e(TAG, "Alighting sync failed: " + response.code());
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing alighting", e);
            return false;
        }
    }

    public void clearCompletedOperations() {
        executorService.execute(() -> {
            syncQueueDao.deleteCompleted();
        });
    }
}

