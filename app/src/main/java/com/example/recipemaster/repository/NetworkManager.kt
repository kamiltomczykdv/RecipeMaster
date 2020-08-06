package com.example.recipemaster.repository

import android.content.Context
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork

class NetworkManager {
    fun getReactiveNetwork(context: Context) = ReactiveNetwork
        .observeNetworkConnectivity(context)
}