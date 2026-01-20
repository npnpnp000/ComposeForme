package com.composeform.utils.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.composeform.data.repositories.Repository
import com.composeform.ui.pages.main.viewmodel.DynamicFormViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val repository: Repository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when (modelClass) {
            DynamicFormViewModel::class.java -> DynamicFormViewModel(repository) as T
            else -> throw Exception("ViewModel not found")
        }
}