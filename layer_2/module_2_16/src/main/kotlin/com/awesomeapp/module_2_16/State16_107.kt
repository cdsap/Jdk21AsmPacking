package com.awesomeapp.module_2_16

sealed class State16_107 {
    data object Loading : State16_107()
    data class Success(val data: String) : State16_107()
    data class Error(val message: String) : State16_107()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}