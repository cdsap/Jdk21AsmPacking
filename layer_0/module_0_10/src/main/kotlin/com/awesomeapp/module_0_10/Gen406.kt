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

data class GenModel_406_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_406_ {
    data class Load(val id: Long) : GenEvent_406_()
    data class Update(val model: GenModel_406_) : GenEvent_406_()
    data class Delete(val id: Long) : GenEvent_406_()
    data object Refresh : GenEvent_406_()
    data class Search(val query: String) : GenEvent_406_()
    data class Filter(val predicate: String) : GenEvent_406_()
}

sealed class GenState_406_ {
    data object Idle : GenState_406_()
    data object Loading : GenState_406_()
    data class Success(val items: List<GenModel_406_>) : GenState_406_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_406_()
    data class Partial(val items: List<GenModel_406_>, val hasMore: Boolean) : GenState_406_()
}

interface GenRepository_406_ {
    suspend fun getAll(): List<GenModel_406_>
    suspend fun getById(id: Long): GenModel_406_?
    suspend fun save(model: GenModel_406_): GenModel_406_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_406_>
}

@Singleton
class GenRepositoryImpl_406_ @Inject constructor() : GenRepository_406_ {
    private val store = mutableMapOf<Long, GenModel_406_>()
    override suspend fun getAll(): List<GenModel_406_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_406_? = store[id]
    override suspend fun save(model: GenModel_406_): GenModel_406_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_406_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_406_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_406_ @Inject constructor(
    private val repository: GenRepositoryImpl_406_
) : GenUseCase_406_<Unit, List<GenModel_406_>> {
    override suspend fun invoke(params: Unit): List<GenModel_406_> = repository.getAll()
}

class GenSaveUseCase_406_ @Inject constructor(
    private val repository: GenRepositoryImpl_406_
) : GenUseCase_406_<GenModel_406_, GenModel_406_> {
    override suspend fun invoke(params: GenModel_406_): GenModel_406_ = repository.save(params)
}

class GenDeleteUseCase_406_ @Inject constructor(
    private val repository: GenRepositoryImpl_406_
) : GenUseCase_406_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_406_ @Inject constructor(
    private val repository: GenRepositoryImpl_406_
) : GenUseCase_406_<String, List<GenModel_406_>> {
    override suspend fun invoke(params: String): List<GenModel_406_> = repository.search(params)
}

abstract class GenMapper_406_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_406_ : GenMapper_406_<GenModel_406_, String>() {
    override fun map(input: GenModel_406_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_406_ : GenMapper_406_<String, GenModel_406_>() {
    override fun map(input: String): GenModel_406_ {
        val parts = input.split(":")
        return GenModel_406_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_406_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_406_,
    private val saveUseCase: GenSaveUseCase_406_,
    private val deleteUseCase: GenDeleteUseCase_406_,
    private val searchUseCase: GenSearchUseCase_406_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_406_>(GenState_406_.Idle)
    val state: StateFlow<GenState_406_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_406_) {
        when (event) {
            is GenEvent_406_.Load -> loadAll()
            is GenEvent_406_.Update -> save(event.model)
            is GenEvent_406_.Delete -> delete(event.id)
            is GenEvent_406_.Refresh -> loadAll()
            is GenEvent_406_.Search -> search(event.query)
            is GenEvent_406_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_406_.Loading; _state.value = GenState_406_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_406_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_406_.Success(searchUseCase(query)) } }
}
