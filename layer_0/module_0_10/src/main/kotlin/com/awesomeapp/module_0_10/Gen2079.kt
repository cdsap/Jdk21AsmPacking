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

data class GenModel_2079_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2079_ {
    data class Load(val id: Long) : GenEvent_2079_()
    data class Update(val model: GenModel_2079_) : GenEvent_2079_()
    data class Delete(val id: Long) : GenEvent_2079_()
    data object Refresh : GenEvent_2079_()
    data class Search(val query: String) : GenEvent_2079_()
    data class Filter(val predicate: String) : GenEvent_2079_()
}

sealed class GenState_2079_ {
    data object Idle : GenState_2079_()
    data object Loading : GenState_2079_()
    data class Success(val items: List<GenModel_2079_>) : GenState_2079_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2079_()
    data class Partial(val items: List<GenModel_2079_>, val hasMore: Boolean) : GenState_2079_()
}

interface GenRepository_2079_ {
    suspend fun getAll(): List<GenModel_2079_>
    suspend fun getById(id: Long): GenModel_2079_?
    suspend fun save(model: GenModel_2079_): GenModel_2079_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2079_>
}

@Singleton
class GenRepositoryImpl_2079_ @Inject constructor() : GenRepository_2079_ {
    private val store = mutableMapOf<Long, GenModel_2079_>()
    override suspend fun getAll(): List<GenModel_2079_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2079_? = store[id]
    override suspend fun save(model: GenModel_2079_): GenModel_2079_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2079_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2079_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2079_ @Inject constructor(
    private val repository: GenRepositoryImpl_2079_
) : GenUseCase_2079_<Unit, List<GenModel_2079_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2079_> = repository.getAll()
}

class GenSaveUseCase_2079_ @Inject constructor(
    private val repository: GenRepositoryImpl_2079_
) : GenUseCase_2079_<GenModel_2079_, GenModel_2079_> {
    override suspend fun invoke(params: GenModel_2079_): GenModel_2079_ = repository.save(params)
}

class GenDeleteUseCase_2079_ @Inject constructor(
    private val repository: GenRepositoryImpl_2079_
) : GenUseCase_2079_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2079_ @Inject constructor(
    private val repository: GenRepositoryImpl_2079_
) : GenUseCase_2079_<String, List<GenModel_2079_>> {
    override suspend fun invoke(params: String): List<GenModel_2079_> = repository.search(params)
}

abstract class GenMapper_2079_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2079_ : GenMapper_2079_<GenModel_2079_, String>() {
    override fun map(input: GenModel_2079_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2079_ : GenMapper_2079_<String, GenModel_2079_>() {
    override fun map(input: String): GenModel_2079_ {
        val parts = input.split(":")
        return GenModel_2079_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2079_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2079_,
    private val saveUseCase: GenSaveUseCase_2079_,
    private val deleteUseCase: GenDeleteUseCase_2079_,
    private val searchUseCase: GenSearchUseCase_2079_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2079_>(GenState_2079_.Idle)
    val state: StateFlow<GenState_2079_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2079_) {
        when (event) {
            is GenEvent_2079_.Load -> loadAll()
            is GenEvent_2079_.Update -> save(event.model)
            is GenEvent_2079_.Delete -> delete(event.id)
            is GenEvent_2079_.Refresh -> loadAll()
            is GenEvent_2079_.Search -> search(event.query)
            is GenEvent_2079_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2079_.Loading; _state.value = GenState_2079_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2079_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2079_.Success(searchUseCase(query)) } }
}
