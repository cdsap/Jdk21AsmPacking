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

data class GenModel_1734_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1734_ {
    data class Load(val id: Long) : GenEvent_1734_()
    data class Update(val model: GenModel_1734_) : GenEvent_1734_()
    data class Delete(val id: Long) : GenEvent_1734_()
    data object Refresh : GenEvent_1734_()
    data class Search(val query: String) : GenEvent_1734_()
    data class Filter(val predicate: String) : GenEvent_1734_()
}

sealed class GenState_1734_ {
    data object Idle : GenState_1734_()
    data object Loading : GenState_1734_()
    data class Success(val items: List<GenModel_1734_>) : GenState_1734_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1734_()
    data class Partial(val items: List<GenModel_1734_>, val hasMore: Boolean) : GenState_1734_()
}

interface GenRepository_1734_ {
    suspend fun getAll(): List<GenModel_1734_>
    suspend fun getById(id: Long): GenModel_1734_?
    suspend fun save(model: GenModel_1734_): GenModel_1734_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1734_>
}

@Singleton
class GenRepositoryImpl_1734_ @Inject constructor() : GenRepository_1734_ {
    private val store = mutableMapOf<Long, GenModel_1734_>()
    override suspend fun getAll(): List<GenModel_1734_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1734_? = store[id]
    override suspend fun save(model: GenModel_1734_): GenModel_1734_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1734_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1734_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1734_ @Inject constructor(
    private val repository: GenRepositoryImpl_1734_
) : GenUseCase_1734_<Unit, List<GenModel_1734_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1734_> = repository.getAll()
}

class GenSaveUseCase_1734_ @Inject constructor(
    private val repository: GenRepositoryImpl_1734_
) : GenUseCase_1734_<GenModel_1734_, GenModel_1734_> {
    override suspend fun invoke(params: GenModel_1734_): GenModel_1734_ = repository.save(params)
}

class GenDeleteUseCase_1734_ @Inject constructor(
    private val repository: GenRepositoryImpl_1734_
) : GenUseCase_1734_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1734_ @Inject constructor(
    private val repository: GenRepositoryImpl_1734_
) : GenUseCase_1734_<String, List<GenModel_1734_>> {
    override suspend fun invoke(params: String): List<GenModel_1734_> = repository.search(params)
}

abstract class GenMapper_1734_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1734_ : GenMapper_1734_<GenModel_1734_, String>() {
    override fun map(input: GenModel_1734_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1734_ : GenMapper_1734_<String, GenModel_1734_>() {
    override fun map(input: String): GenModel_1734_ {
        val parts = input.split(":")
        return GenModel_1734_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1734_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1734_,
    private val saveUseCase: GenSaveUseCase_1734_,
    private val deleteUseCase: GenDeleteUseCase_1734_,
    private val searchUseCase: GenSearchUseCase_1734_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1734_>(GenState_1734_.Idle)
    val state: StateFlow<GenState_1734_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1734_) {
        when (event) {
            is GenEvent_1734_.Load -> loadAll()
            is GenEvent_1734_.Update -> save(event.model)
            is GenEvent_1734_.Delete -> delete(event.id)
            is GenEvent_1734_.Refresh -> loadAll()
            is GenEvent_1734_.Search -> search(event.query)
            is GenEvent_1734_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1734_.Loading; _state.value = GenState_1734_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1734_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1734_.Success(searchUseCase(query)) } }
}
