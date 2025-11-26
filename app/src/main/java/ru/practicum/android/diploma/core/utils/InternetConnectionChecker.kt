package ru.practicum.android.diploma.core.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission

class InternetConnectionChecker(context: Context) { // provides function isConnected(): Boolean

    val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnected(): Boolean {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        var connectionFound = false
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> connectionFound = true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> connectionFound = true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> connectionFound = true
            }
        }
        return connectionFound
    }
}
