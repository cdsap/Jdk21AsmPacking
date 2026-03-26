package com.awesomeapp.module_5_22

sealed class State22_84 {
    data object Loading : State22_84()
    data class Success(val data: String) : State22_84()
    data class Error(val message: String) : State22_84()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}