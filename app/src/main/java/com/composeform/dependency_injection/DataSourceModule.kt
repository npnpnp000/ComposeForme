package com.composeform.dependency_injection

import com.composeform.data.remote.source.RemoteDataSource


object DataSourceModule {


    fun provideRemoteDataSource() : RemoteDataSource {
        return RemoteDataSource(NetworkModule.provideFormApi())
    }

}