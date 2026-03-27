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

data class GenModel_24_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_24_ {
    data class Load(val id: Long) : GenEvent_24_()
    data class Update(val model: GenModel_24_) : GenEvent_24_()
    data class Delete(val id: Long) : GenEvent_24_()
    data object Refresh : GenEvent_24_()
    data class Search(val query: String) : GenEvent_24_()
    data class Filter(val predicate: String) : GenEvent_24_()
}

sealed class GenState_24_ {
    data object Idle : GenState_24_()
    data object Loading : GenState_24_()
    data class Success(val items: List<GenModel_24_>) : GenState_24_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_24_()
    data class Partial(val items: List<GenModel_24_>, val hasMore: Boolean) : GenState_24_()
}

interface GenRepository_24_ {
    suspend fun getAll(): List<GenModel_24_>
    suspend fun getById(id: Long): GenModel_24_?
    suspend fun save(model: GenModel_24_): GenModel_24_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_24_>
}

@Singleton
class GenRepositoryImpl_24_ @Inject constructor() : GenRepository_24_ {
    private val store = mutableMapOf<Long, GenModel_24_>()
    override suspend fun getAll(): List<GenModel_24_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_24_? = store[id]
    override suspend fun save(model: GenModel_24_): GenModel_24_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_24_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_24_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_24_ @Inject constructor(
    private val repository: GenRepositoryImpl_24_
) : GenUseCase_24_<Unit, List<GenModel_24_>> {
    override suspend fun invoke(params: Unit): List<GenModel_24_> = repository.getAll()
}

class GenSaveUseCase_24_ @Inject constructor(
    private val repository: GenRepositoryImpl_24_
) : GenUseCase_24_<GenModel_24_, GenModel_24_> {
    override suspend fun invoke(params: GenModel_24_): GenModel_24_ = repository.save(params)
}

class GenDeleteUseCase_24_ @Inject constructor(
    private val repository: GenRepositoryImpl_24_
) : GenUseCase_24_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_24_ @Inject constructor(
    private val repository: GenRepositoryImpl_24_
) : GenUseCase_24_<String, List<GenModel_24_>> {
    override suspend fun invoke(params: String): List<GenModel_24_> = repository.search(params)
}

abstract class GenMapper_24_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_24_ : GenMapper_24_<GenModel_24_, String>() {
    override fun map(input: GenModel_24_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_24_ : GenMapper_24_<String, GenModel_24_>() {
    override fun map(input: String): GenModel_24_ {
        val parts = input.split(":")
        return GenModel_24_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_24_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_24_,
    private val saveUseCase: GenSaveUseCase_24_,
    private val deleteUseCase: GenDeleteUseCase_24_,
    private val searchUseCase: GenSearchUseCase_24_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_24_>(GenState_24_.Idle)
    val state: StateFlow<GenState_24_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_24_) {
        when (event) {
            is GenEvent_24_.Load -> loadAll()
            is GenEvent_24_.Update -> save(event.model)
            is GenEvent_24_.Delete -> delete(event.id)
            is GenEvent_24_.Refresh -> loadAll()
            is GenEvent_24_.Search -> search(event.query)
            is GenEvent_24_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_24_.Loading; _state.value = GenState_24_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_24_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_24_.Success(searchUseCase(query)) } }
}
