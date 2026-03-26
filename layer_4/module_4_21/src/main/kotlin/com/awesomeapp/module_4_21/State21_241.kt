package com.awesomeapp.module_4_21

sealed class State21_241 {
    data object Loading : State21_241()
    data class Success(val data: String) : State21_241()
    data class Error(val message: String) : State21_241()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}