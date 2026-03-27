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

data class GenModel_105_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_105_ {
    data class Load(val id: Long) : GenEvent_105_()
    data class Update(val model: GenModel_105_) : GenEvent_105_()
    data class Delete(val id: Long) : GenEvent_105_()
    data object Refresh : GenEvent_105_()
    data class Search(val query: String) : GenEvent_105_()
    data class Filter(val predicate: String) : GenEvent_105_()
}

sealed class GenState_105_ {
    data object Idle : GenState_105_()
    data object Loading : GenState_105_()
    data class Success(val items: List<GenModel_105_>) : GenState_105_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_105_()
    data class Partial(val items: List<GenModel_105_>, val hasMore: Boolean) : GenState_105_()
}

interface GenRepository_105_ {
    suspend fun getAll(): List<GenModel_105_>
    suspend fun getById(id: Long): GenModel_105_?
    suspend fun save(model: GenModel_105_): GenModel_105_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_105_>
}

@Singleton
class GenRepositoryImpl_105_ @Inject constructor() : GenRepository_105_ {
    private val store = mutableMapOf<Long, GenModel_105_>()
    override suspend fun getAll(): List<GenModel_105_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_105_? = store[id]
    override suspend fun save(model: GenModel_105_): GenModel_105_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_105_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_105_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_105_ @Inject constructor(
    private val repository: GenRepositoryImpl_105_
) : GenUseCase_105_<Unit, List<GenModel_105_>> {
    override suspend fun invoke(params: Unit): List<GenModel_105_> = repository.getAll()
}

class GenSaveUseCase_105_ @Inject constructor(
    private val repository: GenRepositoryImpl_105_
) : GenUseCase_105_<GenModel_105_, GenModel_105_> {
    override suspend fun invoke(params: GenModel_105_): GenModel_105_ = repository.save(params)
}

class GenDeleteUseCase_105_ @Inject constructor(
    private val repository: GenRepositoryImpl_105_
) : GenUseCase_105_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_105_ @Inject constructor(
    private val repository: GenRepositoryImpl_105_
) : GenUseCase_105_<String, List<GenModel_105_>> {
    override suspend fun invoke(params: String): List<GenModel_105_> = repository.search(params)
}

abstract class GenMapper_105_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_105_ : GenMapper_105_<GenModel_105_, String>() {
    override fun map(input: GenModel_105_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_105_ : GenMapper_105_<String, GenModel_105_>() {
    override fun map(input: String): GenModel_105_ {
        val parts = input.split(":")
        return GenModel_105_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_105_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_105_,
    private val saveUseCase: GenSaveUseCase_105_,
    private val deleteUseCase: GenDeleteUseCase_105_,
    private val searchUseCase: GenSearchUseCase_105_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_105_>(GenState_105_.Idle)
    val state: StateFlow<GenState_105_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_105_) {
        when (event) {
            is GenEvent_105_.Load -> loadAll()
            is GenEvent_105_.Update -> save(event.model)
            is GenEvent_105_.Delete -> delete(event.id)
            is GenEvent_105_.Refresh -> loadAll()
            is GenEvent_105_.Search -> search(event.query)
            is GenEvent_105_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_105_.Loading; _state.value = GenState_105_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_105_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_105_.Success(searchUseCase(query)) } }
}
