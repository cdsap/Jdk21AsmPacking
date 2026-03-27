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

data class GenModel_486_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_486_ {
    data class Load(val id: Long) : GenEvent_486_()
    data class Update(val model: GenModel_486_) : GenEvent_486_()
    data class Delete(val id: Long) : GenEvent_486_()
    data object Refresh : GenEvent_486_()
    data class Search(val query: String) : GenEvent_486_()
    data class Filter(val predicate: String) : GenEvent_486_()
}

sealed class GenState_486_ {
    data object Idle : GenState_486_()
    data object Loading : GenState_486_()
    data class Success(val items: List<GenModel_486_>) : GenState_486_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_486_()
    data class Partial(val items: List<GenModel_486_>, val hasMore: Boolean) : GenState_486_()
}

interface GenRepository_486_ {
    suspend fun getAll(): List<GenModel_486_>
    suspend fun getById(id: Long): GenModel_486_?
    suspend fun save(model: GenModel_486_): GenModel_486_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_486_>
}

@Singleton
class GenRepositoryImpl_486_ @Inject constructor() : GenRepository_486_ {
    private val store = mutableMapOf<Long, GenModel_486_>()
    override suspend fun getAll(): List<GenModel_486_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_486_? = store[id]
    override suspend fun save(model: GenModel_486_): GenModel_486_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_486_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_486_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_486_ @Inject constructor(
    private val repository: GenRepositoryImpl_486_
) : GenUseCase_486_<Unit, List<GenModel_486_>> {
    override suspend fun invoke(params: Unit): List<GenModel_486_> = repository.getAll()
}

class GenSaveUseCase_486_ @Inject constructor(
    private val repository: GenRepositoryImpl_486_
) : GenUseCase_486_<GenModel_486_, GenModel_486_> {
    override suspend fun invoke(params: GenModel_486_): GenModel_486_ = repository.save(params)
}

class GenDeleteUseCase_486_ @Inject constructor(
    private val repository: GenRepositoryImpl_486_
) : GenUseCase_486_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_486_ @Inject constructor(
    private val repository: GenRepositoryImpl_486_
) : GenUseCase_486_<String, List<GenModel_486_>> {
    override suspend fun invoke(params: String): List<GenModel_486_> = repository.search(params)
}

abstract class GenMapper_486_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_486_ : GenMapper_486_<GenModel_486_, String>() {
    override fun map(input: GenModel_486_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_486_ : GenMapper_486_<String, GenModel_486_>() {
    override fun map(input: String): GenModel_486_ {
        val parts = input.split(":")
        return GenModel_486_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_486_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_486_,
    private val saveUseCase: GenSaveUseCase_486_,
    private val deleteUseCase: GenDeleteUseCase_486_,
    private val searchUseCase: GenSearchUseCase_486_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_486_>(GenState_486_.Idle)
    val state: StateFlow<GenState_486_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_486_) {
        when (event) {
            is GenEvent_486_.Load -> loadAll()
            is GenEvent_486_.Update -> save(event.model)
            is GenEvent_486_.Delete -> delete(event.id)
            is GenEvent_486_.Refresh -> loadAll()
            is GenEvent_486_.Search -> search(event.query)
            is GenEvent_486_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_486_.Loading; _state.value = GenState_486_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_486_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_486_.Success(searchUseCase(query)) } }
}
