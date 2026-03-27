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

data class GenModel_805_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_805_ {
    data class Load(val id: Long) : GenEvent_805_()
    data class Update(val model: GenModel_805_) : GenEvent_805_()
    data class Delete(val id: Long) : GenEvent_805_()
    data object Refresh : GenEvent_805_()
    data class Search(val query: String) : GenEvent_805_()
    data class Filter(val predicate: String) : GenEvent_805_()
}

sealed class GenState_805_ {
    data object Idle : GenState_805_()
    data object Loading : GenState_805_()
    data class Success(val items: List<GenModel_805_>) : GenState_805_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_805_()
    data class Partial(val items: List<GenModel_805_>, val hasMore: Boolean) : GenState_805_()
}

interface GenRepository_805_ {
    suspend fun getAll(): List<GenModel_805_>
    suspend fun getById(id: Long): GenModel_805_?
    suspend fun save(model: GenModel_805_): GenModel_805_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_805_>
}

@Singleton
class GenRepositoryImpl_805_ @Inject constructor() : GenRepository_805_ {
    private val store = mutableMapOf<Long, GenModel_805_>()
    override suspend fun getAll(): List<GenModel_805_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_805_? = store[id]
    override suspend fun save(model: GenModel_805_): GenModel_805_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_805_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_805_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_805_ @Inject constructor(
    private val repository: GenRepositoryImpl_805_
) : GenUseCase_805_<Unit, List<GenModel_805_>> {
    override suspend fun invoke(params: Unit): List<GenModel_805_> = repository.getAll()
}

class GenSaveUseCase_805_ @Inject constructor(
    private val repository: GenRepositoryImpl_805_
) : GenUseCase_805_<GenModel_805_, GenModel_805_> {
    override suspend fun invoke(params: GenModel_805_): GenModel_805_ = repository.save(params)
}

class GenDeleteUseCase_805_ @Inject constructor(
    private val repository: GenRepositoryImpl_805_
) : GenUseCase_805_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_805_ @Inject constructor(
    private val repository: GenRepositoryImpl_805_
) : GenUseCase_805_<String, List<GenModel_805_>> {
    override suspend fun invoke(params: String): List<GenModel_805_> = repository.search(params)
}

abstract class GenMapper_805_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_805_ : GenMapper_805_<GenModel_805_, String>() {
    override fun map(input: GenModel_805_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_805_ : GenMapper_805_<String, GenModel_805_>() {
    override fun map(input: String): GenModel_805_ {
        val parts = input.split(":")
        return GenModel_805_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_805_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_805_,
    private val saveUseCase: GenSaveUseCase_805_,
    private val deleteUseCase: GenDeleteUseCase_805_,
    private val searchUseCase: GenSearchUseCase_805_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_805_>(GenState_805_.Idle)
    val state: StateFlow<GenState_805_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_805_) {
        when (event) {
            is GenEvent_805_.Load -> loadAll()
            is GenEvent_805_.Update -> save(event.model)
            is GenEvent_805_.Delete -> delete(event.id)
            is GenEvent_805_.Refresh -> loadAll()
            is GenEvent_805_.Search -> search(event.query)
            is GenEvent_805_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_805_.Loading; _state.value = GenState_805_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_805_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_805_.Success(searchUseCase(query)) } }
}
