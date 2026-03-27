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

data class GenModel_156_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_156_ {
    data class Load(val id: Long) : GenEvent_156_()
    data class Update(val model: GenModel_156_) : GenEvent_156_()
    data class Delete(val id: Long) : GenEvent_156_()
    data object Refresh : GenEvent_156_()
    data class Search(val query: String) : GenEvent_156_()
    data class Filter(val predicate: String) : GenEvent_156_()
}

sealed class GenState_156_ {
    data object Idle : GenState_156_()
    data object Loading : GenState_156_()
    data class Success(val items: List<GenModel_156_>) : GenState_156_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_156_()
    data class Partial(val items: List<GenModel_156_>, val hasMore: Boolean) : GenState_156_()
}

interface GenRepository_156_ {
    suspend fun getAll(): List<GenModel_156_>
    suspend fun getById(id: Long): GenModel_156_?
    suspend fun save(model: GenModel_156_): GenModel_156_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_156_>
}

@Singleton
class GenRepositoryImpl_156_ @Inject constructor() : GenRepository_156_ {
    private val store = mutableMapOf<Long, GenModel_156_>()
    override suspend fun getAll(): List<GenModel_156_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_156_? = store[id]
    override suspend fun save(model: GenModel_156_): GenModel_156_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_156_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_156_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_156_ @Inject constructor(
    private val repository: GenRepositoryImpl_156_
) : GenUseCase_156_<Unit, List<GenModel_156_>> {
    override suspend fun invoke(params: Unit): List<GenModel_156_> = repository.getAll()
}

class GenSaveUseCase_156_ @Inject constructor(
    private val repository: GenRepositoryImpl_156_
) : GenUseCase_156_<GenModel_156_, GenModel_156_> {
    override suspend fun invoke(params: GenModel_156_): GenModel_156_ = repository.save(params)
}

class GenDeleteUseCase_156_ @Inject constructor(
    private val repository: GenRepositoryImpl_156_
) : GenUseCase_156_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_156_ @Inject constructor(
    private val repository: GenRepositoryImpl_156_
) : GenUseCase_156_<String, List<GenModel_156_>> {
    override suspend fun invoke(params: String): List<GenModel_156_> = repository.search(params)
}

abstract class GenMapper_156_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_156_ : GenMapper_156_<GenModel_156_, String>() {
    override fun map(input: GenModel_156_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_156_ : GenMapper_156_<String, GenModel_156_>() {
    override fun map(input: String): GenModel_156_ {
        val parts = input.split(":")
        return GenModel_156_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_156_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_156_,
    private val saveUseCase: GenSaveUseCase_156_,
    private val deleteUseCase: GenDeleteUseCase_156_,
    private val searchUseCase: GenSearchUseCase_156_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_156_>(GenState_156_.Idle)
    val state: StateFlow<GenState_156_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_156_) {
        when (event) {
            is GenEvent_156_.Load -> loadAll()
            is GenEvent_156_.Update -> save(event.model)
            is GenEvent_156_.Delete -> delete(event.id)
            is GenEvent_156_.Refresh -> loadAll()
            is GenEvent_156_.Search -> search(event.query)
            is GenEvent_156_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_156_.Loading; _state.value = GenState_156_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_156_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_156_.Success(searchUseCase(query)) } }
}
