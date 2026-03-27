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

data class GenModel_1150_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1150_ {
    data class Load(val id: Long) : GenEvent_1150_()
    data class Update(val model: GenModel_1150_) : GenEvent_1150_()
    data class Delete(val id: Long) : GenEvent_1150_()
    data object Refresh : GenEvent_1150_()
    data class Search(val query: String) : GenEvent_1150_()
    data class Filter(val predicate: String) : GenEvent_1150_()
}

sealed class GenState_1150_ {
    data object Idle : GenState_1150_()
    data object Loading : GenState_1150_()
    data class Success(val items: List<GenModel_1150_>) : GenState_1150_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1150_()
    data class Partial(val items: List<GenModel_1150_>, val hasMore: Boolean) : GenState_1150_()
}

interface GenRepository_1150_ {
    suspend fun getAll(): List<GenModel_1150_>
    suspend fun getById(id: Long): GenModel_1150_?
    suspend fun save(model: GenModel_1150_): GenModel_1150_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1150_>
}

@Singleton
class GenRepositoryImpl_1150_ @Inject constructor() : GenRepository_1150_ {
    private val store = mutableMapOf<Long, GenModel_1150_>()
    override suspend fun getAll(): List<GenModel_1150_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1150_? = store[id]
    override suspend fun save(model: GenModel_1150_): GenModel_1150_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1150_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1150_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1150_ @Inject constructor(
    private val repository: GenRepositoryImpl_1150_
) : GenUseCase_1150_<Unit, List<GenModel_1150_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1150_> = repository.getAll()
}

class GenSaveUseCase_1150_ @Inject constructor(
    private val repository: GenRepositoryImpl_1150_
) : GenUseCase_1150_<GenModel_1150_, GenModel_1150_> {
    override suspend fun invoke(params: GenModel_1150_): GenModel_1150_ = repository.save(params)
}

class GenDeleteUseCase_1150_ @Inject constructor(
    private val repository: GenRepositoryImpl_1150_
) : GenUseCase_1150_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1150_ @Inject constructor(
    private val repository: GenRepositoryImpl_1150_
) : GenUseCase_1150_<String, List<GenModel_1150_>> {
    override suspend fun invoke(params: String): List<GenModel_1150_> = repository.search(params)
}

abstract class GenMapper_1150_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1150_ : GenMapper_1150_<GenModel_1150_, String>() {
    override fun map(input: GenModel_1150_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1150_ : GenMapper_1150_<String, GenModel_1150_>() {
    override fun map(input: String): GenModel_1150_ {
        val parts = input.split(":")
        return GenModel_1150_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1150_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1150_,
    private val saveUseCase: GenSaveUseCase_1150_,
    private val deleteUseCase: GenDeleteUseCase_1150_,
    private val searchUseCase: GenSearchUseCase_1150_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1150_>(GenState_1150_.Idle)
    val state: StateFlow<GenState_1150_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1150_) {
        when (event) {
            is GenEvent_1150_.Load -> loadAll()
            is GenEvent_1150_.Update -> save(event.model)
            is GenEvent_1150_.Delete -> delete(event.id)
            is GenEvent_1150_.Refresh -> loadAll()
            is GenEvent_1150_.Search -> search(event.query)
            is GenEvent_1150_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1150_.Loading; _state.value = GenState_1150_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1150_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1150_.Success(searchUseCase(query)) } }
}
