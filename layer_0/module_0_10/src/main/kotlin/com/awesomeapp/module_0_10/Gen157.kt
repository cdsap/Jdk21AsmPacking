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

data class GenModel_157_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_157_ {
    data class Load(val id: Long) : GenEvent_157_()
    data class Update(val model: GenModel_157_) : GenEvent_157_()
    data class Delete(val id: Long) : GenEvent_157_()
    data object Refresh : GenEvent_157_()
    data class Search(val query: String) : GenEvent_157_()
    data class Filter(val predicate: String) : GenEvent_157_()
}

sealed class GenState_157_ {
    data object Idle : GenState_157_()
    data object Loading : GenState_157_()
    data class Success(val items: List<GenModel_157_>) : GenState_157_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_157_()
    data class Partial(val items: List<GenModel_157_>, val hasMore: Boolean) : GenState_157_()
}

interface GenRepository_157_ {
    suspend fun getAll(): List<GenModel_157_>
    suspend fun getById(id: Long): GenModel_157_?
    suspend fun save(model: GenModel_157_): GenModel_157_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_157_>
}

@Singleton
class GenRepositoryImpl_157_ @Inject constructor() : GenRepository_157_ {
    private val store = mutableMapOf<Long, GenModel_157_>()
    override suspend fun getAll(): List<GenModel_157_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_157_? = store[id]
    override suspend fun save(model: GenModel_157_): GenModel_157_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_157_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_157_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_157_ @Inject constructor(
    private val repository: GenRepositoryImpl_157_
) : GenUseCase_157_<Unit, List<GenModel_157_>> {
    override suspend fun invoke(params: Unit): List<GenModel_157_> = repository.getAll()
}

class GenSaveUseCase_157_ @Inject constructor(
    private val repository: GenRepositoryImpl_157_
) : GenUseCase_157_<GenModel_157_, GenModel_157_> {
    override suspend fun invoke(params: GenModel_157_): GenModel_157_ = repository.save(params)
}

class GenDeleteUseCase_157_ @Inject constructor(
    private val repository: GenRepositoryImpl_157_
) : GenUseCase_157_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_157_ @Inject constructor(
    private val repository: GenRepositoryImpl_157_
) : GenUseCase_157_<String, List<GenModel_157_>> {
    override suspend fun invoke(params: String): List<GenModel_157_> = repository.search(params)
}

abstract class GenMapper_157_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_157_ : GenMapper_157_<GenModel_157_, String>() {
    override fun map(input: GenModel_157_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_157_ : GenMapper_157_<String, GenModel_157_>() {
    override fun map(input: String): GenModel_157_ {
        val parts = input.split(":")
        return GenModel_157_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_157_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_157_,
    private val saveUseCase: GenSaveUseCase_157_,
    private val deleteUseCase: GenDeleteUseCase_157_,
    private val searchUseCase: GenSearchUseCase_157_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_157_>(GenState_157_.Idle)
    val state: StateFlow<GenState_157_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_157_) {
        when (event) {
            is GenEvent_157_.Load -> loadAll()
            is GenEvent_157_.Update -> save(event.model)
            is GenEvent_157_.Delete -> delete(event.id)
            is GenEvent_157_.Refresh -> loadAll()
            is GenEvent_157_.Search -> search(event.query)
            is GenEvent_157_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_157_.Loading; _state.value = GenState_157_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_157_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_157_.Success(searchUseCase(query)) } }
}
