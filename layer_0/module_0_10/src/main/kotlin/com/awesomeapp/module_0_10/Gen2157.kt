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

data class GenModel_2157_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2157_ {
    data class Load(val id: Long) : GenEvent_2157_()
    data class Update(val model: GenModel_2157_) : GenEvent_2157_()
    data class Delete(val id: Long) : GenEvent_2157_()
    data object Refresh : GenEvent_2157_()
    data class Search(val query: String) : GenEvent_2157_()
    data class Filter(val predicate: String) : GenEvent_2157_()
}

sealed class GenState_2157_ {
    data object Idle : GenState_2157_()
    data object Loading : GenState_2157_()
    data class Success(val items: List<GenModel_2157_>) : GenState_2157_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2157_()
    data class Partial(val items: List<GenModel_2157_>, val hasMore: Boolean) : GenState_2157_()
}

interface GenRepository_2157_ {
    suspend fun getAll(): List<GenModel_2157_>
    suspend fun getById(id: Long): GenModel_2157_?
    suspend fun save(model: GenModel_2157_): GenModel_2157_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2157_>
}

@Singleton
class GenRepositoryImpl_2157_ @Inject constructor() : GenRepository_2157_ {
    private val store = mutableMapOf<Long, GenModel_2157_>()
    override suspend fun getAll(): List<GenModel_2157_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2157_? = store[id]
    override suspend fun save(model: GenModel_2157_): GenModel_2157_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2157_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2157_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2157_ @Inject constructor(
    private val repository: GenRepositoryImpl_2157_
) : GenUseCase_2157_<Unit, List<GenModel_2157_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2157_> = repository.getAll()
}

class GenSaveUseCase_2157_ @Inject constructor(
    private val repository: GenRepositoryImpl_2157_
) : GenUseCase_2157_<GenModel_2157_, GenModel_2157_> {
    override suspend fun invoke(params: GenModel_2157_): GenModel_2157_ = repository.save(params)
}

class GenDeleteUseCase_2157_ @Inject constructor(
    private val repository: GenRepositoryImpl_2157_
) : GenUseCase_2157_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2157_ @Inject constructor(
    private val repository: GenRepositoryImpl_2157_
) : GenUseCase_2157_<String, List<GenModel_2157_>> {
    override suspend fun invoke(params: String): List<GenModel_2157_> = repository.search(params)
}

abstract class GenMapper_2157_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2157_ : GenMapper_2157_<GenModel_2157_, String>() {
    override fun map(input: GenModel_2157_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2157_ : GenMapper_2157_<String, GenModel_2157_>() {
    override fun map(input: String): GenModel_2157_ {
        val parts = input.split(":")
        return GenModel_2157_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2157_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2157_,
    private val saveUseCase: GenSaveUseCase_2157_,
    private val deleteUseCase: GenDeleteUseCase_2157_,
    private val searchUseCase: GenSearchUseCase_2157_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2157_>(GenState_2157_.Idle)
    val state: StateFlow<GenState_2157_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2157_) {
        when (event) {
            is GenEvent_2157_.Load -> loadAll()
            is GenEvent_2157_.Update -> save(event.model)
            is GenEvent_2157_.Delete -> delete(event.id)
            is GenEvent_2157_.Refresh -> loadAll()
            is GenEvent_2157_.Search -> search(event.query)
            is GenEvent_2157_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2157_.Loading; _state.value = GenState_2157_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2157_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2157_.Success(searchUseCase(query)) } }
}
