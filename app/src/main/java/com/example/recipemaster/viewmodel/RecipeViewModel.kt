package com.example.recipemaster.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.recipemaster.repository.Repository

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository

    init {
        repository = Repository()
        repository.getInsatance()
    }

    fun getDish() = repository.getDish()
    fun checkConnection(context: Context) = repository.checkConnection(context)
}