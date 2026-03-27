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

data class GenModel_47_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_47_ {
    data class Load(val id: Long) : GenEvent_47_()
    data class Update(val model: GenModel_47_) : GenEvent_47_()
    data class Delete(val id: Long) : GenEvent_47_()
    data object Refresh : GenEvent_47_()
    data class Search(val query: String) : GenEvent_47_()
    data class Filter(val predicate: String) : GenEvent_47_()
}

sealed class GenState_47_ {
    data object Idle : GenState_47_()
    data object Loading : GenState_47_()
    data class Success(val items: List<GenModel_47_>) : GenState_47_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_47_()
    data class Partial(val items: List<GenModel_47_>, val hasMore: Boolean) : GenState_47_()
}

interface GenRepository_47_ {
    suspend fun getAll(): List<GenModel_47_>
    suspend fun getById(id: Long): GenModel_47_?
    suspend fun save(model: GenModel_47_): GenModel_47_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_47_>
}

@Singleton
class GenRepositoryImpl_47_ @Inject constructor() : GenRepository_47_ {
    private val store = mutableMapOf<Long, GenModel_47_>()
    override suspend fun getAll(): List<GenModel_47_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_47_? = store[id]
    override suspend fun save(model: GenModel_47_): GenModel_47_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_47_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_47_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_47_ @Inject constructor(
    private val repository: GenRepositoryImpl_47_
) : GenUseCase_47_<Unit, List<GenModel_47_>> {
    override suspend fun invoke(params: Unit): List<GenModel_47_> = repository.getAll()
}

class GenSaveUseCase_47_ @Inject constructor(
    private val repository: GenRepositoryImpl_47_
) : GenUseCase_47_<GenModel_47_, GenModel_47_> {
    override suspend fun invoke(params: GenModel_47_): GenModel_47_ = repository.save(params)
}

class GenDeleteUseCase_47_ @Inject constructor(
    private val repository: GenRepositoryImpl_47_
) : GenUseCase_47_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_47_ @Inject constructor(
    private val repository: GenRepositoryImpl_47_
) : GenUseCase_47_<String, List<GenModel_47_>> {
    override suspend fun invoke(params: String): List<GenModel_47_> = repository.search(params)
}

abstract class GenMapper_47_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_47_ : GenMapper_47_<GenModel_47_, String>() {
    override fun map(input: GenModel_47_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_47_ : GenMapper_47_<String, GenModel_47_>() {
    override fun map(input: String): GenModel_47_ {
        val parts = input.split(":")
        return GenModel_47_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_47_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_47_,
    private val saveUseCase: GenSaveUseCase_47_,
    private val deleteUseCase: GenDeleteUseCase_47_,
    private val searchUseCase: GenSearchUseCase_47_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_47_>(GenState_47_.Idle)
    val state: StateFlow<GenState_47_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_47_) {
        when (event) {
            is GenEvent_47_.Load -> loadAll()
            is GenEvent_47_.Update -> save(event.model)
            is GenEvent_47_.Delete -> delete(event.id)
            is GenEvent_47_.Refresh -> loadAll()
            is GenEvent_47_.Search -> search(event.query)
            is GenEvent_47_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_47_.Loading; _state.value = GenState_47_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_47_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_47_.Success(searchUseCase(query)) } }
}
