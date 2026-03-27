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

data class GenModel_547_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_547_ {
    data class Load(val id: Long) : GenEvent_547_()
    data class Update(val model: GenModel_547_) : GenEvent_547_()
    data class Delete(val id: Long) : GenEvent_547_()
    data object Refresh : GenEvent_547_()
    data class Search(val query: String) : GenEvent_547_()
    data class Filter(val predicate: String) : GenEvent_547_()
}

sealed class GenState_547_ {
    data object Idle : GenState_547_()
    data object Loading : GenState_547_()
    data class Success(val items: List<GenModel_547_>) : GenState_547_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_547_()
    data class Partial(val items: List<GenModel_547_>, val hasMore: Boolean) : GenState_547_()
}

interface GenRepository_547_ {
    suspend fun getAll(): List<GenModel_547_>
    suspend fun getById(id: Long): GenModel_547_?
    suspend fun save(model: GenModel_547_): GenModel_547_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_547_>
}

@Singleton
class GenRepositoryImpl_547_ @Inject constructor() : GenRepository_547_ {
    private val store = mutableMapOf<Long, GenModel_547_>()
    override suspend fun getAll(): List<GenModel_547_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_547_? = store[id]
    override suspend fun save(model: GenModel_547_): GenModel_547_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_547_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_547_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_547_ @Inject constructor(
    private val repository: GenRepositoryImpl_547_
) : GenUseCase_547_<Unit, List<GenModel_547_>> {
    override suspend fun invoke(params: Unit): List<GenModel_547_> = repository.getAll()
}

class GenSaveUseCase_547_ @Inject constructor(
    private val repository: GenRepositoryImpl_547_
) : GenUseCase_547_<GenModel_547_, GenModel_547_> {
    override suspend fun invoke(params: GenModel_547_): GenModel_547_ = repository.save(params)
}

class GenDeleteUseCase_547_ @Inject constructor(
    private val repository: GenRepositoryImpl_547_
) : GenUseCase_547_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_547_ @Inject constructor(
    private val repository: GenRepositoryImpl_547_
) : GenUseCase_547_<String, List<GenModel_547_>> {
    override suspend fun invoke(params: String): List<GenModel_547_> = repository.search(params)
}

abstract class GenMapper_547_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_547_ : GenMapper_547_<GenModel_547_, String>() {
    override fun map(input: GenModel_547_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_547_ : GenMapper_547_<String, GenModel_547_>() {
    override fun map(input: String): GenModel_547_ {
        val parts = input.split(":")
        return GenModel_547_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_547_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_547_,
    private val saveUseCase: GenSaveUseCase_547_,
    private val deleteUseCase: GenDeleteUseCase_547_,
    private val searchUseCase: GenSearchUseCase_547_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_547_>(GenState_547_.Idle)
    val state: StateFlow<GenState_547_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_547_) {
        when (event) {
            is GenEvent_547_.Load -> loadAll()
            is GenEvent_547_.Update -> save(event.model)
            is GenEvent_547_.Delete -> delete(event.id)
            is GenEvent_547_.Refresh -> loadAll()
            is GenEvent_547_.Search -> search(event.query)
            is GenEvent_547_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_547_.Loading; _state.value = GenState_547_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_547_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_547_.Success(searchUseCase(query)) } }
}
