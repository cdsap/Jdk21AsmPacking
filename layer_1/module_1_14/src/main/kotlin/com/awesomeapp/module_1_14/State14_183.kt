package com.awesomeapp.module_1_14

sealed class State14_183 {
    data object Loading : State14_183()
    data class Success(val data: String) : State14_183()
    data class Error(val message: String) : State14_183()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}