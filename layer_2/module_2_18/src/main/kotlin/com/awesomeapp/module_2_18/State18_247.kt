package com.awesomeapp.module_2_18

sealed class State18_247 {
    data object Loading : State18_247()
    data class Success(val data: String) : State18_247()
    data class Error(val message: String) : State18_247()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}