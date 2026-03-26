package com.awesomeapp.module_4_21

sealed class State21_259 {
    data object Loading : State21_259()
    data class Success(val data: String) : State21_259()
    data class Error(val message: String) : State21_259()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}