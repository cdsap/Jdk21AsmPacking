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

data class GenModel_1712_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1712_ {
    data class Load(val id: Long) : GenEvent_1712_()
    data class Update(val model: GenModel_1712_) : GenEvent_1712_()
    data class Delete(val id: Long) : GenEvent_1712_()
    data object Refresh : GenEvent_1712_()
    data class Search(val query: String) : GenEvent_1712_()
    data class Filter(val predicate: String) : GenEvent_1712_()
}

sealed class GenState_1712_ {
    data object Idle : GenState_1712_()
    data object Loading : GenState_1712_()
    data class Success(val items: List<GenModel_1712_>) : GenState_1712_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1712_()
    data class Partial(val items: List<GenModel_1712_>, val hasMore: Boolean) : GenState_1712_()
}

interface GenRepository_1712_ {
    suspend fun getAll(): List<GenModel_1712_>
    suspend fun getById(id: Long): GenModel_1712_?
    suspend fun save(model: GenModel_1712_): GenModel_1712_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1712_>
}

@Singleton
class GenRepositoryImpl_1712_ @Inject constructor() : GenRepository_1712_ {
    private val store = mutableMapOf<Long, GenModel_1712_>()
    override suspend fun getAll(): List<GenModel_1712_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1712_? = store[id]
    override suspend fun save(model: GenModel_1712_): GenModel_1712_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1712_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1712_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1712_ @Inject constructor(
    private val repository: GenRepositoryImpl_1712_
) : GenUseCase_1712_<Unit, List<GenModel_1712_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1712_> = repository.getAll()
}

class GenSaveUseCase_1712_ @Inject constructor(
    private val repository: GenRepositoryImpl_1712_
) : GenUseCase_1712_<GenModel_1712_, GenModel_1712_> {
    override suspend fun invoke(params: GenModel_1712_): GenModel_1712_ = repository.save(params)
}

class GenDeleteUseCase_1712_ @Inject constructor(
    private val repository: GenRepositoryImpl_1712_
) : GenUseCase_1712_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1712_ @Inject constructor(
    private val repository: GenRepositoryImpl_1712_
) : GenUseCase_1712_<String, List<GenModel_1712_>> {
    override suspend fun invoke(params: String): List<GenModel_1712_> = repository.search(params)
}

abstract class GenMapper_1712_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1712_ : GenMapper_1712_<GenModel_1712_, String>() {
    override fun map(input: GenModel_1712_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1712_ : GenMapper_1712_<String, GenModel_1712_>() {
    override fun map(input: String): GenModel_1712_ {
        val parts = input.split(":")
        return GenModel_1712_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1712_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1712_,
    private val saveUseCase: GenSaveUseCase_1712_,
    private val deleteUseCase: GenDeleteUseCase_1712_,
    private val searchUseCase: GenSearchUseCase_1712_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1712_>(GenState_1712_.Idle)
    val state: StateFlow<GenState_1712_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1712_) {
        when (event) {
            is GenEvent_1712_.Load -> loadAll()
            is GenEvent_1712_.Update -> save(event.model)
            is GenEvent_1712_.Delete -> delete(event.id)
            is GenEvent_1712_.Refresh -> loadAll()
            is GenEvent_1712_.Search -> search(event.query)
            is GenEvent_1712_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1712_.Loading; _state.value = GenState_1712_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1712_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1712_.Success(searchUseCase(query)) } }
}
