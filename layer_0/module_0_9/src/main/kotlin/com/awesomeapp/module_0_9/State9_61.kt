package com.awesomeapp.module_0_9

sealed class State9_61 {
    data object Loading : State9_61()
    data class Success(val data: String) : State9_61()
    data class Error(val message: String) : State9_61()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}