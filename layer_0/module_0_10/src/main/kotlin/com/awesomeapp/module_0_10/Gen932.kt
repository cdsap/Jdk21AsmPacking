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

data class GenModel_932_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_932_ {
    data class Load(val id: Long) : GenEvent_932_()
    data class Update(val model: GenModel_932_) : GenEvent_932_()
    data class Delete(val id: Long) : GenEvent_932_()
    data object Refresh : GenEvent_932_()
    data class Search(val query: String) : GenEvent_932_()
    data class Filter(val predicate: String) : GenEvent_932_()
}

sealed class GenState_932_ {
    data object Idle : GenState_932_()
    data object Loading : GenState_932_()
    data class Success(val items: List<GenModel_932_>) : GenState_932_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_932_()
    data class Partial(val items: List<GenModel_932_>, val hasMore: Boolean) : GenState_932_()
}

interface GenRepository_932_ {
    suspend fun getAll(): List<GenModel_932_>
    suspend fun getById(id: Long): GenModel_932_?
    suspend fun save(model: GenModel_932_): GenModel_932_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_932_>
}

@Singleton
class GenRepositoryImpl_932_ @Inject constructor() : GenRepository_932_ {
    private val store = mutableMapOf<Long, GenModel_932_>()
    override suspend fun getAll(): List<GenModel_932_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_932_? = store[id]
    override suspend fun save(model: GenModel_932_): GenModel_932_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_932_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_932_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_932_ @Inject constructor(
    private val repository: GenRepositoryImpl_932_
) : GenUseCase_932_<Unit, List<GenModel_932_>> {
    override suspend fun invoke(params: Unit): List<GenModel_932_> = repository.getAll()
}

class GenSaveUseCase_932_ @Inject constructor(
    private val repository: GenRepositoryImpl_932_
) : GenUseCase_932_<GenModel_932_, GenModel_932_> {
    override suspend fun invoke(params: GenModel_932_): GenModel_932_ = repository.save(params)
}

class GenDeleteUseCase_932_ @Inject constructor(
    private val repository: GenRepositoryImpl_932_
) : GenUseCase_932_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_932_ @Inject constructor(
    private val repository: GenRepositoryImpl_932_
) : GenUseCase_932_<String, List<GenModel_932_>> {
    override suspend fun invoke(params: String): List<GenModel_932_> = repository.search(params)
}

abstract class GenMapper_932_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_932_ : GenMapper_932_<GenModel_932_, String>() {
    override fun map(input: GenModel_932_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_932_ : GenMapper_932_<String, GenModel_932_>() {
    override fun map(input: String): GenModel_932_ {
        val parts = input.split(":")
        return GenModel_932_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_932_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_932_,
    private val saveUseCase: GenSaveUseCase_932_,
    private val deleteUseCase: GenDeleteUseCase_932_,
    private val searchUseCase: GenSearchUseCase_932_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_932_>(GenState_932_.Idle)
    val state: StateFlow<GenState_932_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_932_) {
        when (event) {
            is GenEvent_932_.Load -> loadAll()
            is GenEvent_932_.Update -> save(event.model)
            is GenEvent_932_.Delete -> delete(event.id)
            is GenEvent_932_.Refresh -> loadAll()
            is GenEvent_932_.Search -> search(event.query)
            is GenEvent_932_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_932_.Loading; _state.value = GenState_932_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_932_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_932_.Success(searchUseCase(query)) } }
}
