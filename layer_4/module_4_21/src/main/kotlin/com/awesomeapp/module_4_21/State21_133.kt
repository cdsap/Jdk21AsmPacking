package com.awesomeapp.module_4_21

sealed class State21_133 {
    data object Loading : State21_133()
    data class Success(val data: String) : State21_133()
    data class Error(val message: String) : State21_133()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}