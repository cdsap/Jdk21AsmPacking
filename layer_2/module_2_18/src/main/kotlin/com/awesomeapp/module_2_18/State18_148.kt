package com.awesomeapp.module_2_18

sealed class State18_148 {
    data object Loading : State18_148()
    data class Success(val data: String) : State18_148()
    data class Error(val message: String) : State18_148()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}