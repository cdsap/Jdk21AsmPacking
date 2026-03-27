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

data class GenModel_90_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_90_ {
    data class Load(val id: Long) : GenEvent_90_()
    data class Update(val model: GenModel_90_) : GenEvent_90_()
    data class Delete(val id: Long) : GenEvent_90_()
    data object Refresh : GenEvent_90_()
    data class Search(val query: String) : GenEvent_90_()
    data class Filter(val predicate: String) : GenEvent_90_()
}

sealed class GenState_90_ {
    data object Idle : GenState_90_()
    data object Loading : GenState_90_()
    data class Success(val items: List<GenModel_90_>) : GenState_90_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_90_()
    data class Partial(val items: List<GenModel_90_>, val hasMore: Boolean) : GenState_90_()
}

interface GenRepository_90_ {
    suspend fun getAll(): List<GenModel_90_>
    suspend fun getById(id: Long): GenModel_90_?
    suspend fun save(model: GenModel_90_): GenModel_90_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_90_>
}

@Singleton
class GenRepositoryImpl_90_ @Inject constructor() : GenRepository_90_ {
    private val store = mutableMapOf<Long, GenModel_90_>()
    override suspend fun getAll(): List<GenModel_90_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_90_? = store[id]
    override suspend fun save(model: GenModel_90_): GenModel_90_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_90_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_90_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_90_ @Inject constructor(
    private val repository: GenRepositoryImpl_90_
) : GenUseCase_90_<Unit, List<GenModel_90_>> {
    override suspend fun invoke(params: Unit): List<GenModel_90_> = repository.getAll()
}

class GenSaveUseCase_90_ @Inject constructor(
    private val repository: GenRepositoryImpl_90_
) : GenUseCase_90_<GenModel_90_, GenModel_90_> {
    override suspend fun invoke(params: GenModel_90_): GenModel_90_ = repository.save(params)
}

class GenDeleteUseCase_90_ @Inject constructor(
    private val repository: GenRepositoryImpl_90_
) : GenUseCase_90_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_90_ @Inject constructor(
    private val repository: GenRepositoryImpl_90_
) : GenUseCase_90_<String, List<GenModel_90_>> {
    override suspend fun invoke(params: String): List<GenModel_90_> = repository.search(params)
}

abstract class GenMapper_90_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_90_ : GenMapper_90_<GenModel_90_, String>() {
    override fun map(input: GenModel_90_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_90_ : GenMapper_90_<String, GenModel_90_>() {
    override fun map(input: String): GenModel_90_ {
        val parts = input.split(":")
        return GenModel_90_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_90_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_90_,
    private val saveUseCase: GenSaveUseCase_90_,
    private val deleteUseCase: GenDeleteUseCase_90_,
    private val searchUseCase: GenSearchUseCase_90_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_90_>(GenState_90_.Idle)
    val state: StateFlow<GenState_90_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_90_) {
        when (event) {
            is GenEvent_90_.Load -> loadAll()
            is GenEvent_90_.Update -> save(event.model)
            is GenEvent_90_.Delete -> delete(event.id)
            is GenEvent_90_.Refresh -> loadAll()
            is GenEvent_90_.Search -> search(event.query)
            is GenEvent_90_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_90_.Loading; _state.value = GenState_90_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_90_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_90_.Success(searchUseCase(query)) } }
}
