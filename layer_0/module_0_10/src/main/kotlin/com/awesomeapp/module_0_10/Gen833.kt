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

data class GenModel_833_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_833_ {
    data class Load(val id: Long) : GenEvent_833_()
    data class Update(val model: GenModel_833_) : GenEvent_833_()
    data class Delete(val id: Long) : GenEvent_833_()
    data object Refresh : GenEvent_833_()
    data class Search(val query: String) : GenEvent_833_()
    data class Filter(val predicate: String) : GenEvent_833_()
}

sealed class GenState_833_ {
    data object Idle : GenState_833_()
    data object Loading : GenState_833_()
    data class Success(val items: List<GenModel_833_>) : GenState_833_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_833_()
    data class Partial(val items: List<GenModel_833_>, val hasMore: Boolean) : GenState_833_()
}

interface GenRepository_833_ {
    suspend fun getAll(): List<GenModel_833_>
    suspend fun getById(id: Long): GenModel_833_?
    suspend fun save(model: GenModel_833_): GenModel_833_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_833_>
}

@Singleton
class GenRepositoryImpl_833_ @Inject constructor() : GenRepository_833_ {
    private val store = mutableMapOf<Long, GenModel_833_>()
    override suspend fun getAll(): List<GenModel_833_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_833_? = store[id]
    override suspend fun save(model: GenModel_833_): GenModel_833_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_833_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_833_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_833_ @Inject constructor(
    private val repository: GenRepositoryImpl_833_
) : GenUseCase_833_<Unit, List<GenModel_833_>> {
    override suspend fun invoke(params: Unit): List<GenModel_833_> = repository.getAll()
}

class GenSaveUseCase_833_ @Inject constructor(
    private val repository: GenRepositoryImpl_833_
) : GenUseCase_833_<GenModel_833_, GenModel_833_> {
    override suspend fun invoke(params: GenModel_833_): GenModel_833_ = repository.save(params)
}

class GenDeleteUseCase_833_ @Inject constructor(
    private val repository: GenRepositoryImpl_833_
) : GenUseCase_833_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_833_ @Inject constructor(
    private val repository: GenRepositoryImpl_833_
) : GenUseCase_833_<String, List<GenModel_833_>> {
    override suspend fun invoke(params: String): List<GenModel_833_> = repository.search(params)
}

abstract class GenMapper_833_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_833_ : GenMapper_833_<GenModel_833_, String>() {
    override fun map(input: GenModel_833_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_833_ : GenMapper_833_<String, GenModel_833_>() {
    override fun map(input: String): GenModel_833_ {
        val parts = input.split(":")
        return GenModel_833_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_833_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_833_,
    private val saveUseCase: GenSaveUseCase_833_,
    private val deleteUseCase: GenDeleteUseCase_833_,
    private val searchUseCase: GenSearchUseCase_833_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_833_>(GenState_833_.Idle)
    val state: StateFlow<GenState_833_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_833_) {
        when (event) {
            is GenEvent_833_.Load -> loadAll()
            is GenEvent_833_.Update -> save(event.model)
            is GenEvent_833_.Delete -> delete(event.id)
            is GenEvent_833_.Refresh -> loadAll()
            is GenEvent_833_.Search -> search(event.query)
            is GenEvent_833_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_833_.Loading; _state.value = GenState_833_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_833_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_833_.Success(searchUseCase(query)) } }
}
