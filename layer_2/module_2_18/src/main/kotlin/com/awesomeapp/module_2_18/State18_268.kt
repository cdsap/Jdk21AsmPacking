package com.awesomeapp.module_2_18

sealed class State18_268 {
    data object Loading : State18_268()
    data class Success(val data: String) : State18_268()
    data class Error(val message: String) : State18_268()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}