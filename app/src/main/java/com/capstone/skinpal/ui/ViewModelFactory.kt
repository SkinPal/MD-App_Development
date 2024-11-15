@file:Suppress("unused", "RedundantSuppression")

package com.capstone.skinpal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.also
import kotlin.jvm.java

class ViewModelFactory private constructor(
    private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(com.capstone.skinpal.ui.product.ProductViewModel::class.java) -> {
                com.capstone.skinpal.ui.product.ProductViewModel(repository) as T
            }

            modelClass.isAssignableFrom(com.capstone.skinpal.ui.home.HomeViewModel::class.java) -> {
                com.capstone.skinpal.ui.home.HomeViewModel(repository) as T
            }

            modelClass.isAssignableFrom(com.capstone.skinpal.ui.login.LoginViewModel::class.java) -> {
                com.capstone.skinpal.ui.login.LoginViewModel(repository) as T
            }

            modelClass.isAssignableFrom(com.capstone.skinpal.ui.register.RegisterViewModel::class.java) -> {
                com.capstone.skinpal.ui.register.RegisterViewModel(repository) as T
            }

            modelClass.isAssignableFrom(com.capstone.skinpal.ui.setting.AccountViewModel::class.java) -> {
                com.capstone.skinpal.ui.setting.AccountViewModel(repository) as T
            }

            modelClass.isAssignableFrom(com.capstone.skinpal.ui.reminder.ReminderViewModel::class.java) -> {
                com.capstone.skinpal.ui.reminder.ReminderViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    com.capstone.skinpal.di.Injection.provideEventRepository(context)
                ).also { instance = it }
            }
    }
}
