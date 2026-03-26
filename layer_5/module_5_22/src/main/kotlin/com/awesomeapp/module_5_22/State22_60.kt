package com.awesomeapp.module_5_22

sealed class State22_60 {
    data object Loading : State22_60()
    data class Success(val data: String) : State22_60()
    data class Error(val message: String) : State22_60()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}