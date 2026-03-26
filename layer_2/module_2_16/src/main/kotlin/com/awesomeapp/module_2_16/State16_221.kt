package com.awesomeapp.module_2_16

sealed class State16_221 {
    data object Loading : State16_221()
    data class Success(val data: String) : State16_221()
    data class Error(val message: String) : State16_221()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}