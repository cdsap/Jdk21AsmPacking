package com.awesomeapp.module_2_17

sealed class State17_78 {
    data object Loading : State17_78()
    data class Success(val data: String) : State17_78()
    data class Error(val message: String) : State17_78()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}