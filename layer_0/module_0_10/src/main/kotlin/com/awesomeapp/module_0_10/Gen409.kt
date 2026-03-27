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

data class GenModel_409_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_409_ {
    data class Load(val id: Long) : GenEvent_409_()
    data class Update(val model: GenModel_409_) : GenEvent_409_()
    data class Delete(val id: Long) : GenEvent_409_()
    data object Refresh : GenEvent_409_()
    data class Search(val query: String) : GenEvent_409_()
    data class Filter(val predicate: String) : GenEvent_409_()
}

sealed class GenState_409_ {
    data object Idle : GenState_409_()
    data object Loading : GenState_409_()
    data class Success(val items: List<GenModel_409_>) : GenState_409_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_409_()
    data class Partial(val items: List<GenModel_409_>, val hasMore: Boolean) : GenState_409_()
}

interface GenRepository_409_ {
    suspend fun getAll(): List<GenModel_409_>
    suspend fun getById(id: Long): GenModel_409_?
    suspend fun save(model: GenModel_409_): GenModel_409_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_409_>
}

@Singleton
class GenRepositoryImpl_409_ @Inject constructor() : GenRepository_409_ {
    private val store = mutableMapOf<Long, GenModel_409_>()
    override suspend fun getAll(): List<GenModel_409_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_409_? = store[id]
    override suspend fun save(model: GenModel_409_): GenModel_409_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_409_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_409_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_409_ @Inject constructor(
    private val repository: GenRepositoryImpl_409_
) : GenUseCase_409_<Unit, List<GenModel_409_>> {
    override suspend fun invoke(params: Unit): List<GenModel_409_> = repository.getAll()
}

class GenSaveUseCase_409_ @Inject constructor(
    private val repository: GenRepositoryImpl_409_
) : GenUseCase_409_<GenModel_409_, GenModel_409_> {
    override suspend fun invoke(params: GenModel_409_): GenModel_409_ = repository.save(params)
}

class GenDeleteUseCase_409_ @Inject constructor(
    private val repository: GenRepositoryImpl_409_
) : GenUseCase_409_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_409_ @Inject constructor(
    private val repository: GenRepositoryImpl_409_
) : GenUseCase_409_<String, List<GenModel_409_>> {
    override suspend fun invoke(params: String): List<GenModel_409_> = repository.search(params)
}

abstract class GenMapper_409_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_409_ : GenMapper_409_<GenModel_409_, String>() {
    override fun map(input: GenModel_409_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_409_ : GenMapper_409_<String, GenModel_409_>() {
    override fun map(input: String): GenModel_409_ {
        val parts = input.split(":")
        return GenModel_409_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_409_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_409_,
    private val saveUseCase: GenSaveUseCase_409_,
    private val deleteUseCase: GenDeleteUseCase_409_,
    private val searchUseCase: GenSearchUseCase_409_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_409_>(GenState_409_.Idle)
    val state: StateFlow<GenState_409_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_409_) {
        when (event) {
            is GenEvent_409_.Load -> loadAll()
            is GenEvent_409_.Update -> save(event.model)
            is GenEvent_409_.Delete -> delete(event.id)
            is GenEvent_409_.Refresh -> loadAll()
            is GenEvent_409_.Search -> search(event.query)
            is GenEvent_409_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_409_.Loading; _state.value = GenState_409_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_409_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_409_.Success(searchUseCase(query)) } }
}
