package com.awesomeapp.module_1_12

sealed class State12_228 {
    data object Loading : State12_228()
    data class Success(val data: String) : State12_228()
    data class Error(val message: String) : State12_228()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}