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

data class GenModel_3068_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3068_ {
    data class Load(val id: Long) : GenEvent_3068_()
    data class Update(val model: GenModel_3068_) : GenEvent_3068_()
    data class Delete(val id: Long) : GenEvent_3068_()
    data object Refresh : GenEvent_3068_()
    data class Search(val query: String) : GenEvent_3068_()
    data class Filter(val predicate: String) : GenEvent_3068_()
}

sealed class GenState_3068_ {
    data object Idle : GenState_3068_()
    data object Loading : GenState_3068_()
    data class Success(val items: List<GenModel_3068_>) : GenState_3068_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3068_()
    data class Partial(val items: List<GenModel_3068_>, val hasMore: Boolean) : GenState_3068_()
}

interface GenRepository_3068_ {
    suspend fun getAll(): List<GenModel_3068_>
    suspend fun getById(id: Long): GenModel_3068_?
    suspend fun save(model: GenModel_3068_): GenModel_3068_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3068_>
}

@Singleton
class GenRepositoryImpl_3068_ @Inject constructor() : GenRepository_3068_ {
    private val store = mutableMapOf<Long, GenModel_3068_>()
    override suspend fun getAll(): List<GenModel_3068_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3068_? = store[id]
    override suspend fun save(model: GenModel_3068_): GenModel_3068_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3068_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3068_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3068_ @Inject constructor(
    private val repository: GenRepositoryImpl_3068_
) : GenUseCase_3068_<Unit, List<GenModel_3068_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3068_> = repository.getAll()
}

class GenSaveUseCase_3068_ @Inject constructor(
    private val repository: GenRepositoryImpl_3068_
) : GenUseCase_3068_<GenModel_3068_, GenModel_3068_> {
    override suspend fun invoke(params: GenModel_3068_): GenModel_3068_ = repository.save(params)
}

class GenDeleteUseCase_3068_ @Inject constructor(
    private val repository: GenRepositoryImpl_3068_
) : GenUseCase_3068_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3068_ @Inject constructor(
    private val repository: GenRepositoryImpl_3068_
) : GenUseCase_3068_<String, List<GenModel_3068_>> {
    override suspend fun invoke(params: String): List<GenModel_3068_> = repository.search(params)
}

abstract class GenMapper_3068_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3068_ : GenMapper_3068_<GenModel_3068_, String>() {
    override fun map(input: GenModel_3068_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3068_ : GenMapper_3068_<String, GenModel_3068_>() {
    override fun map(input: String): GenModel_3068_ {
        val parts = input.split(":")
        return GenModel_3068_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3068_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3068_,
    private val saveUseCase: GenSaveUseCase_3068_,
    private val deleteUseCase: GenDeleteUseCase_3068_,
    private val searchUseCase: GenSearchUseCase_3068_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3068_>(GenState_3068_.Idle)
    val state: StateFlow<GenState_3068_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3068_) {
        when (event) {
            is GenEvent_3068_.Load -> loadAll()
            is GenEvent_3068_.Update -> save(event.model)
            is GenEvent_3068_.Delete -> delete(event.id)
            is GenEvent_3068_.Refresh -> loadAll()
            is GenEvent_3068_.Search -> search(event.query)
            is GenEvent_3068_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3068_.Loading; _state.value = GenState_3068_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3068_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3068_.Success(searchUseCase(query)) } }
}
