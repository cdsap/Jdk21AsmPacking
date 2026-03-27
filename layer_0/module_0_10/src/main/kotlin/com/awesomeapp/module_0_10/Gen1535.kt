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

data class GenModel_1535_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1535_ {
    data class Load(val id: Long) : GenEvent_1535_()
    data class Update(val model: GenModel_1535_) : GenEvent_1535_()
    data class Delete(val id: Long) : GenEvent_1535_()
    data object Refresh : GenEvent_1535_()
    data class Search(val query: String) : GenEvent_1535_()
    data class Filter(val predicate: String) : GenEvent_1535_()
}

sealed class GenState_1535_ {
    data object Idle : GenState_1535_()
    data object Loading : GenState_1535_()
    data class Success(val items: List<GenModel_1535_>) : GenState_1535_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1535_()
    data class Partial(val items: List<GenModel_1535_>, val hasMore: Boolean) : GenState_1535_()
}

interface GenRepository_1535_ {
    suspend fun getAll(): List<GenModel_1535_>
    suspend fun getById(id: Long): GenModel_1535_?
    suspend fun save(model: GenModel_1535_): GenModel_1535_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1535_>
}

@Singleton
class GenRepositoryImpl_1535_ @Inject constructor() : GenRepository_1535_ {
    private val store = mutableMapOf<Long, GenModel_1535_>()
    override suspend fun getAll(): List<GenModel_1535_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1535_? = store[id]
    override suspend fun save(model: GenModel_1535_): GenModel_1535_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1535_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1535_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1535_ @Inject constructor(
    private val repository: GenRepositoryImpl_1535_
) : GenUseCase_1535_<Unit, List<GenModel_1535_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1535_> = repository.getAll()
}

class GenSaveUseCase_1535_ @Inject constructor(
    private val repository: GenRepositoryImpl_1535_
) : GenUseCase_1535_<GenModel_1535_, GenModel_1535_> {
    override suspend fun invoke(params: GenModel_1535_): GenModel_1535_ = repository.save(params)
}

class GenDeleteUseCase_1535_ @Inject constructor(
    private val repository: GenRepositoryImpl_1535_
) : GenUseCase_1535_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1535_ @Inject constructor(
    private val repository: GenRepositoryImpl_1535_
) : GenUseCase_1535_<String, List<GenModel_1535_>> {
    override suspend fun invoke(params: String): List<GenModel_1535_> = repository.search(params)
}

abstract class GenMapper_1535_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1535_ : GenMapper_1535_<GenModel_1535_, String>() {
    override fun map(input: GenModel_1535_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1535_ : GenMapper_1535_<String, GenModel_1535_>() {
    override fun map(input: String): GenModel_1535_ {
        val parts = input.split(":")
        return GenModel_1535_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1535_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1535_,
    private val saveUseCase: GenSaveUseCase_1535_,
    private val deleteUseCase: GenDeleteUseCase_1535_,
    private val searchUseCase: GenSearchUseCase_1535_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1535_>(GenState_1535_.Idle)
    val state: StateFlow<GenState_1535_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1535_) {
        when (event) {
            is GenEvent_1535_.Load -> loadAll()
            is GenEvent_1535_.Update -> save(event.model)
            is GenEvent_1535_.Delete -> delete(event.id)
            is GenEvent_1535_.Refresh -> loadAll()
            is GenEvent_1535_.Search -> search(event.query)
            is GenEvent_1535_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1535_.Loading; _state.value = GenState_1535_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1535_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1535_.Success(searchUseCase(query)) } }
}
