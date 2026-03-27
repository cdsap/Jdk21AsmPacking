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

data class GenModel_116_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_116_ {
    data class Load(val id: Long) : GenEvent_116_()
    data class Update(val model: GenModel_116_) : GenEvent_116_()
    data class Delete(val id: Long) : GenEvent_116_()
    data object Refresh : GenEvent_116_()
    data class Search(val query: String) : GenEvent_116_()
    data class Filter(val predicate: String) : GenEvent_116_()
}

sealed class GenState_116_ {
    data object Idle : GenState_116_()
    data object Loading : GenState_116_()
    data class Success(val items: List<GenModel_116_>) : GenState_116_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_116_()
    data class Partial(val items: List<GenModel_116_>, val hasMore: Boolean) : GenState_116_()
}

interface GenRepository_116_ {
    suspend fun getAll(): List<GenModel_116_>
    suspend fun getById(id: Long): GenModel_116_?
    suspend fun save(model: GenModel_116_): GenModel_116_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_116_>
}

@Singleton
class GenRepositoryImpl_116_ @Inject constructor() : GenRepository_116_ {
    private val store = mutableMapOf<Long, GenModel_116_>()
    override suspend fun getAll(): List<GenModel_116_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_116_? = store[id]
    override suspend fun save(model: GenModel_116_): GenModel_116_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_116_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_116_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_116_ @Inject constructor(
    private val repository: GenRepositoryImpl_116_
) : GenUseCase_116_<Unit, List<GenModel_116_>> {
    override suspend fun invoke(params: Unit): List<GenModel_116_> = repository.getAll()
}

class GenSaveUseCase_116_ @Inject constructor(
    private val repository: GenRepositoryImpl_116_
) : GenUseCase_116_<GenModel_116_, GenModel_116_> {
    override suspend fun invoke(params: GenModel_116_): GenModel_116_ = repository.save(params)
}

class GenDeleteUseCase_116_ @Inject constructor(
    private val repository: GenRepositoryImpl_116_
) : GenUseCase_116_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_116_ @Inject constructor(
    private val repository: GenRepositoryImpl_116_
) : GenUseCase_116_<String, List<GenModel_116_>> {
    override suspend fun invoke(params: String): List<GenModel_116_> = repository.search(params)
}

abstract class GenMapper_116_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_116_ : GenMapper_116_<GenModel_116_, String>() {
    override fun map(input: GenModel_116_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_116_ : GenMapper_116_<String, GenModel_116_>() {
    override fun map(input: String): GenModel_116_ {
        val parts = input.split(":")
        return GenModel_116_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_116_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_116_,
    private val saveUseCase: GenSaveUseCase_116_,
    private val deleteUseCase: GenDeleteUseCase_116_,
    private val searchUseCase: GenSearchUseCase_116_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_116_>(GenState_116_.Idle)
    val state: StateFlow<GenState_116_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_116_) {
        when (event) {
            is GenEvent_116_.Load -> loadAll()
            is GenEvent_116_.Update -> save(event.model)
            is GenEvent_116_.Delete -> delete(event.id)
            is GenEvent_116_.Refresh -> loadAll()
            is GenEvent_116_.Search -> search(event.query)
            is GenEvent_116_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_116_.Loading; _state.value = GenState_116_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_116_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_116_.Success(searchUseCase(query)) } }
}
