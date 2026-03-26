package com.awesomeapp.module_0_9

sealed class State9_94 {
    data object Loading : State9_94()
    data class Success(val data: String) : State9_94()
    data class Error(val message: String) : State9_94()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}