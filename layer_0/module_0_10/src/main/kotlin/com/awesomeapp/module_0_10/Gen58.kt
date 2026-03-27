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

data class GenModel_58_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_58_ {
    data class Load(val id: Long) : GenEvent_58_()
    data class Update(val model: GenModel_58_) : GenEvent_58_()
    data class Delete(val id: Long) : GenEvent_58_()
    data object Refresh : GenEvent_58_()
    data class Search(val query: String) : GenEvent_58_()
    data class Filter(val predicate: String) : GenEvent_58_()
}

sealed class GenState_58_ {
    data object Idle : GenState_58_()
    data object Loading : GenState_58_()
    data class Success(val items: List<GenModel_58_>) : GenState_58_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_58_()
    data class Partial(val items: List<GenModel_58_>, val hasMore: Boolean) : GenState_58_()
}

interface GenRepository_58_ {
    suspend fun getAll(): List<GenModel_58_>
    suspend fun getById(id: Long): GenModel_58_?
    suspend fun save(model: GenModel_58_): GenModel_58_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_58_>
}

@Singleton
class GenRepositoryImpl_58_ @Inject constructor() : GenRepository_58_ {
    private val store = mutableMapOf<Long, GenModel_58_>()
    override suspend fun getAll(): List<GenModel_58_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_58_? = store[id]
    override suspend fun save(model: GenModel_58_): GenModel_58_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_58_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_58_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_58_ @Inject constructor(
    private val repository: GenRepositoryImpl_58_
) : GenUseCase_58_<Unit, List<GenModel_58_>> {
    override suspend fun invoke(params: Unit): List<GenModel_58_> = repository.getAll()
}

class GenSaveUseCase_58_ @Inject constructor(
    private val repository: GenRepositoryImpl_58_
) : GenUseCase_58_<GenModel_58_, GenModel_58_> {
    override suspend fun invoke(params: GenModel_58_): GenModel_58_ = repository.save(params)
}

class GenDeleteUseCase_58_ @Inject constructor(
    private val repository: GenRepositoryImpl_58_
) : GenUseCase_58_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_58_ @Inject constructor(
    private val repository: GenRepositoryImpl_58_
) : GenUseCase_58_<String, List<GenModel_58_>> {
    override suspend fun invoke(params: String): List<GenModel_58_> = repository.search(params)
}

abstract class GenMapper_58_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_58_ : GenMapper_58_<GenModel_58_, String>() {
    override fun map(input: GenModel_58_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_58_ : GenMapper_58_<String, GenModel_58_>() {
    override fun map(input: String): GenModel_58_ {
        val parts = input.split(":")
        return GenModel_58_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_58_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_58_,
    private val saveUseCase: GenSaveUseCase_58_,
    private val deleteUseCase: GenDeleteUseCase_58_,
    private val searchUseCase: GenSearchUseCase_58_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_58_>(GenState_58_.Idle)
    val state: StateFlow<GenState_58_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_58_) {
        when (event) {
            is GenEvent_58_.Load -> loadAll()
            is GenEvent_58_.Update -> save(event.model)
            is GenEvent_58_.Delete -> delete(event.id)
            is GenEvent_58_.Refresh -> loadAll()
            is GenEvent_58_.Search -> search(event.query)
            is GenEvent_58_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_58_.Loading; _state.value = GenState_58_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_58_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_58_.Success(searchUseCase(query)) } }
}
