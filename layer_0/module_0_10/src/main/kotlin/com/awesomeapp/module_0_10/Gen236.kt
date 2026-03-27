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

data class GenModel_236_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_236_ {
    data class Load(val id: Long) : GenEvent_236_()
    data class Update(val model: GenModel_236_) : GenEvent_236_()
    data class Delete(val id: Long) : GenEvent_236_()
    data object Refresh : GenEvent_236_()
    data class Search(val query: String) : GenEvent_236_()
    data class Filter(val predicate: String) : GenEvent_236_()
}

sealed class GenState_236_ {
    data object Idle : GenState_236_()
    data object Loading : GenState_236_()
    data class Success(val items: List<GenModel_236_>) : GenState_236_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_236_()
    data class Partial(val items: List<GenModel_236_>, val hasMore: Boolean) : GenState_236_()
}

interface GenRepository_236_ {
    suspend fun getAll(): List<GenModel_236_>
    suspend fun getById(id: Long): GenModel_236_?
    suspend fun save(model: GenModel_236_): GenModel_236_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_236_>
}

@Singleton
class GenRepositoryImpl_236_ @Inject constructor() : GenRepository_236_ {
    private val store = mutableMapOf<Long, GenModel_236_>()
    override suspend fun getAll(): List<GenModel_236_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_236_? = store[id]
    override suspend fun save(model: GenModel_236_): GenModel_236_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_236_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_236_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_236_ @Inject constructor(
    private val repository: GenRepositoryImpl_236_
) : GenUseCase_236_<Unit, List<GenModel_236_>> {
    override suspend fun invoke(params: Unit): List<GenModel_236_> = repository.getAll()
}

class GenSaveUseCase_236_ @Inject constructor(
    private val repository: GenRepositoryImpl_236_
) : GenUseCase_236_<GenModel_236_, GenModel_236_> {
    override suspend fun invoke(params: GenModel_236_): GenModel_236_ = repository.save(params)
}

class GenDeleteUseCase_236_ @Inject constructor(
    private val repository: GenRepositoryImpl_236_
) : GenUseCase_236_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_236_ @Inject constructor(
    private val repository: GenRepositoryImpl_236_
) : GenUseCase_236_<String, List<GenModel_236_>> {
    override suspend fun invoke(params: String): List<GenModel_236_> = repository.search(params)
}

abstract class GenMapper_236_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_236_ : GenMapper_236_<GenModel_236_, String>() {
    override fun map(input: GenModel_236_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_236_ : GenMapper_236_<String, GenModel_236_>() {
    override fun map(input: String): GenModel_236_ {
        val parts = input.split(":")
        return GenModel_236_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_236_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_236_,
    private val saveUseCase: GenSaveUseCase_236_,
    private val deleteUseCase: GenDeleteUseCase_236_,
    private val searchUseCase: GenSearchUseCase_236_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_236_>(GenState_236_.Idle)
    val state: StateFlow<GenState_236_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_236_) {
        when (event) {
            is GenEvent_236_.Load -> loadAll()
            is GenEvent_236_.Update -> save(event.model)
            is GenEvent_236_.Delete -> delete(event.id)
            is GenEvent_236_.Refresh -> loadAll()
            is GenEvent_236_.Search -> search(event.query)
            is GenEvent_236_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_236_.Loading; _state.value = GenState_236_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_236_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_236_.Success(searchUseCase(query)) } }
}
