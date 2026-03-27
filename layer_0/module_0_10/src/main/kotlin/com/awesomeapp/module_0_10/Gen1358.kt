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

data class GenModel_1358_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1358_ {
    data class Load(val id: Long) : GenEvent_1358_()
    data class Update(val model: GenModel_1358_) : GenEvent_1358_()
    data class Delete(val id: Long) : GenEvent_1358_()
    data object Refresh : GenEvent_1358_()
    data class Search(val query: String) : GenEvent_1358_()
    data class Filter(val predicate: String) : GenEvent_1358_()
}

sealed class GenState_1358_ {
    data object Idle : GenState_1358_()
    data object Loading : GenState_1358_()
    data class Success(val items: List<GenModel_1358_>) : GenState_1358_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1358_()
    data class Partial(val items: List<GenModel_1358_>, val hasMore: Boolean) : GenState_1358_()
}

interface GenRepository_1358_ {
    suspend fun getAll(): List<GenModel_1358_>
    suspend fun getById(id: Long): GenModel_1358_?
    suspend fun save(model: GenModel_1358_): GenModel_1358_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1358_>
}

@Singleton
class GenRepositoryImpl_1358_ @Inject constructor() : GenRepository_1358_ {
    private val store = mutableMapOf<Long, GenModel_1358_>()
    override suspend fun getAll(): List<GenModel_1358_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1358_? = store[id]
    override suspend fun save(model: GenModel_1358_): GenModel_1358_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1358_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1358_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1358_ @Inject constructor(
    private val repository: GenRepositoryImpl_1358_
) : GenUseCase_1358_<Unit, List<GenModel_1358_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1358_> = repository.getAll()
}

class GenSaveUseCase_1358_ @Inject constructor(
    private val repository: GenRepositoryImpl_1358_
) : GenUseCase_1358_<GenModel_1358_, GenModel_1358_> {
    override suspend fun invoke(params: GenModel_1358_): GenModel_1358_ = repository.save(params)
}

class GenDeleteUseCase_1358_ @Inject constructor(
    private val repository: GenRepositoryImpl_1358_
) : GenUseCase_1358_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1358_ @Inject constructor(
    private val repository: GenRepositoryImpl_1358_
) : GenUseCase_1358_<String, List<GenModel_1358_>> {
    override suspend fun invoke(params: String): List<GenModel_1358_> = repository.search(params)
}

abstract class GenMapper_1358_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1358_ : GenMapper_1358_<GenModel_1358_, String>() {
    override fun map(input: GenModel_1358_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1358_ : GenMapper_1358_<String, GenModel_1358_>() {
    override fun map(input: String): GenModel_1358_ {
        val parts = input.split(":")
        return GenModel_1358_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1358_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1358_,
    private val saveUseCase: GenSaveUseCase_1358_,
    private val deleteUseCase: GenDeleteUseCase_1358_,
    private val searchUseCase: GenSearchUseCase_1358_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1358_>(GenState_1358_.Idle)
    val state: StateFlow<GenState_1358_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1358_) {
        when (event) {
            is GenEvent_1358_.Load -> loadAll()
            is GenEvent_1358_.Update -> save(event.model)
            is GenEvent_1358_.Delete -> delete(event.id)
            is GenEvent_1358_.Refresh -> loadAll()
            is GenEvent_1358_.Search -> search(event.query)
            is GenEvent_1358_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1358_.Loading; _state.value = GenState_1358_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1358_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1358_.Success(searchUseCase(query)) } }
}
