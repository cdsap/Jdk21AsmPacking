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

data class GenModel_3162_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3162_ {
    data class Load(val id: Long) : GenEvent_3162_()
    data class Update(val model: GenModel_3162_) : GenEvent_3162_()
    data class Delete(val id: Long) : GenEvent_3162_()
    data object Refresh : GenEvent_3162_()
    data class Search(val query: String) : GenEvent_3162_()
    data class Filter(val predicate: String) : GenEvent_3162_()
}

sealed class GenState_3162_ {
    data object Idle : GenState_3162_()
    data object Loading : GenState_3162_()
    data class Success(val items: List<GenModel_3162_>) : GenState_3162_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3162_()
    data class Partial(val items: List<GenModel_3162_>, val hasMore: Boolean) : GenState_3162_()
}

interface GenRepository_3162_ {
    suspend fun getAll(): List<GenModel_3162_>
    suspend fun getById(id: Long): GenModel_3162_?
    suspend fun save(model: GenModel_3162_): GenModel_3162_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3162_>
}

@Singleton
class GenRepositoryImpl_3162_ @Inject constructor() : GenRepository_3162_ {
    private val store = mutableMapOf<Long, GenModel_3162_>()
    override suspend fun getAll(): List<GenModel_3162_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3162_? = store[id]
    override suspend fun save(model: GenModel_3162_): GenModel_3162_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3162_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3162_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3162_ @Inject constructor(
    private val repository: GenRepositoryImpl_3162_
) : GenUseCase_3162_<Unit, List<GenModel_3162_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3162_> = repository.getAll()
}

class GenSaveUseCase_3162_ @Inject constructor(
    private val repository: GenRepositoryImpl_3162_
) : GenUseCase_3162_<GenModel_3162_, GenModel_3162_> {
    override suspend fun invoke(params: GenModel_3162_): GenModel_3162_ = repository.save(params)
}

class GenDeleteUseCase_3162_ @Inject constructor(
    private val repository: GenRepositoryImpl_3162_
) : GenUseCase_3162_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3162_ @Inject constructor(
    private val repository: GenRepositoryImpl_3162_
) : GenUseCase_3162_<String, List<GenModel_3162_>> {
    override suspend fun invoke(params: String): List<GenModel_3162_> = repository.search(params)
}

abstract class GenMapper_3162_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3162_ : GenMapper_3162_<GenModel_3162_, String>() {
    override fun map(input: GenModel_3162_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3162_ : GenMapper_3162_<String, GenModel_3162_>() {
    override fun map(input: String): GenModel_3162_ {
        val parts = input.split(":")
        return GenModel_3162_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3162_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3162_,
    private val saveUseCase: GenSaveUseCase_3162_,
    private val deleteUseCase: GenDeleteUseCase_3162_,
    private val searchUseCase: GenSearchUseCase_3162_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3162_>(GenState_3162_.Idle)
    val state: StateFlow<GenState_3162_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3162_) {
        when (event) {
            is GenEvent_3162_.Load -> loadAll()
            is GenEvent_3162_.Update -> save(event.model)
            is GenEvent_3162_.Delete -> delete(event.id)
            is GenEvent_3162_.Refresh -> loadAll()
            is GenEvent_3162_.Search -> search(event.query)
            is GenEvent_3162_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3162_.Loading; _state.value = GenState_3162_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3162_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3162_.Success(searchUseCase(query)) } }
}
