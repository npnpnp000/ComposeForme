package com.composeform.dependency_injection

import com.composeform.data.repositories.Repository

object RepositoryModule {

    fun provideRepository(): Repository {
        return Repository(
            DataSourceModule.provideRemoteDataSource()
        )
    }
}