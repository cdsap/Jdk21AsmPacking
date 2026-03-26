package com.awesomeapp.module_2_18

sealed class State18_280 {
    data object Loading : State18_280()
    data class Success(val data: String) : State18_280()
    data class Error(val message: String) : State18_280()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}