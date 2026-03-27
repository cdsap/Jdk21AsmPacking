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

data class GenModel_880_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_880_ {
    data class Load(val id: Long) : GenEvent_880_()
    data class Update(val model: GenModel_880_) : GenEvent_880_()
    data class Delete(val id: Long) : GenEvent_880_()
    data object Refresh : GenEvent_880_()
    data class Search(val query: String) : GenEvent_880_()
    data class Filter(val predicate: String) : GenEvent_880_()
}

sealed class GenState_880_ {
    data object Idle : GenState_880_()
    data object Loading : GenState_880_()
    data class Success(val items: List<GenModel_880_>) : GenState_880_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_880_()
    data class Partial(val items: List<GenModel_880_>, val hasMore: Boolean) : GenState_880_()
}

interface GenRepository_880_ {
    suspend fun getAll(): List<GenModel_880_>
    suspend fun getById(id: Long): GenModel_880_?
    suspend fun save(model: GenModel_880_): GenModel_880_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_880_>
}

@Singleton
class GenRepositoryImpl_880_ @Inject constructor() : GenRepository_880_ {
    private val store = mutableMapOf<Long, GenModel_880_>()
    override suspend fun getAll(): List<GenModel_880_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_880_? = store[id]
    override suspend fun save(model: GenModel_880_): GenModel_880_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_880_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_880_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_880_ @Inject constructor(
    private val repository: GenRepositoryImpl_880_
) : GenUseCase_880_<Unit, List<GenModel_880_>> {
    override suspend fun invoke(params: Unit): List<GenModel_880_> = repository.getAll()
}

class GenSaveUseCase_880_ @Inject constructor(
    private val repository: GenRepositoryImpl_880_
) : GenUseCase_880_<GenModel_880_, GenModel_880_> {
    override suspend fun invoke(params: GenModel_880_): GenModel_880_ = repository.save(params)
}

class GenDeleteUseCase_880_ @Inject constructor(
    private val repository: GenRepositoryImpl_880_
) : GenUseCase_880_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_880_ @Inject constructor(
    private val repository: GenRepositoryImpl_880_
) : GenUseCase_880_<String, List<GenModel_880_>> {
    override suspend fun invoke(params: String): List<GenModel_880_> = repository.search(params)
}

abstract class GenMapper_880_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_880_ : GenMapper_880_<GenModel_880_, String>() {
    override fun map(input: GenModel_880_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_880_ : GenMapper_880_<String, GenModel_880_>() {
    override fun map(input: String): GenModel_880_ {
        val parts = input.split(":")
        return GenModel_880_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_880_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_880_,
    private val saveUseCase: GenSaveUseCase_880_,
    private val deleteUseCase: GenDeleteUseCase_880_,
    private val searchUseCase: GenSearchUseCase_880_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_880_>(GenState_880_.Idle)
    val state: StateFlow<GenState_880_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_880_) {
        when (event) {
            is GenEvent_880_.Load -> loadAll()
            is GenEvent_880_.Update -> save(event.model)
            is GenEvent_880_.Delete -> delete(event.id)
            is GenEvent_880_.Refresh -> loadAll()
            is GenEvent_880_.Search -> search(event.query)
            is GenEvent_880_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_880_.Loading; _state.value = GenState_880_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_880_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_880_.Success(searchUseCase(query)) } }
}
