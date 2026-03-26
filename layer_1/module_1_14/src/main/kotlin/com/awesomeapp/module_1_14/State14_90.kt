package com.awesomeapp.module_1_14

sealed class State14_90 {
    data object Loading : State14_90()
    data class Success(val data: String) : State14_90()
    data class Error(val message: String) : State14_90()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}