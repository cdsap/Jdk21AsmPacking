package com.awesomeapp.module_2_17

sealed class State17_57 {
    data object Loading : State17_57()
    data class Success(val data: String) : State17_57()
    data class Error(val message: String) : State17_57()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}