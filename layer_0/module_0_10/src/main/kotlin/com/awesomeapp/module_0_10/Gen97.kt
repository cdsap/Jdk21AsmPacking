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

data class GenModel_97_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_97_ {
    data class Load(val id: Long) : GenEvent_97_()
    data class Update(val model: GenModel_97_) : GenEvent_97_()
    data class Delete(val id: Long) : GenEvent_97_()
    data object Refresh : GenEvent_97_()
    data class Search(val query: String) : GenEvent_97_()
    data class Filter(val predicate: String) : GenEvent_97_()
}

sealed class GenState_97_ {
    data object Idle : GenState_97_()
    data object Loading : GenState_97_()
    data class Success(val items: List<GenModel_97_>) : GenState_97_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_97_()
    data class Partial(val items: List<GenModel_97_>, val hasMore: Boolean) : GenState_97_()
}

interface GenRepository_97_ {
    suspend fun getAll(): List<GenModel_97_>
    suspend fun getById(id: Long): GenModel_97_?
    suspend fun save(model: GenModel_97_): GenModel_97_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_97_>
}

@Singleton
class GenRepositoryImpl_97_ @Inject constructor() : GenRepository_97_ {
    private val store = mutableMapOf<Long, GenModel_97_>()
    override suspend fun getAll(): List<GenModel_97_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_97_? = store[id]
    override suspend fun save(model: GenModel_97_): GenModel_97_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_97_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_97_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_97_ @Inject constructor(
    private val repository: GenRepositoryImpl_97_
) : GenUseCase_97_<Unit, List<GenModel_97_>> {
    override suspend fun invoke(params: Unit): List<GenModel_97_> = repository.getAll()
}

class GenSaveUseCase_97_ @Inject constructor(
    private val repository: GenRepositoryImpl_97_
) : GenUseCase_97_<GenModel_97_, GenModel_97_> {
    override suspend fun invoke(params: GenModel_97_): GenModel_97_ = repository.save(params)
}

class GenDeleteUseCase_97_ @Inject constructor(
    private val repository: GenRepositoryImpl_97_
) : GenUseCase_97_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_97_ @Inject constructor(
    private val repository: GenRepositoryImpl_97_
) : GenUseCase_97_<String, List<GenModel_97_>> {
    override suspend fun invoke(params: String): List<GenModel_97_> = repository.search(params)
}

abstract class GenMapper_97_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_97_ : GenMapper_97_<GenModel_97_, String>() {
    override fun map(input: GenModel_97_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_97_ : GenMapper_97_<String, GenModel_97_>() {
    override fun map(input: String): GenModel_97_ {
        val parts = input.split(":")
        return GenModel_97_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_97_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_97_,
    private val saveUseCase: GenSaveUseCase_97_,
    private val deleteUseCase: GenDeleteUseCase_97_,
    private val searchUseCase: GenSearchUseCase_97_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_97_>(GenState_97_.Idle)
    val state: StateFlow<GenState_97_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_97_) {
        when (event) {
            is GenEvent_97_.Load -> loadAll()
            is GenEvent_97_.Update -> save(event.model)
            is GenEvent_97_.Delete -> delete(event.id)
            is GenEvent_97_.Refresh -> loadAll()
            is GenEvent_97_.Search -> search(event.query)
            is GenEvent_97_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_97_.Loading; _state.value = GenState_97_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_97_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_97_.Success(searchUseCase(query)) } }
}
