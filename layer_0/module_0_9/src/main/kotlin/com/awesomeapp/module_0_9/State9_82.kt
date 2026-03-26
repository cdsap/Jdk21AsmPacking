package com.awesomeapp.module_0_9

sealed class State9_82 {
    data object Loading : State9_82()
    data class Success(val data: String) : State9_82()
    data class Error(val message: String) : State9_82()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}