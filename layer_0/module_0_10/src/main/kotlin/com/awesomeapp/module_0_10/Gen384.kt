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

data class GenModel_384_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_384_ {
    data class Load(val id: Long) : GenEvent_384_()
    data class Update(val model: GenModel_384_) : GenEvent_384_()
    data class Delete(val id: Long) : GenEvent_384_()
    data object Refresh : GenEvent_384_()
    data class Search(val query: String) : GenEvent_384_()
    data class Filter(val predicate: String) : GenEvent_384_()
}

sealed class GenState_384_ {
    data object Idle : GenState_384_()
    data object Loading : GenState_384_()
    data class Success(val items: List<GenModel_384_>) : GenState_384_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_384_()
    data class Partial(val items: List<GenModel_384_>, val hasMore: Boolean) : GenState_384_()
}

interface GenRepository_384_ {
    suspend fun getAll(): List<GenModel_384_>
    suspend fun getById(id: Long): GenModel_384_?
    suspend fun save(model: GenModel_384_): GenModel_384_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_384_>
}

@Singleton
class GenRepositoryImpl_384_ @Inject constructor() : GenRepository_384_ {
    private val store = mutableMapOf<Long, GenModel_384_>()
    override suspend fun getAll(): List<GenModel_384_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_384_? = store[id]
    override suspend fun save(model: GenModel_384_): GenModel_384_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_384_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_384_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_384_ @Inject constructor(
    private val repository: GenRepositoryImpl_384_
) : GenUseCase_384_<Unit, List<GenModel_384_>> {
    override suspend fun invoke(params: Unit): List<GenModel_384_> = repository.getAll()
}

class GenSaveUseCase_384_ @Inject constructor(
    private val repository: GenRepositoryImpl_384_
) : GenUseCase_384_<GenModel_384_, GenModel_384_> {
    override suspend fun invoke(params: GenModel_384_): GenModel_384_ = repository.save(params)
}

class GenDeleteUseCase_384_ @Inject constructor(
    private val repository: GenRepositoryImpl_384_
) : GenUseCase_384_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_384_ @Inject constructor(
    private val repository: GenRepositoryImpl_384_
) : GenUseCase_384_<String, List<GenModel_384_>> {
    override suspend fun invoke(params: String): List<GenModel_384_> = repository.search(params)
}

abstract class GenMapper_384_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_384_ : GenMapper_384_<GenModel_384_, String>() {
    override fun map(input: GenModel_384_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_384_ : GenMapper_384_<String, GenModel_384_>() {
    override fun map(input: String): GenModel_384_ {
        val parts = input.split(":")
        return GenModel_384_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_384_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_384_,
    private val saveUseCase: GenSaveUseCase_384_,
    private val deleteUseCase: GenDeleteUseCase_384_,
    private val searchUseCase: GenSearchUseCase_384_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_384_>(GenState_384_.Idle)
    val state: StateFlow<GenState_384_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_384_) {
        when (event) {
            is GenEvent_384_.Load -> loadAll()
            is GenEvent_384_.Update -> save(event.model)
            is GenEvent_384_.Delete -> delete(event.id)
            is GenEvent_384_.Refresh -> loadAll()
            is GenEvent_384_.Search -> search(event.query)
            is GenEvent_384_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_384_.Loading; _state.value = GenState_384_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_384_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_384_.Success(searchUseCase(query)) } }
}
