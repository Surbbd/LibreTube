package com.github.libretube.api

import android.util.Log
import com.github.libretube.api.obj.Subscribe
import com.github.libretube.db.DatabaseHolder.Companion.Database
import com.github.libretube.db.obj.LocalSubscription
import com.github.libretube.extensions.TAG
import com.github.libretube.extensions.await
import com.github.libretube.extensions.awaitQuery
import com.github.libretube.extensions.query
import com.github.libretube.util.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SubscriptionHelper {

    fun subscribe(channelId: String) {
        if (PreferenceHelper.getToken() != "") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitInstance.authApi.subscribe(
                        PreferenceHelper.getToken(),
                        com.github.libretube.api.obj.Subscribe(channelId)
                    )
                } catch (e: Exception) {
                    Log.e(TAG(), e.toString())
                }
            }
        } else {
            query {
                Database.localSubscriptionDao().insertAll(
                    LocalSubscription(channelId)
                )
            }
        }
    }

    fun unsubscribe(channelId: String) {
        if (PreferenceHelper.getToken() != "") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitInstance.authApi.unsubscribe(
                        PreferenceHelper.getToken(),
                        com.github.libretube.api.obj.Subscribe(channelId)
                    )
                } catch (e: Exception) {
                    Log.e(TAG(), e.toString())
                }
            }
        } else {
            query {
                Database.localSubscriptionDao().delete(
                    LocalSubscription(channelId)
                )
            }
        }
    }

    suspend fun isSubscribed(channelId: String): Boolean? {
        if (PreferenceHelper.getToken() != "") {
            val isSubscribed = try {
                RetrofitInstance.authApi.isSubscribed(
                    channelId,
                    PreferenceHelper.getToken()
                )
            } catch (e: Exception) {
                Log.e(TAG(), e.toString())
                return null
            }
            return isSubscribed.subscribed
        } else {
            return awaitQuery {
                Database.localSubscriptionDao().includes(channelId)
            }
        }
    }

    suspend fun importSubscriptions(newChannels: List<String>) {
        if (PreferenceHelper.getToken() != "") {
            try {
                val token = PreferenceHelper.getToken()
                RetrofitInstance.authApi.importSubscriptions(
                    false,
                    token,
                    newChannels
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val newLocalSubscriptions = mutableListOf<LocalSubscription>()
            newChannels.forEach {
                newLocalSubscriptions += LocalSubscription(channelId = it)
            }
            query {
                Database.localSubscriptionDao().insertAll(
                    *newChannels.map { LocalSubscription(it) }.toTypedArray()
                )
            }
        }
    }

    fun getLocalSubscriptions(): List<LocalSubscription> {
        return awaitQuery {
            Database.localSubscriptionDao().getAll()
        }
    }

    fun getFormattedLocalSubscriptions(): String {
        val localSubscriptions = getLocalSubscriptions()
        return localSubscriptions.map { it.channelId }.joinToString(",")
    }
}
