package com.awesomeapp.module_1_12

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.awesomeapp.module_0_8.Api8_6
import com.awesomeapp.module_0_4.Api4_6


@Singleton
class Repository12_5 @Inject constructor(
    private val api0: Api8_6,
    private val api1: Api4_6
) {
    suspend fun getData(): String = withContext(Dispatchers.IO) {
        api0.fetchData() + api1.fetchData()
    }
}