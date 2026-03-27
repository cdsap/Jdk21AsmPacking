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

data class GenModel_1287_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1287_ {
    data class Load(val id: Long) : GenEvent_1287_()
    data class Update(val model: GenModel_1287_) : GenEvent_1287_()
    data class Delete(val id: Long) : GenEvent_1287_()
    data object Refresh : GenEvent_1287_()
    data class Search(val query: String) : GenEvent_1287_()
    data class Filter(val predicate: String) : GenEvent_1287_()
}

sealed class GenState_1287_ {
    data object Idle : GenState_1287_()
    data object Loading : GenState_1287_()
    data class Success(val items: List<GenModel_1287_>) : GenState_1287_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1287_()
    data class Partial(val items: List<GenModel_1287_>, val hasMore: Boolean) : GenState_1287_()
}

interface GenRepository_1287_ {
    suspend fun getAll(): List<GenModel_1287_>
    suspend fun getById(id: Long): GenModel_1287_?
    suspend fun save(model: GenModel_1287_): GenModel_1287_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1287_>
}

@Singleton
class GenRepositoryImpl_1287_ @Inject constructor() : GenRepository_1287_ {
    private val store = mutableMapOf<Long, GenModel_1287_>()
    override suspend fun getAll(): List<GenModel_1287_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1287_? = store[id]
    override suspend fun save(model: GenModel_1287_): GenModel_1287_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1287_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1287_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1287_ @Inject constructor(
    private val repository: GenRepositoryImpl_1287_
) : GenUseCase_1287_<Unit, List<GenModel_1287_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1287_> = repository.getAll()
}

class GenSaveUseCase_1287_ @Inject constructor(
    private val repository: GenRepositoryImpl_1287_
) : GenUseCase_1287_<GenModel_1287_, GenModel_1287_> {
    override suspend fun invoke(params: GenModel_1287_): GenModel_1287_ = repository.save(params)
}

class GenDeleteUseCase_1287_ @Inject constructor(
    private val repository: GenRepositoryImpl_1287_
) : GenUseCase_1287_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1287_ @Inject constructor(
    private val repository: GenRepositoryImpl_1287_
) : GenUseCase_1287_<String, List<GenModel_1287_>> {
    override suspend fun invoke(params: String): List<GenModel_1287_> = repository.search(params)
}

abstract class GenMapper_1287_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1287_ : GenMapper_1287_<GenModel_1287_, String>() {
    override fun map(input: GenModel_1287_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1287_ : GenMapper_1287_<String, GenModel_1287_>() {
    override fun map(input: String): GenModel_1287_ {
        val parts = input.split(":")
        return GenModel_1287_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1287_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1287_,
    private val saveUseCase: GenSaveUseCase_1287_,
    private val deleteUseCase: GenDeleteUseCase_1287_,
    private val searchUseCase: GenSearchUseCase_1287_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1287_>(GenState_1287_.Idle)
    val state: StateFlow<GenState_1287_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1287_) {
        when (event) {
            is GenEvent_1287_.Load -> loadAll()
            is GenEvent_1287_.Update -> save(event.model)
            is GenEvent_1287_.Delete -> delete(event.id)
            is GenEvent_1287_.Refresh -> loadAll()
            is GenEvent_1287_.Search -> search(event.query)
            is GenEvent_1287_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1287_.Loading; _state.value = GenState_1287_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1287_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1287_.Success(searchUseCase(query)) } }
}
