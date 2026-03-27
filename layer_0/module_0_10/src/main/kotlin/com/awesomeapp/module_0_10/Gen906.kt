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

data class GenModel_906_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_906_ {
    data class Load(val id: Long) : GenEvent_906_()
    data class Update(val model: GenModel_906_) : GenEvent_906_()
    data class Delete(val id: Long) : GenEvent_906_()
    data object Refresh : GenEvent_906_()
    data class Search(val query: String) : GenEvent_906_()
    data class Filter(val predicate: String) : GenEvent_906_()
}

sealed class GenState_906_ {
    data object Idle : GenState_906_()
    data object Loading : GenState_906_()
    data class Success(val items: List<GenModel_906_>) : GenState_906_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_906_()
    data class Partial(val items: List<GenModel_906_>, val hasMore: Boolean) : GenState_906_()
}

interface GenRepository_906_ {
    suspend fun getAll(): List<GenModel_906_>
    suspend fun getById(id: Long): GenModel_906_?
    suspend fun save(model: GenModel_906_): GenModel_906_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_906_>
}

@Singleton
class GenRepositoryImpl_906_ @Inject constructor() : GenRepository_906_ {
    private val store = mutableMapOf<Long, GenModel_906_>()
    override suspend fun getAll(): List<GenModel_906_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_906_? = store[id]
    override suspend fun save(model: GenModel_906_): GenModel_906_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_906_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_906_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_906_ @Inject constructor(
    private val repository: GenRepositoryImpl_906_
) : GenUseCase_906_<Unit, List<GenModel_906_>> {
    override suspend fun invoke(params: Unit): List<GenModel_906_> = repository.getAll()
}

class GenSaveUseCase_906_ @Inject constructor(
    private val repository: GenRepositoryImpl_906_
) : GenUseCase_906_<GenModel_906_, GenModel_906_> {
    override suspend fun invoke(params: GenModel_906_): GenModel_906_ = repository.save(params)
}

class GenDeleteUseCase_906_ @Inject constructor(
    private val repository: GenRepositoryImpl_906_
) : GenUseCase_906_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_906_ @Inject constructor(
    private val repository: GenRepositoryImpl_906_
) : GenUseCase_906_<String, List<GenModel_906_>> {
    override suspend fun invoke(params: String): List<GenModel_906_> = repository.search(params)
}

abstract class GenMapper_906_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_906_ : GenMapper_906_<GenModel_906_, String>() {
    override fun map(input: GenModel_906_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_906_ : GenMapper_906_<String, GenModel_906_>() {
    override fun map(input: String): GenModel_906_ {
        val parts = input.split(":")
        return GenModel_906_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_906_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_906_,
    private val saveUseCase: GenSaveUseCase_906_,
    private val deleteUseCase: GenDeleteUseCase_906_,
    private val searchUseCase: GenSearchUseCase_906_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_906_>(GenState_906_.Idle)
    val state: StateFlow<GenState_906_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_906_) {
        when (event) {
            is GenEvent_906_.Load -> loadAll()
            is GenEvent_906_.Update -> save(event.model)
            is GenEvent_906_.Delete -> delete(event.id)
            is GenEvent_906_.Refresh -> loadAll()
            is GenEvent_906_.Search -> search(event.query)
            is GenEvent_906_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_906_.Loading; _state.value = GenState_906_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_906_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_906_.Success(searchUseCase(query)) } }
}
