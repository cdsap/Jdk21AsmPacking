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

data class GenModel_603_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_603_ {
    data class Load(val id: Long) : GenEvent_603_()
    data class Update(val model: GenModel_603_) : GenEvent_603_()
    data class Delete(val id: Long) : GenEvent_603_()
    data object Refresh : GenEvent_603_()
    data class Search(val query: String) : GenEvent_603_()
    data class Filter(val predicate: String) : GenEvent_603_()
}

sealed class GenState_603_ {
    data object Idle : GenState_603_()
    data object Loading : GenState_603_()
    data class Success(val items: List<GenModel_603_>) : GenState_603_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_603_()
    data class Partial(val items: List<GenModel_603_>, val hasMore: Boolean) : GenState_603_()
}

interface GenRepository_603_ {
    suspend fun getAll(): List<GenModel_603_>
    suspend fun getById(id: Long): GenModel_603_?
    suspend fun save(model: GenModel_603_): GenModel_603_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_603_>
}

@Singleton
class GenRepositoryImpl_603_ @Inject constructor() : GenRepository_603_ {
    private val store = mutableMapOf<Long, GenModel_603_>()
    override suspend fun getAll(): List<GenModel_603_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_603_? = store[id]
    override suspend fun save(model: GenModel_603_): GenModel_603_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_603_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_603_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_603_ @Inject constructor(
    private val repository: GenRepositoryImpl_603_
) : GenUseCase_603_<Unit, List<GenModel_603_>> {
    override suspend fun invoke(params: Unit): List<GenModel_603_> = repository.getAll()
}

class GenSaveUseCase_603_ @Inject constructor(
    private val repository: GenRepositoryImpl_603_
) : GenUseCase_603_<GenModel_603_, GenModel_603_> {
    override suspend fun invoke(params: GenModel_603_): GenModel_603_ = repository.save(params)
}

class GenDeleteUseCase_603_ @Inject constructor(
    private val repository: GenRepositoryImpl_603_
) : GenUseCase_603_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_603_ @Inject constructor(
    private val repository: GenRepositoryImpl_603_
) : GenUseCase_603_<String, List<GenModel_603_>> {
    override suspend fun invoke(params: String): List<GenModel_603_> = repository.search(params)
}

abstract class GenMapper_603_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_603_ : GenMapper_603_<GenModel_603_, String>() {
    override fun map(input: GenModel_603_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_603_ : GenMapper_603_<String, GenModel_603_>() {
    override fun map(input: String): GenModel_603_ {
        val parts = input.split(":")
        return GenModel_603_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_603_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_603_,
    private val saveUseCase: GenSaveUseCase_603_,
    private val deleteUseCase: GenDeleteUseCase_603_,
    private val searchUseCase: GenSearchUseCase_603_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_603_>(GenState_603_.Idle)
    val state: StateFlow<GenState_603_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_603_) {
        when (event) {
            is GenEvent_603_.Load -> loadAll()
            is GenEvent_603_.Update -> save(event.model)
            is GenEvent_603_.Delete -> delete(event.id)
            is GenEvent_603_.Refresh -> loadAll()
            is GenEvent_603_.Search -> search(event.query)
            is GenEvent_603_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_603_.Loading; _state.value = GenState_603_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_603_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_603_.Success(searchUseCase(query)) } }
}
