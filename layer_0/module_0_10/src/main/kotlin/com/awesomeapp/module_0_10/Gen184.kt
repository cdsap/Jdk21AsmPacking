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

data class GenModel_184_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_184_ {
    data class Load(val id: Long) : GenEvent_184_()
    data class Update(val model: GenModel_184_) : GenEvent_184_()
    data class Delete(val id: Long) : GenEvent_184_()
    data object Refresh : GenEvent_184_()
    data class Search(val query: String) : GenEvent_184_()
    data class Filter(val predicate: String) : GenEvent_184_()
}

sealed class GenState_184_ {
    data object Idle : GenState_184_()
    data object Loading : GenState_184_()
    data class Success(val items: List<GenModel_184_>) : GenState_184_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_184_()
    data class Partial(val items: List<GenModel_184_>, val hasMore: Boolean) : GenState_184_()
}

interface GenRepository_184_ {
    suspend fun getAll(): List<GenModel_184_>
    suspend fun getById(id: Long): GenModel_184_?
    suspend fun save(model: GenModel_184_): GenModel_184_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_184_>
}

@Singleton
class GenRepositoryImpl_184_ @Inject constructor() : GenRepository_184_ {
    private val store = mutableMapOf<Long, GenModel_184_>()
    override suspend fun getAll(): List<GenModel_184_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_184_? = store[id]
    override suspend fun save(model: GenModel_184_): GenModel_184_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_184_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_184_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_184_ @Inject constructor(
    private val repository: GenRepositoryImpl_184_
) : GenUseCase_184_<Unit, List<GenModel_184_>> {
    override suspend fun invoke(params: Unit): List<GenModel_184_> = repository.getAll()
}

class GenSaveUseCase_184_ @Inject constructor(
    private val repository: GenRepositoryImpl_184_
) : GenUseCase_184_<GenModel_184_, GenModel_184_> {
    override suspend fun invoke(params: GenModel_184_): GenModel_184_ = repository.save(params)
}

class GenDeleteUseCase_184_ @Inject constructor(
    private val repository: GenRepositoryImpl_184_
) : GenUseCase_184_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_184_ @Inject constructor(
    private val repository: GenRepositoryImpl_184_
) : GenUseCase_184_<String, List<GenModel_184_>> {
    override suspend fun invoke(params: String): List<GenModel_184_> = repository.search(params)
}

abstract class GenMapper_184_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_184_ : GenMapper_184_<GenModel_184_, String>() {
    override fun map(input: GenModel_184_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_184_ : GenMapper_184_<String, GenModel_184_>() {
    override fun map(input: String): GenModel_184_ {
        val parts = input.split(":")
        return GenModel_184_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_184_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_184_,
    private val saveUseCase: GenSaveUseCase_184_,
    private val deleteUseCase: GenDeleteUseCase_184_,
    private val searchUseCase: GenSearchUseCase_184_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_184_>(GenState_184_.Idle)
    val state: StateFlow<GenState_184_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_184_) {
        when (event) {
            is GenEvent_184_.Load -> loadAll()
            is GenEvent_184_.Update -> save(event.model)
            is GenEvent_184_.Delete -> delete(event.id)
            is GenEvent_184_.Refresh -> loadAll()
            is GenEvent_184_.Search -> search(event.query)
            is GenEvent_184_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_184_.Loading; _state.value = GenState_184_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_184_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_184_.Success(searchUseCase(query)) } }
}
