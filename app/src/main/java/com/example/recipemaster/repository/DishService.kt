package com.example.recipemaster.repository

import com.example.recipemaster.model.Dish
import io.reactivex.Observable
import retrofit2.http.GET

interface DishService {
    @GET("test/info.php")
    fun getDish(): Observable<Dish>
}