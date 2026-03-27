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

data class GenModel_2358_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2358_ {
    data class Load(val id: Long) : GenEvent_2358_()
    data class Update(val model: GenModel_2358_) : GenEvent_2358_()
    data class Delete(val id: Long) : GenEvent_2358_()
    data object Refresh : GenEvent_2358_()
    data class Search(val query: String) : GenEvent_2358_()
    data class Filter(val predicate: String) : GenEvent_2358_()
}

sealed class GenState_2358_ {
    data object Idle : GenState_2358_()
    data object Loading : GenState_2358_()
    data class Success(val items: List<GenModel_2358_>) : GenState_2358_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2358_()
    data class Partial(val items: List<GenModel_2358_>, val hasMore: Boolean) : GenState_2358_()
}

interface GenRepository_2358_ {
    suspend fun getAll(): List<GenModel_2358_>
    suspend fun getById(id: Long): GenModel_2358_?
    suspend fun save(model: GenModel_2358_): GenModel_2358_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2358_>
}

@Singleton
class GenRepositoryImpl_2358_ @Inject constructor() : GenRepository_2358_ {
    private val store = mutableMapOf<Long, GenModel_2358_>()
    override suspend fun getAll(): List<GenModel_2358_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2358_? = store[id]
    override suspend fun save(model: GenModel_2358_): GenModel_2358_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2358_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2358_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2358_ @Inject constructor(
    private val repository: GenRepositoryImpl_2358_
) : GenUseCase_2358_<Unit, List<GenModel_2358_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2358_> = repository.getAll()
}

class GenSaveUseCase_2358_ @Inject constructor(
    private val repository: GenRepositoryImpl_2358_
) : GenUseCase_2358_<GenModel_2358_, GenModel_2358_> {
    override suspend fun invoke(params: GenModel_2358_): GenModel_2358_ = repository.save(params)
}

class GenDeleteUseCase_2358_ @Inject constructor(
    private val repository: GenRepositoryImpl_2358_
) : GenUseCase_2358_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2358_ @Inject constructor(
    private val repository: GenRepositoryImpl_2358_
) : GenUseCase_2358_<String, List<GenModel_2358_>> {
    override suspend fun invoke(params: String): List<GenModel_2358_> = repository.search(params)
}

abstract class GenMapper_2358_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2358_ : GenMapper_2358_<GenModel_2358_, String>() {
    override fun map(input: GenModel_2358_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2358_ : GenMapper_2358_<String, GenModel_2358_>() {
    override fun map(input: String): GenModel_2358_ {
        val parts = input.split(":")
        return GenModel_2358_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2358_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2358_,
    private val saveUseCase: GenSaveUseCase_2358_,
    private val deleteUseCase: GenDeleteUseCase_2358_,
    private val searchUseCase: GenSearchUseCase_2358_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2358_>(GenState_2358_.Idle)
    val state: StateFlow<GenState_2358_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2358_) {
        when (event) {
            is GenEvent_2358_.Load -> loadAll()
            is GenEvent_2358_.Update -> save(event.model)
            is GenEvent_2358_.Delete -> delete(event.id)
            is GenEvent_2358_.Refresh -> loadAll()
            is GenEvent_2358_.Search -> search(event.query)
            is GenEvent_2358_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2358_.Loading; _state.value = GenState_2358_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2358_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2358_.Success(searchUseCase(query)) } }
}
