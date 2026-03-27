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

data class GenModel_358_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_358_ {
    data class Load(val id: Long) : GenEvent_358_()
    data class Update(val model: GenModel_358_) : GenEvent_358_()
    data class Delete(val id: Long) : GenEvent_358_()
    data object Refresh : GenEvent_358_()
    data class Search(val query: String) : GenEvent_358_()
    data class Filter(val predicate: String) : GenEvent_358_()
}

sealed class GenState_358_ {
    data object Idle : GenState_358_()
    data object Loading : GenState_358_()
    data class Success(val items: List<GenModel_358_>) : GenState_358_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_358_()
    data class Partial(val items: List<GenModel_358_>, val hasMore: Boolean) : GenState_358_()
}

interface GenRepository_358_ {
    suspend fun getAll(): List<GenModel_358_>
    suspend fun getById(id: Long): GenModel_358_?
    suspend fun save(model: GenModel_358_): GenModel_358_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_358_>
}

@Singleton
class GenRepositoryImpl_358_ @Inject constructor() : GenRepository_358_ {
    private val store = mutableMapOf<Long, GenModel_358_>()
    override suspend fun getAll(): List<GenModel_358_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_358_? = store[id]
    override suspend fun save(model: GenModel_358_): GenModel_358_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_358_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_358_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_358_ @Inject constructor(
    private val repository: GenRepositoryImpl_358_
) : GenUseCase_358_<Unit, List<GenModel_358_>> {
    override suspend fun invoke(params: Unit): List<GenModel_358_> = repository.getAll()
}

class GenSaveUseCase_358_ @Inject constructor(
    private val repository: GenRepositoryImpl_358_
) : GenUseCase_358_<GenModel_358_, GenModel_358_> {
    override suspend fun invoke(params: GenModel_358_): GenModel_358_ = repository.save(params)
}

class GenDeleteUseCase_358_ @Inject constructor(
    private val repository: GenRepositoryImpl_358_
) : GenUseCase_358_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_358_ @Inject constructor(
    private val repository: GenRepositoryImpl_358_
) : GenUseCase_358_<String, List<GenModel_358_>> {
    override suspend fun invoke(params: String): List<GenModel_358_> = repository.search(params)
}

abstract class GenMapper_358_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_358_ : GenMapper_358_<GenModel_358_, String>() {
    override fun map(input: GenModel_358_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_358_ : GenMapper_358_<String, GenModel_358_>() {
    override fun map(input: String): GenModel_358_ {
        val parts = input.split(":")
        return GenModel_358_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_358_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_358_,
    private val saveUseCase: GenSaveUseCase_358_,
    private val deleteUseCase: GenDeleteUseCase_358_,
    private val searchUseCase: GenSearchUseCase_358_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_358_>(GenState_358_.Idle)
    val state: StateFlow<GenState_358_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_358_) {
        when (event) {
            is GenEvent_358_.Load -> loadAll()
            is GenEvent_358_.Update -> save(event.model)
            is GenEvent_358_.Delete -> delete(event.id)
            is GenEvent_358_.Refresh -> loadAll()
            is GenEvent_358_.Search -> search(event.query)
            is GenEvent_358_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_358_.Loading; _state.value = GenState_358_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_358_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_358_.Success(searchUseCase(query)) } }
}
