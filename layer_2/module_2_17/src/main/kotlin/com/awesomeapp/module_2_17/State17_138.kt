package com.awesomeapp.module_2_17

sealed class State17_138 {
    data object Loading : State17_138()
    data class Success(val data: String) : State17_138()
    data class Error(val message: String) : State17_138()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}