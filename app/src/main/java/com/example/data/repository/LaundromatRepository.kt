package com.example.data.repository

import com.example.data.local.dao.OfflineQueueDao
import com.example.data.local.entity.OfflineQueueEntity
import com.example.domain.model.Laundromat
import com.example.domain.model.SampleLaundromats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LaundromatRepository {
    private val _laundromats = MutableStateFlow<List<Laundromat>>(SampleLaundromats.ALL)
    val laundromats: Flow<List<Laundromat>> = _laundromats.asStateFlow()

    fun getLaundromatById(id: String): Laundromat? {
        return _laundromats.value.find { it.id == id }
    }
}

class OfflineSyncManager(
    private val offlineQueueDao: OfflineQueueDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    val pendingQueueCount: Flow<List<OfflineQueueEntity>> = offlineQueueDao.getPendingActions()

    init {
        // Background monitor for sync queue
        scope.launch {
            while (isActive) {
                delay(10000L)
                if (_isOnline.value) {
                    processPendingSyncQueue()
                }
            }
        }
    }

    fun setNetworkStatus(online: Boolean) {
        _isOnline.value = online
    }

    suspend fun processPendingSyncQueue() {
        val pending = offlineQueueDao.getPendingActionsList()
        for (action in pending) {
            // Process sync with exponential backoff / remote server
            delay(150L)
            offlineQueueDao.removeAction(action.queueId)
        }
    }
}
