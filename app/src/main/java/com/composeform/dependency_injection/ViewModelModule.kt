package com.composeform.dependency_injection

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.composeform.ui.pages.main.viewmodel.DynamicFormViewModel
import com.composeform.utils.factories.ViewModelFactory


object ViewModelModule {

    inline fun <reified VM : ViewModel> provideViewModel(activity: ComponentActivity): Lazy<VM> {
        val viewModelFactory = when (VM::class.java) {
            DynamicFormViewModel::class.java -> {
                ViewModelFactory(RepositoryModule.provideRepository())
            }

            else -> throw RuntimeException("ViewModel does not exist")
        }
        return lazy { ViewModelProvider(activity, viewModelFactory)[VM::class.java] }
    }
}