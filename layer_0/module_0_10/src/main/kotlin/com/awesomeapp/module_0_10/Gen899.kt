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

data class GenModel_899_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_899_ {
    data class Load(val id: Long) : GenEvent_899_()
    data class Update(val model: GenModel_899_) : GenEvent_899_()
    data class Delete(val id: Long) : GenEvent_899_()
    data object Refresh : GenEvent_899_()
    data class Search(val query: String) : GenEvent_899_()
    data class Filter(val predicate: String) : GenEvent_899_()
}

sealed class GenState_899_ {
    data object Idle : GenState_899_()
    data object Loading : GenState_899_()
    data class Success(val items: List<GenModel_899_>) : GenState_899_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_899_()
    data class Partial(val items: List<GenModel_899_>, val hasMore: Boolean) : GenState_899_()
}

interface GenRepository_899_ {
    suspend fun getAll(): List<GenModel_899_>
    suspend fun getById(id: Long): GenModel_899_?
    suspend fun save(model: GenModel_899_): GenModel_899_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_899_>
}

@Singleton
class GenRepositoryImpl_899_ @Inject constructor() : GenRepository_899_ {
    private val store = mutableMapOf<Long, GenModel_899_>()
    override suspend fun getAll(): List<GenModel_899_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_899_? = store[id]
    override suspend fun save(model: GenModel_899_): GenModel_899_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_899_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_899_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_899_ @Inject constructor(
    private val repository: GenRepositoryImpl_899_
) : GenUseCase_899_<Unit, List<GenModel_899_>> {
    override suspend fun invoke(params: Unit): List<GenModel_899_> = repository.getAll()
}

class GenSaveUseCase_899_ @Inject constructor(
    private val repository: GenRepositoryImpl_899_
) : GenUseCase_899_<GenModel_899_, GenModel_899_> {
    override suspend fun invoke(params: GenModel_899_): GenModel_899_ = repository.save(params)
}

class GenDeleteUseCase_899_ @Inject constructor(
    private val repository: GenRepositoryImpl_899_
) : GenUseCase_899_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_899_ @Inject constructor(
    private val repository: GenRepositoryImpl_899_
) : GenUseCase_899_<String, List<GenModel_899_>> {
    override suspend fun invoke(params: String): List<GenModel_899_> = repository.search(params)
}

abstract class GenMapper_899_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_899_ : GenMapper_899_<GenModel_899_, String>() {
    override fun map(input: GenModel_899_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_899_ : GenMapper_899_<String, GenModel_899_>() {
    override fun map(input: String): GenModel_899_ {
        val parts = input.split(":")
        return GenModel_899_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_899_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_899_,
    private val saveUseCase: GenSaveUseCase_899_,
    private val deleteUseCase: GenDeleteUseCase_899_,
    private val searchUseCase: GenSearchUseCase_899_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_899_>(GenState_899_.Idle)
    val state: StateFlow<GenState_899_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_899_) {
        when (event) {
            is GenEvent_899_.Load -> loadAll()
            is GenEvent_899_.Update -> save(event.model)
            is GenEvent_899_.Delete -> delete(event.id)
            is GenEvent_899_.Refresh -> loadAll()
            is GenEvent_899_.Search -> search(event.query)
            is GenEvent_899_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_899_.Loading; _state.value = GenState_899_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_899_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_899_.Success(searchUseCase(query)) } }
}
