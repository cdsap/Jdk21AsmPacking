package com.awesomeapp.module_3_20

sealed class State20_115 {
    data object Loading : State20_115()
    data class Success(val data: String) : State20_115()
    data class Error(val message: String) : State20_115()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}