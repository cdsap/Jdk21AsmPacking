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

data class GenModel_2909_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2909_ {
    data class Load(val id: Long) : GenEvent_2909_()
    data class Update(val model: GenModel_2909_) : GenEvent_2909_()
    data class Delete(val id: Long) : GenEvent_2909_()
    data object Refresh : GenEvent_2909_()
    data class Search(val query: String) : GenEvent_2909_()
    data class Filter(val predicate: String) : GenEvent_2909_()
}

sealed class GenState_2909_ {
    data object Idle : GenState_2909_()
    data object Loading : GenState_2909_()
    data class Success(val items: List<GenModel_2909_>) : GenState_2909_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2909_()
    data class Partial(val items: List<GenModel_2909_>, val hasMore: Boolean) : GenState_2909_()
}

interface GenRepository_2909_ {
    suspend fun getAll(): List<GenModel_2909_>
    suspend fun getById(id: Long): GenModel_2909_?
    suspend fun save(model: GenModel_2909_): GenModel_2909_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2909_>
}

@Singleton
class GenRepositoryImpl_2909_ @Inject constructor() : GenRepository_2909_ {
    private val store = mutableMapOf<Long, GenModel_2909_>()
    override suspend fun getAll(): List<GenModel_2909_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2909_? = store[id]
    override suspend fun save(model: GenModel_2909_): GenModel_2909_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2909_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2909_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2909_ @Inject constructor(
    private val repository: GenRepositoryImpl_2909_
) : GenUseCase_2909_<Unit, List<GenModel_2909_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2909_> = repository.getAll()
}

class GenSaveUseCase_2909_ @Inject constructor(
    private val repository: GenRepositoryImpl_2909_
) : GenUseCase_2909_<GenModel_2909_, GenModel_2909_> {
    override suspend fun invoke(params: GenModel_2909_): GenModel_2909_ = repository.save(params)
}

class GenDeleteUseCase_2909_ @Inject constructor(
    private val repository: GenRepositoryImpl_2909_
) : GenUseCase_2909_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2909_ @Inject constructor(
    private val repository: GenRepositoryImpl_2909_
) : GenUseCase_2909_<String, List<GenModel_2909_>> {
    override suspend fun invoke(params: String): List<GenModel_2909_> = repository.search(params)
}

abstract class GenMapper_2909_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2909_ : GenMapper_2909_<GenModel_2909_, String>() {
    override fun map(input: GenModel_2909_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2909_ : GenMapper_2909_<String, GenModel_2909_>() {
    override fun map(input: String): GenModel_2909_ {
        val parts = input.split(":")
        return GenModel_2909_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2909_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2909_,
    private val saveUseCase: GenSaveUseCase_2909_,
    private val deleteUseCase: GenDeleteUseCase_2909_,
    private val searchUseCase: GenSearchUseCase_2909_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2909_>(GenState_2909_.Idle)
    val state: StateFlow<GenState_2909_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2909_) {
        when (event) {
            is GenEvent_2909_.Load -> loadAll()
            is GenEvent_2909_.Update -> save(event.model)
            is GenEvent_2909_.Delete -> delete(event.id)
            is GenEvent_2909_.Refresh -> loadAll()
            is GenEvent_2909_.Search -> search(event.query)
            is GenEvent_2909_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2909_.Loading; _state.value = GenState_2909_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2909_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2909_.Success(searchUseCase(query)) } }
}
