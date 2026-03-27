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

data class GenModel_1068_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1068_ {
    data class Load(val id: Long) : GenEvent_1068_()
    data class Update(val model: GenModel_1068_) : GenEvent_1068_()
    data class Delete(val id: Long) : GenEvent_1068_()
    data object Refresh : GenEvent_1068_()
    data class Search(val query: String) : GenEvent_1068_()
    data class Filter(val predicate: String) : GenEvent_1068_()
}

sealed class GenState_1068_ {
    data object Idle : GenState_1068_()
    data object Loading : GenState_1068_()
    data class Success(val items: List<GenModel_1068_>) : GenState_1068_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1068_()
    data class Partial(val items: List<GenModel_1068_>, val hasMore: Boolean) : GenState_1068_()
}

interface GenRepository_1068_ {
    suspend fun getAll(): List<GenModel_1068_>
    suspend fun getById(id: Long): GenModel_1068_?
    suspend fun save(model: GenModel_1068_): GenModel_1068_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1068_>
}

@Singleton
class GenRepositoryImpl_1068_ @Inject constructor() : GenRepository_1068_ {
    private val store = mutableMapOf<Long, GenModel_1068_>()
    override suspend fun getAll(): List<GenModel_1068_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1068_? = store[id]
    override suspend fun save(model: GenModel_1068_): GenModel_1068_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1068_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1068_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1068_ @Inject constructor(
    private val repository: GenRepositoryImpl_1068_
) : GenUseCase_1068_<Unit, List<GenModel_1068_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1068_> = repository.getAll()
}

class GenSaveUseCase_1068_ @Inject constructor(
    private val repository: GenRepositoryImpl_1068_
) : GenUseCase_1068_<GenModel_1068_, GenModel_1068_> {
    override suspend fun invoke(params: GenModel_1068_): GenModel_1068_ = repository.save(params)
}

class GenDeleteUseCase_1068_ @Inject constructor(
    private val repository: GenRepositoryImpl_1068_
) : GenUseCase_1068_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1068_ @Inject constructor(
    private val repository: GenRepositoryImpl_1068_
) : GenUseCase_1068_<String, List<GenModel_1068_>> {
    override suspend fun invoke(params: String): List<GenModel_1068_> = repository.search(params)
}

abstract class GenMapper_1068_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1068_ : GenMapper_1068_<GenModel_1068_, String>() {
    override fun map(input: GenModel_1068_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1068_ : GenMapper_1068_<String, GenModel_1068_>() {
    override fun map(input: String): GenModel_1068_ {
        val parts = input.split(":")
        return GenModel_1068_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1068_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1068_,
    private val saveUseCase: GenSaveUseCase_1068_,
    private val deleteUseCase: GenDeleteUseCase_1068_,
    private val searchUseCase: GenSearchUseCase_1068_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1068_>(GenState_1068_.Idle)
    val state: StateFlow<GenState_1068_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1068_) {
        when (event) {
            is GenEvent_1068_.Load -> loadAll()
            is GenEvent_1068_.Update -> save(event.model)
            is GenEvent_1068_.Delete -> delete(event.id)
            is GenEvent_1068_.Refresh -> loadAll()
            is GenEvent_1068_.Search -> search(event.query)
            is GenEvent_1068_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1068_.Loading; _state.value = GenState_1068_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1068_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1068_.Success(searchUseCase(query)) } }
}
