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

data class GenModel_535_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_535_ {
    data class Load(val id: Long) : GenEvent_535_()
    data class Update(val model: GenModel_535_) : GenEvent_535_()
    data class Delete(val id: Long) : GenEvent_535_()
    data object Refresh : GenEvent_535_()
    data class Search(val query: String) : GenEvent_535_()
    data class Filter(val predicate: String) : GenEvent_535_()
}

sealed class GenState_535_ {
    data object Idle : GenState_535_()
    data object Loading : GenState_535_()
    data class Success(val items: List<GenModel_535_>) : GenState_535_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_535_()
    data class Partial(val items: List<GenModel_535_>, val hasMore: Boolean) : GenState_535_()
}

interface GenRepository_535_ {
    suspend fun getAll(): List<GenModel_535_>
    suspend fun getById(id: Long): GenModel_535_?
    suspend fun save(model: GenModel_535_): GenModel_535_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_535_>
}

@Singleton
class GenRepositoryImpl_535_ @Inject constructor() : GenRepository_535_ {
    private val store = mutableMapOf<Long, GenModel_535_>()
    override suspend fun getAll(): List<GenModel_535_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_535_? = store[id]
    override suspend fun save(model: GenModel_535_): GenModel_535_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_535_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_535_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_535_ @Inject constructor(
    private val repository: GenRepositoryImpl_535_
) : GenUseCase_535_<Unit, List<GenModel_535_>> {
    override suspend fun invoke(params: Unit): List<GenModel_535_> = repository.getAll()
}

class GenSaveUseCase_535_ @Inject constructor(
    private val repository: GenRepositoryImpl_535_
) : GenUseCase_535_<GenModel_535_, GenModel_535_> {
    override suspend fun invoke(params: GenModel_535_): GenModel_535_ = repository.save(params)
}

class GenDeleteUseCase_535_ @Inject constructor(
    private val repository: GenRepositoryImpl_535_
) : GenUseCase_535_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_535_ @Inject constructor(
    private val repository: GenRepositoryImpl_535_
) : GenUseCase_535_<String, List<GenModel_535_>> {
    override suspend fun invoke(params: String): List<GenModel_535_> = repository.search(params)
}

abstract class GenMapper_535_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_535_ : GenMapper_535_<GenModel_535_, String>() {
    override fun map(input: GenModel_535_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_535_ : GenMapper_535_<String, GenModel_535_>() {
    override fun map(input: String): GenModel_535_ {
        val parts = input.split(":")
        return GenModel_535_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_535_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_535_,
    private val saveUseCase: GenSaveUseCase_535_,
    private val deleteUseCase: GenDeleteUseCase_535_,
    private val searchUseCase: GenSearchUseCase_535_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_535_>(GenState_535_.Idle)
    val state: StateFlow<GenState_535_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_535_) {
        when (event) {
            is GenEvent_535_.Load -> loadAll()
            is GenEvent_535_.Update -> save(event.model)
            is GenEvent_535_.Delete -> delete(event.id)
            is GenEvent_535_.Refresh -> loadAll()
            is GenEvent_535_.Search -> search(event.query)
            is GenEvent_535_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_535_.Loading; _state.value = GenState_535_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_535_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_535_.Success(searchUseCase(query)) } }
}
