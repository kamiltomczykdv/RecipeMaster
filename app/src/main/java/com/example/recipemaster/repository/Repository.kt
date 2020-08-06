package com.example.recipemaster.repository

import android.content.Context
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

const val baseUrl = " https://moodup.team/"

class Repository {
    private lateinit var retrofit: Retrofit
    private lateinit var dishService: DishService
    private lateinit var networkManager: NetworkManager

    fun getInsatance() {
        retrofit = Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
        dishService = retrofit.create(DishService::class.java)
        networkManager = NetworkManager()
    }

    fun getDish() = dishService.getDish().subscribeOn(Schedulers.io())

    fun checkConnection(context: Context) = networkManager.getReactiveNetwork(context)
}