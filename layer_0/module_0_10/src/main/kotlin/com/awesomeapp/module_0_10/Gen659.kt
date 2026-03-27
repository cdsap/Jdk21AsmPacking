package com.awesomeapp.module_0_10

import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GenModel_659_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_659_ {
    data class Load(val id: Long) : GenEvent_659_()
    data class Update(val model: GenModel_659_) : GenEvent_659_()
    data class Delete(val id: Long) : GenEvent_659_()
    data object Refresh : GenEvent_659_()
    data class Search(val query: String) : GenEvent_659_()
    data class Filter(val predicate: String) : GenEvent_659_()
}

sealed class GenState_659_ {
    data object Idle : GenState_659_()
    data object Loading : GenState_659_()
    data class Success(val items: List<GenModel_659_>) : GenState_659_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_659_()
    data class Partial(val items: List<GenModel_659_>, val hasMore: Boolean) : GenState_659_()
}

interface GenRepository_659_ {
    suspend fun getAll(): List<GenModel_659_>
    suspend fun getById(id: Long): GenModel_659_?
    suspend fun save(model: GenModel_659_): GenModel_659_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_659_>
}

@Singleton
class GenRepositoryImpl_659_ @Inject constructor() : GenRepository_659_ {
    private val store = mutableMapOf<Long, GenModel_659_>()
    override suspend fun getAll(): List<GenModel_659_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_659_? = store[id]
    override suspend fun save(model: GenModel_659_): GenModel_659_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_659_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_659_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_659_ @Inject constructor(
    private val repository: GenRepositoryImpl_659_
) : GenUseCase_659_<Unit, List<GenModel_659_>> {
    override suspend fun invoke(params: Unit): List<GenModel_659_> = repository.getAll()
}

class GenSaveUseCase_659_ @Inject constructor(
    private val repository: GenRepositoryImpl_659_
) : GenUseCase_659_<GenModel_659_, GenModel_659_> {
    override suspend fun invoke(params: GenModel_659_): GenModel_659_ = repository.save(params)
}

class GenDeleteUseCase_659_ @Inject constructor(
    private val repository: GenRepositoryImpl_659_
) : GenUseCase_659_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_659_ @Inject constructor(
    private val repository: GenRepositoryImpl_659_
) : GenUseCase_659_<String, List<GenModel_659_>> {
    override suspend fun invoke(params: String): List<GenModel_659_> = repository.search(params)
}

abstract class GenMapper_659_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_659_ : GenMapper_659_<GenModel_659_, String>() {
    override fun map(input: GenModel_659_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_659_ : GenMapper_659_<String, GenModel_659_>() {
    override fun map(input: String): GenModel_659_ {
        val parts = input.split(":")
        return GenModel_659_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_659_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_659_,
    private val saveUseCase: GenSaveUseCase_659_,
    private val deleteUseCase: GenDeleteUseCase_659_,
    private val searchUseCase: GenSearchUseCase_659_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_659_>(GenState_659_.Idle)
    val state: StateFlow<GenState_659_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_659_) {
        when (event) {
            is GenEvent_659_.Load -> loadAll()
            is GenEvent_659_.Update -> save(event.model)
            is GenEvent_659_.Delete -> delete(event.id)
            is GenEvent_659_.Refresh -> loadAll()
            is GenEvent_659_.Search -> search(event.query)
            is GenEvent_659_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_659_.Loading; _state.value = GenState_659_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_659_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_659_.Success(searchUseCase(query)) } }
}
