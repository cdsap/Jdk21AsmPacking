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

data class GenModel_1184_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1184_ {
    data class Load(val id: Long) : GenEvent_1184_()
    data class Update(val model: GenModel_1184_) : GenEvent_1184_()
    data class Delete(val id: Long) : GenEvent_1184_()
    data object Refresh : GenEvent_1184_()
    data class Search(val query: String) : GenEvent_1184_()
    data class Filter(val predicate: String) : GenEvent_1184_()
}

sealed class GenState_1184_ {
    data object Idle : GenState_1184_()
    data object Loading : GenState_1184_()
    data class Success(val items: List<GenModel_1184_>) : GenState_1184_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1184_()
    data class Partial(val items: List<GenModel_1184_>, val hasMore: Boolean) : GenState_1184_()
}

interface GenRepository_1184_ {
    suspend fun getAll(): List<GenModel_1184_>
    suspend fun getById(id: Long): GenModel_1184_?
    suspend fun save(model: GenModel_1184_): GenModel_1184_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1184_>
}

@Singleton
class GenRepositoryImpl_1184_ @Inject constructor() : GenRepository_1184_ {
    private val store = mutableMapOf<Long, GenModel_1184_>()
    override suspend fun getAll(): List<GenModel_1184_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1184_? = store[id]
    override suspend fun save(model: GenModel_1184_): GenModel_1184_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1184_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1184_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1184_ @Inject constructor(
    private val repository: GenRepositoryImpl_1184_
) : GenUseCase_1184_<Unit, List<GenModel_1184_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1184_> = repository.getAll()
}

class GenSaveUseCase_1184_ @Inject constructor(
    private val repository: GenRepositoryImpl_1184_
) : GenUseCase_1184_<GenModel_1184_, GenModel_1184_> {
    override suspend fun invoke(params: GenModel_1184_): GenModel_1184_ = repository.save(params)
}

class GenDeleteUseCase_1184_ @Inject constructor(
    private val repository: GenRepositoryImpl_1184_
) : GenUseCase_1184_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1184_ @Inject constructor(
    private val repository: GenRepositoryImpl_1184_
) : GenUseCase_1184_<String, List<GenModel_1184_>> {
    override suspend fun invoke(params: String): List<GenModel_1184_> = repository.search(params)
}

abstract class GenMapper_1184_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1184_ : GenMapper_1184_<GenModel_1184_, String>() {
    override fun map(input: GenModel_1184_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1184_ : GenMapper_1184_<String, GenModel_1184_>() {
    override fun map(input: String): GenModel_1184_ {
        val parts = input.split(":")
        return GenModel_1184_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1184_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1184_,
    private val saveUseCase: GenSaveUseCase_1184_,
    private val deleteUseCase: GenDeleteUseCase_1184_,
    private val searchUseCase: GenSearchUseCase_1184_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1184_>(GenState_1184_.Idle)
    val state: StateFlow<GenState_1184_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1184_) {
        when (event) {
            is GenEvent_1184_.Load -> loadAll()
            is GenEvent_1184_.Update -> save(event.model)
            is GenEvent_1184_.Delete -> delete(event.id)
            is GenEvent_1184_.Refresh -> loadAll()
            is GenEvent_1184_.Search -> search(event.query)
            is GenEvent_1184_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1184_.Loading; _state.value = GenState_1184_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1184_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1184_.Success(searchUseCase(query)) } }
}
