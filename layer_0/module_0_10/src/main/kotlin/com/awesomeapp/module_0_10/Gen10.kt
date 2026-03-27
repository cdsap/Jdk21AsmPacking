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

data class GenModel_10_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_10_ {
    data class Load(val id: Long) : GenEvent_10_()
    data class Update(val model: GenModel_10_) : GenEvent_10_()
    data class Delete(val id: Long) : GenEvent_10_()
    data object Refresh : GenEvent_10_()
    data class Search(val query: String) : GenEvent_10_()
    data class Filter(val predicate: String) : GenEvent_10_()
}

sealed class GenState_10_ {
    data object Idle : GenState_10_()
    data object Loading : GenState_10_()
    data class Success(val items: List<GenModel_10_>) : GenState_10_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_10_()
    data class Partial(val items: List<GenModel_10_>, val hasMore: Boolean) : GenState_10_()
}

interface GenRepository_10_ {
    suspend fun getAll(): List<GenModel_10_>
    suspend fun getById(id: Long): GenModel_10_?
    suspend fun save(model: GenModel_10_): GenModel_10_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_10_>
}

@Singleton
class GenRepositoryImpl_10_ @Inject constructor() : GenRepository_10_ {
    private val store = mutableMapOf<Long, GenModel_10_>()
    override suspend fun getAll(): List<GenModel_10_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_10_? = store[id]
    override suspend fun save(model: GenModel_10_): GenModel_10_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_10_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_10_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_10_ @Inject constructor(
    private val repository: GenRepositoryImpl_10_
) : GenUseCase_10_<Unit, List<GenModel_10_>> {
    override suspend fun invoke(params: Unit): List<GenModel_10_> = repository.getAll()
}

class GenSaveUseCase_10_ @Inject constructor(
    private val repository: GenRepositoryImpl_10_
) : GenUseCase_10_<GenModel_10_, GenModel_10_> {
    override suspend fun invoke(params: GenModel_10_): GenModel_10_ = repository.save(params)
}

class GenDeleteUseCase_10_ @Inject constructor(
    private val repository: GenRepositoryImpl_10_
) : GenUseCase_10_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_10_ @Inject constructor(
    private val repository: GenRepositoryImpl_10_
) : GenUseCase_10_<String, List<GenModel_10_>> {
    override suspend fun invoke(params: String): List<GenModel_10_> = repository.search(params)
}

abstract class GenMapper_10_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_10_ : GenMapper_10_<GenModel_10_, String>() {
    override fun map(input: GenModel_10_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_10_ : GenMapper_10_<String, GenModel_10_>() {
    override fun map(input: String): GenModel_10_ {
        val parts = input.split(":")
        return GenModel_10_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_10_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_10_,
    private val saveUseCase: GenSaveUseCase_10_,
    private val deleteUseCase: GenDeleteUseCase_10_,
    private val searchUseCase: GenSearchUseCase_10_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_10_>(GenState_10_.Idle)
    val state: StateFlow<GenState_10_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_10_) {
        when (event) {
            is GenEvent_10_.Load -> loadAll()
            is GenEvent_10_.Update -> save(event.model)
            is GenEvent_10_.Delete -> delete(event.id)
            is GenEvent_10_.Refresh -> loadAll()
            is GenEvent_10_.Search -> search(event.query)
            is GenEvent_10_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_10_.Loading; _state.value = GenState_10_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_10_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_10_.Success(searchUseCase(query)) } }
}
