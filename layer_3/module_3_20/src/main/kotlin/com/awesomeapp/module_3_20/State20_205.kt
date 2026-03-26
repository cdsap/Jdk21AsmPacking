package com.awesomeapp.module_3_20

sealed class State20_205 {
    data object Loading : State20_205()
    data class Success(val data: String) : State20_205()
    data class Error(val message: String) : State20_205()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}