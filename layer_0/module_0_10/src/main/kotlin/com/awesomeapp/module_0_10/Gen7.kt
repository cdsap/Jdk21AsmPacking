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

data class GenModel_7_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_7_ {
    data class Load(val id: Long) : GenEvent_7_()
    data class Update(val model: GenModel_7_) : GenEvent_7_()
    data class Delete(val id: Long) : GenEvent_7_()
    data object Refresh : GenEvent_7_()
    data class Search(val query: String) : GenEvent_7_()
    data class Filter(val predicate: String) : GenEvent_7_()
}

sealed class GenState_7_ {
    data object Idle : GenState_7_()
    data object Loading : GenState_7_()
    data class Success(val items: List<GenModel_7_>) : GenState_7_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_7_()
    data class Partial(val items: List<GenModel_7_>, val hasMore: Boolean) : GenState_7_()
}

interface GenRepository_7_ {
    suspend fun getAll(): List<GenModel_7_>
    suspend fun getById(id: Long): GenModel_7_?
    suspend fun save(model: GenModel_7_): GenModel_7_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_7_>
}

@Singleton
class GenRepositoryImpl_7_ @Inject constructor() : GenRepository_7_ {
    private val store = mutableMapOf<Long, GenModel_7_>()
    override suspend fun getAll(): List<GenModel_7_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_7_? = store[id]
    override suspend fun save(model: GenModel_7_): GenModel_7_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_7_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_7_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_7_ @Inject constructor(
    private val repository: GenRepositoryImpl_7_
) : GenUseCase_7_<Unit, List<GenModel_7_>> {
    override suspend fun invoke(params: Unit): List<GenModel_7_> = repository.getAll()
}

class GenSaveUseCase_7_ @Inject constructor(
    private val repository: GenRepositoryImpl_7_
) : GenUseCase_7_<GenModel_7_, GenModel_7_> {
    override suspend fun invoke(params: GenModel_7_): GenModel_7_ = repository.save(params)
}

class GenDeleteUseCase_7_ @Inject constructor(
    private val repository: GenRepositoryImpl_7_
) : GenUseCase_7_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_7_ @Inject constructor(
    private val repository: GenRepositoryImpl_7_
) : GenUseCase_7_<String, List<GenModel_7_>> {
    override suspend fun invoke(params: String): List<GenModel_7_> = repository.search(params)
}

abstract class GenMapper_7_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_7_ : GenMapper_7_<GenModel_7_, String>() {
    override fun map(input: GenModel_7_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_7_ : GenMapper_7_<String, GenModel_7_>() {
    override fun map(input: String): GenModel_7_ {
        val parts = input.split(":")
        return GenModel_7_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_7_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_7_,
    private val saveUseCase: GenSaveUseCase_7_,
    private val deleteUseCase: GenDeleteUseCase_7_,
    private val searchUseCase: GenSearchUseCase_7_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_7_>(GenState_7_.Idle)
    val state: StateFlow<GenState_7_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_7_) {
        when (event) {
            is GenEvent_7_.Load -> loadAll()
            is GenEvent_7_.Update -> save(event.model)
            is GenEvent_7_.Delete -> delete(event.id)
            is GenEvent_7_.Refresh -> loadAll()
            is GenEvent_7_.Search -> search(event.query)
            is GenEvent_7_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_7_.Loading; _state.value = GenState_7_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_7_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_7_.Success(searchUseCase(query)) } }
}
