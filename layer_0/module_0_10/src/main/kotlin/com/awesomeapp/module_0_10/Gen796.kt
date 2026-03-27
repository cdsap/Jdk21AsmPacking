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

data class GenModel_796_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_796_ {
    data class Load(val id: Long) : GenEvent_796_()
    data class Update(val model: GenModel_796_) : GenEvent_796_()
    data class Delete(val id: Long) : GenEvent_796_()
    data object Refresh : GenEvent_796_()
    data class Search(val query: String) : GenEvent_796_()
    data class Filter(val predicate: String) : GenEvent_796_()
}

sealed class GenState_796_ {
    data object Idle : GenState_796_()
    data object Loading : GenState_796_()
    data class Success(val items: List<GenModel_796_>) : GenState_796_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_796_()
    data class Partial(val items: List<GenModel_796_>, val hasMore: Boolean) : GenState_796_()
}

interface GenRepository_796_ {
    suspend fun getAll(): List<GenModel_796_>
    suspend fun getById(id: Long): GenModel_796_?
    suspend fun save(model: GenModel_796_): GenModel_796_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_796_>
}

@Singleton
class GenRepositoryImpl_796_ @Inject constructor() : GenRepository_796_ {
    private val store = mutableMapOf<Long, GenModel_796_>()
    override suspend fun getAll(): List<GenModel_796_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_796_? = store[id]
    override suspend fun save(model: GenModel_796_): GenModel_796_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_796_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_796_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_796_ @Inject constructor(
    private val repository: GenRepositoryImpl_796_
) : GenUseCase_796_<Unit, List<GenModel_796_>> {
    override suspend fun invoke(params: Unit): List<GenModel_796_> = repository.getAll()
}

class GenSaveUseCase_796_ @Inject constructor(
    private val repository: GenRepositoryImpl_796_
) : GenUseCase_796_<GenModel_796_, GenModel_796_> {
    override suspend fun invoke(params: GenModel_796_): GenModel_796_ = repository.save(params)
}

class GenDeleteUseCase_796_ @Inject constructor(
    private val repository: GenRepositoryImpl_796_
) : GenUseCase_796_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_796_ @Inject constructor(
    private val repository: GenRepositoryImpl_796_
) : GenUseCase_796_<String, List<GenModel_796_>> {
    override suspend fun invoke(params: String): List<GenModel_796_> = repository.search(params)
}

abstract class GenMapper_796_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_796_ : GenMapper_796_<GenModel_796_, String>() {
    override fun map(input: GenModel_796_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_796_ : GenMapper_796_<String, GenModel_796_>() {
    override fun map(input: String): GenModel_796_ {
        val parts = input.split(":")
        return GenModel_796_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_796_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_796_,
    private val saveUseCase: GenSaveUseCase_796_,
    private val deleteUseCase: GenDeleteUseCase_796_,
    private val searchUseCase: GenSearchUseCase_796_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_796_>(GenState_796_.Idle)
    val state: StateFlow<GenState_796_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_796_) {
        when (event) {
            is GenEvent_796_.Load -> loadAll()
            is GenEvent_796_.Update -> save(event.model)
            is GenEvent_796_.Delete -> delete(event.id)
            is GenEvent_796_.Refresh -> loadAll()
            is GenEvent_796_.Search -> search(event.query)
            is GenEvent_796_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_796_.Loading; _state.value = GenState_796_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_796_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_796_.Success(searchUseCase(query)) } }
}
