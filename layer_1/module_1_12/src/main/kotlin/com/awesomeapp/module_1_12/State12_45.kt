package com.awesomeapp.module_1_12

sealed class State12_45 {
    data object Loading : State12_45()
    data class Success(val data: String) : State12_45()
    data class Error(val message: String) : State12_45()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}