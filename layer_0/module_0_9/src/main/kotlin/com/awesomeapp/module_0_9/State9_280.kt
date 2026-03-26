package com.awesomeapp.module_0_9

sealed class State9_280 {
    data object Loading : State9_280()
    data class Success(val data: String) : State9_280()
    data class Error(val message: String) : State9_280()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}