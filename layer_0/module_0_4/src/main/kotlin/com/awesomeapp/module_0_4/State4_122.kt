package com.awesomeapp.module_0_4

sealed class State4_122 {
    data object Loading : State4_122()
    data class Success(val data: String) : State4_122()
    data class Error(val message: String) : State4_122()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}