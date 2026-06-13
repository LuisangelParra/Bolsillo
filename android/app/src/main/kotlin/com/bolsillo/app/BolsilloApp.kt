package com.bolsillo.app

import android.app.Application
import com.bolsillo.data.seed.AppSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BolsilloApp : Application() {
    @Inject lateinit var seeder: AppSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // First-launch seed: default Cash account + category taxonomy.
        // Idempotent — safe to re-run on every launch.
        appScope.launch { seeder.seed() }
    }
}
