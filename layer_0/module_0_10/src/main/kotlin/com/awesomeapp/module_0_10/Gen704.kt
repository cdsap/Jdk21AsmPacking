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

data class GenModel_704_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_704_ {
    data class Load(val id: Long) : GenEvent_704_()
    data class Update(val model: GenModel_704_) : GenEvent_704_()
    data class Delete(val id: Long) : GenEvent_704_()
    data object Refresh : GenEvent_704_()
    data class Search(val query: String) : GenEvent_704_()
    data class Filter(val predicate: String) : GenEvent_704_()
}

sealed class GenState_704_ {
    data object Idle : GenState_704_()
    data object Loading : GenState_704_()
    data class Success(val items: List<GenModel_704_>) : GenState_704_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_704_()
    data class Partial(val items: List<GenModel_704_>, val hasMore: Boolean) : GenState_704_()
}

interface GenRepository_704_ {
    suspend fun getAll(): List<GenModel_704_>
    suspend fun getById(id: Long): GenModel_704_?
    suspend fun save(model: GenModel_704_): GenModel_704_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_704_>
}

@Singleton
class GenRepositoryImpl_704_ @Inject constructor() : GenRepository_704_ {
    private val store = mutableMapOf<Long, GenModel_704_>()
    override suspend fun getAll(): List<GenModel_704_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_704_? = store[id]
    override suspend fun save(model: GenModel_704_): GenModel_704_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_704_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_704_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_704_ @Inject constructor(
    private val repository: GenRepositoryImpl_704_
) : GenUseCase_704_<Unit, List<GenModel_704_>> {
    override suspend fun invoke(params: Unit): List<GenModel_704_> = repository.getAll()
}

class GenSaveUseCase_704_ @Inject constructor(
    private val repository: GenRepositoryImpl_704_
) : GenUseCase_704_<GenModel_704_, GenModel_704_> {
    override suspend fun invoke(params: GenModel_704_): GenModel_704_ = repository.save(params)
}

class GenDeleteUseCase_704_ @Inject constructor(
    private val repository: GenRepositoryImpl_704_
) : GenUseCase_704_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_704_ @Inject constructor(
    private val repository: GenRepositoryImpl_704_
) : GenUseCase_704_<String, List<GenModel_704_>> {
    override suspend fun invoke(params: String): List<GenModel_704_> = repository.search(params)
}

abstract class GenMapper_704_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_704_ : GenMapper_704_<GenModel_704_, String>() {
    override fun map(input: GenModel_704_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_704_ : GenMapper_704_<String, GenModel_704_>() {
    override fun map(input: String): GenModel_704_ {
        val parts = input.split(":")
        return GenModel_704_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_704_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_704_,
    private val saveUseCase: GenSaveUseCase_704_,
    private val deleteUseCase: GenDeleteUseCase_704_,
    private val searchUseCase: GenSearchUseCase_704_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_704_>(GenState_704_.Idle)
    val state: StateFlow<GenState_704_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_704_) {
        when (event) {
            is GenEvent_704_.Load -> loadAll()
            is GenEvent_704_.Update -> save(event.model)
            is GenEvent_704_.Delete -> delete(event.id)
            is GenEvent_704_.Refresh -> loadAll()
            is GenEvent_704_.Search -> search(event.query)
            is GenEvent_704_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_704_.Loading; _state.value = GenState_704_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_704_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_704_.Success(searchUseCase(query)) } }
}
