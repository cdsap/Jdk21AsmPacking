package com.awesomeapp.module_2_18

sealed class State18_232 {
    data object Loading : State18_232()
    data class Success(val data: String) : State18_232()
    data class Error(val message: String) : State18_232()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}