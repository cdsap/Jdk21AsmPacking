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

data class GenModel_926_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_926_ {
    data class Load(val id: Long) : GenEvent_926_()
    data class Update(val model: GenModel_926_) : GenEvent_926_()
    data class Delete(val id: Long) : GenEvent_926_()
    data object Refresh : GenEvent_926_()
    data class Search(val query: String) : GenEvent_926_()
    data class Filter(val predicate: String) : GenEvent_926_()
}

sealed class GenState_926_ {
    data object Idle : GenState_926_()
    data object Loading : GenState_926_()
    data class Success(val items: List<GenModel_926_>) : GenState_926_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_926_()
    data class Partial(val items: List<GenModel_926_>, val hasMore: Boolean) : GenState_926_()
}

interface GenRepository_926_ {
    suspend fun getAll(): List<GenModel_926_>
    suspend fun getById(id: Long): GenModel_926_?
    suspend fun save(model: GenModel_926_): GenModel_926_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_926_>
}

@Singleton
class GenRepositoryImpl_926_ @Inject constructor() : GenRepository_926_ {
    private val store = mutableMapOf<Long, GenModel_926_>()
    override suspend fun getAll(): List<GenModel_926_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_926_? = store[id]
    override suspend fun save(model: GenModel_926_): GenModel_926_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_926_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_926_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_926_ @Inject constructor(
    private val repository: GenRepositoryImpl_926_
) : GenUseCase_926_<Unit, List<GenModel_926_>> {
    override suspend fun invoke(params: Unit): List<GenModel_926_> = repository.getAll()
}

class GenSaveUseCase_926_ @Inject constructor(
    private val repository: GenRepositoryImpl_926_
) : GenUseCase_926_<GenModel_926_, GenModel_926_> {
    override suspend fun invoke(params: GenModel_926_): GenModel_926_ = repository.save(params)
}

class GenDeleteUseCase_926_ @Inject constructor(
    private val repository: GenRepositoryImpl_926_
) : GenUseCase_926_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_926_ @Inject constructor(
    private val repository: GenRepositoryImpl_926_
) : GenUseCase_926_<String, List<GenModel_926_>> {
    override suspend fun invoke(params: String): List<GenModel_926_> = repository.search(params)
}

abstract class GenMapper_926_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_926_ : GenMapper_926_<GenModel_926_, String>() {
    override fun map(input: GenModel_926_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_926_ : GenMapper_926_<String, GenModel_926_>() {
    override fun map(input: String): GenModel_926_ {
        val parts = input.split(":")
        return GenModel_926_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_926_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_926_,
    private val saveUseCase: GenSaveUseCase_926_,
    private val deleteUseCase: GenDeleteUseCase_926_,
    private val searchUseCase: GenSearchUseCase_926_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_926_>(GenState_926_.Idle)
    val state: StateFlow<GenState_926_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_926_) {
        when (event) {
            is GenEvent_926_.Load -> loadAll()
            is GenEvent_926_.Update -> save(event.model)
            is GenEvent_926_.Delete -> delete(event.id)
            is GenEvent_926_.Refresh -> loadAll()
            is GenEvent_926_.Search -> search(event.query)
            is GenEvent_926_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_926_.Loading; _state.value = GenState_926_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_926_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_926_.Success(searchUseCase(query)) } }
}
