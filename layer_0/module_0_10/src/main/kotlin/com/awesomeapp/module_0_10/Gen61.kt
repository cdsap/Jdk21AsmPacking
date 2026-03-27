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

data class GenModel_61_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_61_ {
    data class Load(val id: Long) : GenEvent_61_()
    data class Update(val model: GenModel_61_) : GenEvent_61_()
    data class Delete(val id: Long) : GenEvent_61_()
    data object Refresh : GenEvent_61_()
    data class Search(val query: String) : GenEvent_61_()
    data class Filter(val predicate: String) : GenEvent_61_()
}

sealed class GenState_61_ {
    data object Idle : GenState_61_()
    data object Loading : GenState_61_()
    data class Success(val items: List<GenModel_61_>) : GenState_61_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_61_()
    data class Partial(val items: List<GenModel_61_>, val hasMore: Boolean) : GenState_61_()
}

interface GenRepository_61_ {
    suspend fun getAll(): List<GenModel_61_>
    suspend fun getById(id: Long): GenModel_61_?
    suspend fun save(model: GenModel_61_): GenModel_61_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_61_>
}

@Singleton
class GenRepositoryImpl_61_ @Inject constructor() : GenRepository_61_ {
    private val store = mutableMapOf<Long, GenModel_61_>()
    override suspend fun getAll(): List<GenModel_61_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_61_? = store[id]
    override suspend fun save(model: GenModel_61_): GenModel_61_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_61_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_61_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_61_ @Inject constructor(
    private val repository: GenRepositoryImpl_61_
) : GenUseCase_61_<Unit, List<GenModel_61_>> {
    override suspend fun invoke(params: Unit): List<GenModel_61_> = repository.getAll()
}

class GenSaveUseCase_61_ @Inject constructor(
    private val repository: GenRepositoryImpl_61_
) : GenUseCase_61_<GenModel_61_, GenModel_61_> {
    override suspend fun invoke(params: GenModel_61_): GenModel_61_ = repository.save(params)
}

class GenDeleteUseCase_61_ @Inject constructor(
    private val repository: GenRepositoryImpl_61_
) : GenUseCase_61_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_61_ @Inject constructor(
    private val repository: GenRepositoryImpl_61_
) : GenUseCase_61_<String, List<GenModel_61_>> {
    override suspend fun invoke(params: String): List<GenModel_61_> = repository.search(params)
}

abstract class GenMapper_61_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_61_ : GenMapper_61_<GenModel_61_, String>() {
    override fun map(input: GenModel_61_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_61_ : GenMapper_61_<String, GenModel_61_>() {
    override fun map(input: String): GenModel_61_ {
        val parts = input.split(":")
        return GenModel_61_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_61_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_61_,
    private val saveUseCase: GenSaveUseCase_61_,
    private val deleteUseCase: GenDeleteUseCase_61_,
    private val searchUseCase: GenSearchUseCase_61_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_61_>(GenState_61_.Idle)
    val state: StateFlow<GenState_61_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_61_) {
        when (event) {
            is GenEvent_61_.Load -> loadAll()
            is GenEvent_61_.Update -> save(event.model)
            is GenEvent_61_.Delete -> delete(event.id)
            is GenEvent_61_.Refresh -> loadAll()
            is GenEvent_61_.Search -> search(event.query)
            is GenEvent_61_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_61_.Loading; _state.value = GenState_61_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_61_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_61_.Success(searchUseCase(query)) } }
}
