package com.awesomeapp.module_2_18

sealed class State18_31 {
    data object Loading : State18_31()
    data class Success(val data: String) : State18_31()
    data class Error(val message: String) : State18_31()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}