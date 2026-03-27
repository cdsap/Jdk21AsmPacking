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

data class GenModel_967_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_967_ {
    data class Load(val id: Long) : GenEvent_967_()
    data class Update(val model: GenModel_967_) : GenEvent_967_()
    data class Delete(val id: Long) : GenEvent_967_()
    data object Refresh : GenEvent_967_()
    data class Search(val query: String) : GenEvent_967_()
    data class Filter(val predicate: String) : GenEvent_967_()
}

sealed class GenState_967_ {
    data object Idle : GenState_967_()
    data object Loading : GenState_967_()
    data class Success(val items: List<GenModel_967_>) : GenState_967_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_967_()
    data class Partial(val items: List<GenModel_967_>, val hasMore: Boolean) : GenState_967_()
}

interface GenRepository_967_ {
    suspend fun getAll(): List<GenModel_967_>
    suspend fun getById(id: Long): GenModel_967_?
    suspend fun save(model: GenModel_967_): GenModel_967_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_967_>
}

@Singleton
class GenRepositoryImpl_967_ @Inject constructor() : GenRepository_967_ {
    private val store = mutableMapOf<Long, GenModel_967_>()
    override suspend fun getAll(): List<GenModel_967_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_967_? = store[id]
    override suspend fun save(model: GenModel_967_): GenModel_967_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_967_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_967_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_967_ @Inject constructor(
    private val repository: GenRepositoryImpl_967_
) : GenUseCase_967_<Unit, List<GenModel_967_>> {
    override suspend fun invoke(params: Unit): List<GenModel_967_> = repository.getAll()
}

class GenSaveUseCase_967_ @Inject constructor(
    private val repository: GenRepositoryImpl_967_
) : GenUseCase_967_<GenModel_967_, GenModel_967_> {
    override suspend fun invoke(params: GenModel_967_): GenModel_967_ = repository.save(params)
}

class GenDeleteUseCase_967_ @Inject constructor(
    private val repository: GenRepositoryImpl_967_
) : GenUseCase_967_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_967_ @Inject constructor(
    private val repository: GenRepositoryImpl_967_
) : GenUseCase_967_<String, List<GenModel_967_>> {
    override suspend fun invoke(params: String): List<GenModel_967_> = repository.search(params)
}

abstract class GenMapper_967_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_967_ : GenMapper_967_<GenModel_967_, String>() {
    override fun map(input: GenModel_967_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_967_ : GenMapper_967_<String, GenModel_967_>() {
    override fun map(input: String): GenModel_967_ {
        val parts = input.split(":")
        return GenModel_967_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_967_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_967_,
    private val saveUseCase: GenSaveUseCase_967_,
    private val deleteUseCase: GenDeleteUseCase_967_,
    private val searchUseCase: GenSearchUseCase_967_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_967_>(GenState_967_.Idle)
    val state: StateFlow<GenState_967_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_967_) {
        when (event) {
            is GenEvent_967_.Load -> loadAll()
            is GenEvent_967_.Update -> save(event.model)
            is GenEvent_967_.Delete -> delete(event.id)
            is GenEvent_967_.Refresh -> loadAll()
            is GenEvent_967_.Search -> search(event.query)
            is GenEvent_967_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_967_.Loading; _state.value = GenState_967_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_967_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_967_.Success(searchUseCase(query)) } }
}
