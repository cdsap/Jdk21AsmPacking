package com.awesomeapp.module_0_9

sealed class State9_241 {
    data object Loading : State9_241()
    data class Success(val data: String) : State9_241()
    data class Error(val message: String) : State9_241()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}