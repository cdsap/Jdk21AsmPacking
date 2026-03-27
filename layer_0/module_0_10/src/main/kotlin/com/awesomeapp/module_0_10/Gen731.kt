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

data class GenModel_731_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_731_ {
    data class Load(val id: Long) : GenEvent_731_()
    data class Update(val model: GenModel_731_) : GenEvent_731_()
    data class Delete(val id: Long) : GenEvent_731_()
    data object Refresh : GenEvent_731_()
    data class Search(val query: String) : GenEvent_731_()
    data class Filter(val predicate: String) : GenEvent_731_()
}

sealed class GenState_731_ {
    data object Idle : GenState_731_()
    data object Loading : GenState_731_()
    data class Success(val items: List<GenModel_731_>) : GenState_731_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_731_()
    data class Partial(val items: List<GenModel_731_>, val hasMore: Boolean) : GenState_731_()
}

interface GenRepository_731_ {
    suspend fun getAll(): List<GenModel_731_>
    suspend fun getById(id: Long): GenModel_731_?
    suspend fun save(model: GenModel_731_): GenModel_731_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_731_>
}

@Singleton
class GenRepositoryImpl_731_ @Inject constructor() : GenRepository_731_ {
    private val store = mutableMapOf<Long, GenModel_731_>()
    override suspend fun getAll(): List<GenModel_731_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_731_? = store[id]
    override suspend fun save(model: GenModel_731_): GenModel_731_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_731_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_731_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_731_ @Inject constructor(
    private val repository: GenRepositoryImpl_731_
) : GenUseCase_731_<Unit, List<GenModel_731_>> {
    override suspend fun invoke(params: Unit): List<GenModel_731_> = repository.getAll()
}

class GenSaveUseCase_731_ @Inject constructor(
    private val repository: GenRepositoryImpl_731_
) : GenUseCase_731_<GenModel_731_, GenModel_731_> {
    override suspend fun invoke(params: GenModel_731_): GenModel_731_ = repository.save(params)
}

class GenDeleteUseCase_731_ @Inject constructor(
    private val repository: GenRepositoryImpl_731_
) : GenUseCase_731_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_731_ @Inject constructor(
    private val repository: GenRepositoryImpl_731_
) : GenUseCase_731_<String, List<GenModel_731_>> {
    override suspend fun invoke(params: String): List<GenModel_731_> = repository.search(params)
}

abstract class GenMapper_731_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_731_ : GenMapper_731_<GenModel_731_, String>() {
    override fun map(input: GenModel_731_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_731_ : GenMapper_731_<String, GenModel_731_>() {
    override fun map(input: String): GenModel_731_ {
        val parts = input.split(":")
        return GenModel_731_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_731_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_731_,
    private val saveUseCase: GenSaveUseCase_731_,
    private val deleteUseCase: GenDeleteUseCase_731_,
    private val searchUseCase: GenSearchUseCase_731_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_731_>(GenState_731_.Idle)
    val state: StateFlow<GenState_731_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_731_) {
        when (event) {
            is GenEvent_731_.Load -> loadAll()
            is GenEvent_731_.Update -> save(event.model)
            is GenEvent_731_.Delete -> delete(event.id)
            is GenEvent_731_.Refresh -> loadAll()
            is GenEvent_731_.Search -> search(event.query)
            is GenEvent_731_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_731_.Loading; _state.value = GenState_731_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_731_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_731_.Success(searchUseCase(query)) } }
}
