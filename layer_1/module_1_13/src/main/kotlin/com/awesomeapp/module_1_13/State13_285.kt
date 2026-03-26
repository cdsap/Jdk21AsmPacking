package com.awesomeapp.module_1_13

sealed class State13_285 {
    data object Loading : State13_285()
    data class Success(val data: String) : State13_285()
    data class Error(val message: String) : State13_285()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}