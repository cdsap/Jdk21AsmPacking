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

data class GenModel_304_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_304_ {
    data class Load(val id: Long) : GenEvent_304_()
    data class Update(val model: GenModel_304_) : GenEvent_304_()
    data class Delete(val id: Long) : GenEvent_304_()
    data object Refresh : GenEvent_304_()
    data class Search(val query: String) : GenEvent_304_()
    data class Filter(val predicate: String) : GenEvent_304_()
}

sealed class GenState_304_ {
    data object Idle : GenState_304_()
    data object Loading : GenState_304_()
    data class Success(val items: List<GenModel_304_>) : GenState_304_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_304_()
    data class Partial(val items: List<GenModel_304_>, val hasMore: Boolean) : GenState_304_()
}

interface GenRepository_304_ {
    suspend fun getAll(): List<GenModel_304_>
    suspend fun getById(id: Long): GenModel_304_?
    suspend fun save(model: GenModel_304_): GenModel_304_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_304_>
}

@Singleton
class GenRepositoryImpl_304_ @Inject constructor() : GenRepository_304_ {
    private val store = mutableMapOf<Long, GenModel_304_>()
    override suspend fun getAll(): List<GenModel_304_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_304_? = store[id]
    override suspend fun save(model: GenModel_304_): GenModel_304_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_304_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_304_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_304_ @Inject constructor(
    private val repository: GenRepositoryImpl_304_
) : GenUseCase_304_<Unit, List<GenModel_304_>> {
    override suspend fun invoke(params: Unit): List<GenModel_304_> = repository.getAll()
}

class GenSaveUseCase_304_ @Inject constructor(
    private val repository: GenRepositoryImpl_304_
) : GenUseCase_304_<GenModel_304_, GenModel_304_> {
    override suspend fun invoke(params: GenModel_304_): GenModel_304_ = repository.save(params)
}

class GenDeleteUseCase_304_ @Inject constructor(
    private val repository: GenRepositoryImpl_304_
) : GenUseCase_304_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_304_ @Inject constructor(
    private val repository: GenRepositoryImpl_304_
) : GenUseCase_304_<String, List<GenModel_304_>> {
    override suspend fun invoke(params: String): List<GenModel_304_> = repository.search(params)
}

abstract class GenMapper_304_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_304_ : GenMapper_304_<GenModel_304_, String>() {
    override fun map(input: GenModel_304_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_304_ : GenMapper_304_<String, GenModel_304_>() {
    override fun map(input: String): GenModel_304_ {
        val parts = input.split(":")
        return GenModel_304_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_304_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_304_,
    private val saveUseCase: GenSaveUseCase_304_,
    private val deleteUseCase: GenDeleteUseCase_304_,
    private val searchUseCase: GenSearchUseCase_304_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_304_>(GenState_304_.Idle)
    val state: StateFlow<GenState_304_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_304_) {
        when (event) {
            is GenEvent_304_.Load -> loadAll()
            is GenEvent_304_.Update -> save(event.model)
            is GenEvent_304_.Delete -> delete(event.id)
            is GenEvent_304_.Refresh -> loadAll()
            is GenEvent_304_.Search -> search(event.query)
            is GenEvent_304_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_304_.Loading; _state.value = GenState_304_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_304_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_304_.Success(searchUseCase(query)) } }
}
