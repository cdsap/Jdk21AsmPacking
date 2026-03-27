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

data class GenModel_2036_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2036_ {
    data class Load(val id: Long) : GenEvent_2036_()
    data class Update(val model: GenModel_2036_) : GenEvent_2036_()
    data class Delete(val id: Long) : GenEvent_2036_()
    data object Refresh : GenEvent_2036_()
    data class Search(val query: String) : GenEvent_2036_()
    data class Filter(val predicate: String) : GenEvent_2036_()
}

sealed class GenState_2036_ {
    data object Idle : GenState_2036_()
    data object Loading : GenState_2036_()
    data class Success(val items: List<GenModel_2036_>) : GenState_2036_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2036_()
    data class Partial(val items: List<GenModel_2036_>, val hasMore: Boolean) : GenState_2036_()
}

interface GenRepository_2036_ {
    suspend fun getAll(): List<GenModel_2036_>
    suspend fun getById(id: Long): GenModel_2036_?
    suspend fun save(model: GenModel_2036_): GenModel_2036_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2036_>
}

@Singleton
class GenRepositoryImpl_2036_ @Inject constructor() : GenRepository_2036_ {
    private val store = mutableMapOf<Long, GenModel_2036_>()
    override suspend fun getAll(): List<GenModel_2036_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2036_? = store[id]
    override suspend fun save(model: GenModel_2036_): GenModel_2036_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2036_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2036_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2036_ @Inject constructor(
    private val repository: GenRepositoryImpl_2036_
) : GenUseCase_2036_<Unit, List<GenModel_2036_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2036_> = repository.getAll()
}

class GenSaveUseCase_2036_ @Inject constructor(
    private val repository: GenRepositoryImpl_2036_
) : GenUseCase_2036_<GenModel_2036_, GenModel_2036_> {
    override suspend fun invoke(params: GenModel_2036_): GenModel_2036_ = repository.save(params)
}

class GenDeleteUseCase_2036_ @Inject constructor(
    private val repository: GenRepositoryImpl_2036_
) : GenUseCase_2036_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2036_ @Inject constructor(
    private val repository: GenRepositoryImpl_2036_
) : GenUseCase_2036_<String, List<GenModel_2036_>> {
    override suspend fun invoke(params: String): List<GenModel_2036_> = repository.search(params)
}

abstract class GenMapper_2036_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2036_ : GenMapper_2036_<GenModel_2036_, String>() {
    override fun map(input: GenModel_2036_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2036_ : GenMapper_2036_<String, GenModel_2036_>() {
    override fun map(input: String): GenModel_2036_ {
        val parts = input.split(":")
        return GenModel_2036_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2036_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2036_,
    private val saveUseCase: GenSaveUseCase_2036_,
    private val deleteUseCase: GenDeleteUseCase_2036_,
    private val searchUseCase: GenSearchUseCase_2036_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2036_>(GenState_2036_.Idle)
    val state: StateFlow<GenState_2036_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2036_) {
        when (event) {
            is GenEvent_2036_.Load -> loadAll()
            is GenEvent_2036_.Update -> save(event.model)
            is GenEvent_2036_.Delete -> delete(event.id)
            is GenEvent_2036_.Refresh -> loadAll()
            is GenEvent_2036_.Search -> search(event.query)
            is GenEvent_2036_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2036_.Loading; _state.value = GenState_2036_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2036_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2036_.Success(searchUseCase(query)) } }
}
