package com.awesomeapp.module_2_16

sealed class State16_266 {
    data object Loading : State16_266()
    data class Success(val data: String) : State16_266()
    data class Error(val message: String) : State16_266()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}