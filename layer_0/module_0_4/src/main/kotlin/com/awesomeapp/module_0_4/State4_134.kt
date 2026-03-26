package com.awesomeapp.module_0_4

sealed class State4_134 {
    data object Loading : State4_134()
    data class Success(val data: String) : State4_134()
    data class Error(val message: String) : State4_134()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}