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

data class GenModel_27_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_27_ {
    data class Load(val id: Long) : GenEvent_27_()
    data class Update(val model: GenModel_27_) : GenEvent_27_()
    data class Delete(val id: Long) : GenEvent_27_()
    data object Refresh : GenEvent_27_()
    data class Search(val query: String) : GenEvent_27_()
    data class Filter(val predicate: String) : GenEvent_27_()
}

sealed class GenState_27_ {
    data object Idle : GenState_27_()
    data object Loading : GenState_27_()
    data class Success(val items: List<GenModel_27_>) : GenState_27_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_27_()
    data class Partial(val items: List<GenModel_27_>, val hasMore: Boolean) : GenState_27_()
}

interface GenRepository_27_ {
    suspend fun getAll(): List<GenModel_27_>
    suspend fun getById(id: Long): GenModel_27_?
    suspend fun save(model: GenModel_27_): GenModel_27_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_27_>
}

@Singleton
class GenRepositoryImpl_27_ @Inject constructor() : GenRepository_27_ {
    private val store = mutableMapOf<Long, GenModel_27_>()
    override suspend fun getAll(): List<GenModel_27_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_27_? = store[id]
    override suspend fun save(model: GenModel_27_): GenModel_27_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_27_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_27_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_27_ @Inject constructor(
    private val repository: GenRepositoryImpl_27_
) : GenUseCase_27_<Unit, List<GenModel_27_>> {
    override suspend fun invoke(params: Unit): List<GenModel_27_> = repository.getAll()
}

class GenSaveUseCase_27_ @Inject constructor(
    private val repository: GenRepositoryImpl_27_
) : GenUseCase_27_<GenModel_27_, GenModel_27_> {
    override suspend fun invoke(params: GenModel_27_): GenModel_27_ = repository.save(params)
}

class GenDeleteUseCase_27_ @Inject constructor(
    private val repository: GenRepositoryImpl_27_
) : GenUseCase_27_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_27_ @Inject constructor(
    private val repository: GenRepositoryImpl_27_
) : GenUseCase_27_<String, List<GenModel_27_>> {
    override suspend fun invoke(params: String): List<GenModel_27_> = repository.search(params)
}

abstract class GenMapper_27_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_27_ : GenMapper_27_<GenModel_27_, String>() {
    override fun map(input: GenModel_27_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_27_ : GenMapper_27_<String, GenModel_27_>() {
    override fun map(input: String): GenModel_27_ {
        val parts = input.split(":")
        return GenModel_27_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_27_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_27_,
    private val saveUseCase: GenSaveUseCase_27_,
    private val deleteUseCase: GenDeleteUseCase_27_,
    private val searchUseCase: GenSearchUseCase_27_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_27_>(GenState_27_.Idle)
    val state: StateFlow<GenState_27_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_27_) {
        when (event) {
            is GenEvent_27_.Load -> loadAll()
            is GenEvent_27_.Update -> save(event.model)
            is GenEvent_27_.Delete -> delete(event.id)
            is GenEvent_27_.Refresh -> loadAll()
            is GenEvent_27_.Search -> search(event.query)
            is GenEvent_27_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_27_.Loading; _state.value = GenState_27_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_27_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_27_.Success(searchUseCase(query)) } }
}
