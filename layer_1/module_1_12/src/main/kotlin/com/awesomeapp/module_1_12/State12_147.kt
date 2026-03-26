package com.awesomeapp.module_1_12

sealed class State12_147 {
    data object Loading : State12_147()
    data class Success(val data: String) : State12_147()
    data class Error(val message: String) : State12_147()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}