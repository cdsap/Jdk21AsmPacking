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

data class GenModel_3283_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3283_ {
    data class Load(val id: Long) : GenEvent_3283_()
    data class Update(val model: GenModel_3283_) : GenEvent_3283_()
    data class Delete(val id: Long) : GenEvent_3283_()
    data object Refresh : GenEvent_3283_()
    data class Search(val query: String) : GenEvent_3283_()
    data class Filter(val predicate: String) : GenEvent_3283_()
}

sealed class GenState_3283_ {
    data object Idle : GenState_3283_()
    data object Loading : GenState_3283_()
    data class Success(val items: List<GenModel_3283_>) : GenState_3283_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3283_()
    data class Partial(val items: List<GenModel_3283_>, val hasMore: Boolean) : GenState_3283_()
}

interface GenRepository_3283_ {
    suspend fun getAll(): List<GenModel_3283_>
    suspend fun getById(id: Long): GenModel_3283_?
    suspend fun save(model: GenModel_3283_): GenModel_3283_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3283_>
}

@Singleton
class GenRepositoryImpl_3283_ @Inject constructor() : GenRepository_3283_ {
    private val store = mutableMapOf<Long, GenModel_3283_>()
    override suspend fun getAll(): List<GenModel_3283_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3283_? = store[id]
    override suspend fun save(model: GenModel_3283_): GenModel_3283_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3283_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3283_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3283_ @Inject constructor(
    private val repository: GenRepositoryImpl_3283_
) : GenUseCase_3283_<Unit, List<GenModel_3283_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3283_> = repository.getAll()
}

class GenSaveUseCase_3283_ @Inject constructor(
    private val repository: GenRepositoryImpl_3283_
) : GenUseCase_3283_<GenModel_3283_, GenModel_3283_> {
    override suspend fun invoke(params: GenModel_3283_): GenModel_3283_ = repository.save(params)
}

class GenDeleteUseCase_3283_ @Inject constructor(
    private val repository: GenRepositoryImpl_3283_
) : GenUseCase_3283_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3283_ @Inject constructor(
    private val repository: GenRepositoryImpl_3283_
) : GenUseCase_3283_<String, List<GenModel_3283_>> {
    override suspend fun invoke(params: String): List<GenModel_3283_> = repository.search(params)
}

abstract class GenMapper_3283_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3283_ : GenMapper_3283_<GenModel_3283_, String>() {
    override fun map(input: GenModel_3283_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3283_ : GenMapper_3283_<String, GenModel_3283_>() {
    override fun map(input: String): GenModel_3283_ {
        val parts = input.split(":")
        return GenModel_3283_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3283_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3283_,
    private val saveUseCase: GenSaveUseCase_3283_,
    private val deleteUseCase: GenDeleteUseCase_3283_,
    private val searchUseCase: GenSearchUseCase_3283_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3283_>(GenState_3283_.Idle)
    val state: StateFlow<GenState_3283_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3283_) {
        when (event) {
            is GenEvent_3283_.Load -> loadAll()
            is GenEvent_3283_.Update -> save(event.model)
            is GenEvent_3283_.Delete -> delete(event.id)
            is GenEvent_3283_.Refresh -> loadAll()
            is GenEvent_3283_.Search -> search(event.query)
            is GenEvent_3283_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3283_.Loading; _state.value = GenState_3283_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3283_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3283_.Success(searchUseCase(query)) } }
}
