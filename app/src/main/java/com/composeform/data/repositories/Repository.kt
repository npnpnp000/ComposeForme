package com.composeform.data.repositories

import com.composeform.data.remote.source.RemoteDataSource

class Repository(
    private val remoteDataSource: RemoteDataSource,
) {

    suspend fun fetchSchema() = remoteDataSource.fetchSchema()

    suspend fun fetchPrefillData(dataUrl: String) = remoteDataSource.fetchPrefillData(dataUrl)
}