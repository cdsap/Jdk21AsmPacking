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

data class GenModel_503_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_503_ {
    data class Load(val id: Long) : GenEvent_503_()
    data class Update(val model: GenModel_503_) : GenEvent_503_()
    data class Delete(val id: Long) : GenEvent_503_()
    data object Refresh : GenEvent_503_()
    data class Search(val query: String) : GenEvent_503_()
    data class Filter(val predicate: String) : GenEvent_503_()
}

sealed class GenState_503_ {
    data object Idle : GenState_503_()
    data object Loading : GenState_503_()
    data class Success(val items: List<GenModel_503_>) : GenState_503_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_503_()
    data class Partial(val items: List<GenModel_503_>, val hasMore: Boolean) : GenState_503_()
}

interface GenRepository_503_ {
    suspend fun getAll(): List<GenModel_503_>
    suspend fun getById(id: Long): GenModel_503_?
    suspend fun save(model: GenModel_503_): GenModel_503_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_503_>
}

@Singleton
class GenRepositoryImpl_503_ @Inject constructor() : GenRepository_503_ {
    private val store = mutableMapOf<Long, GenModel_503_>()
    override suspend fun getAll(): List<GenModel_503_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_503_? = store[id]
    override suspend fun save(model: GenModel_503_): GenModel_503_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_503_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_503_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_503_ @Inject constructor(
    private val repository: GenRepositoryImpl_503_
) : GenUseCase_503_<Unit, List<GenModel_503_>> {
    override suspend fun invoke(params: Unit): List<GenModel_503_> = repository.getAll()
}

class GenSaveUseCase_503_ @Inject constructor(
    private val repository: GenRepositoryImpl_503_
) : GenUseCase_503_<GenModel_503_, GenModel_503_> {
    override suspend fun invoke(params: GenModel_503_): GenModel_503_ = repository.save(params)
}

class GenDeleteUseCase_503_ @Inject constructor(
    private val repository: GenRepositoryImpl_503_
) : GenUseCase_503_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_503_ @Inject constructor(
    private val repository: GenRepositoryImpl_503_
) : GenUseCase_503_<String, List<GenModel_503_>> {
    override suspend fun invoke(params: String): List<GenModel_503_> = repository.search(params)
}

abstract class GenMapper_503_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_503_ : GenMapper_503_<GenModel_503_, String>() {
    override fun map(input: GenModel_503_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_503_ : GenMapper_503_<String, GenModel_503_>() {
    override fun map(input: String): GenModel_503_ {
        val parts = input.split(":")
        return GenModel_503_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_503_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_503_,
    private val saveUseCase: GenSaveUseCase_503_,
    private val deleteUseCase: GenDeleteUseCase_503_,
    private val searchUseCase: GenSearchUseCase_503_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_503_>(GenState_503_.Idle)
    val state: StateFlow<GenState_503_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_503_) {
        when (event) {
            is GenEvent_503_.Load -> loadAll()
            is GenEvent_503_.Update -> save(event.model)
            is GenEvent_503_.Delete -> delete(event.id)
            is GenEvent_503_.Refresh -> loadAll()
            is GenEvent_503_.Search -> search(event.query)
            is GenEvent_503_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_503_.Loading; _state.value = GenState_503_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_503_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_503_.Success(searchUseCase(query)) } }
}
