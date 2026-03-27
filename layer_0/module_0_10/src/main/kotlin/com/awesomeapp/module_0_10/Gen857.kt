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

data class GenModel_857_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_857_ {
    data class Load(val id: Long) : GenEvent_857_()
    data class Update(val model: GenModel_857_) : GenEvent_857_()
    data class Delete(val id: Long) : GenEvent_857_()
    data object Refresh : GenEvent_857_()
    data class Search(val query: String) : GenEvent_857_()
    data class Filter(val predicate: String) : GenEvent_857_()
}

sealed class GenState_857_ {
    data object Idle : GenState_857_()
    data object Loading : GenState_857_()
    data class Success(val items: List<GenModel_857_>) : GenState_857_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_857_()
    data class Partial(val items: List<GenModel_857_>, val hasMore: Boolean) : GenState_857_()
}

interface GenRepository_857_ {
    suspend fun getAll(): List<GenModel_857_>
    suspend fun getById(id: Long): GenModel_857_?
    suspend fun save(model: GenModel_857_): GenModel_857_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_857_>
}

@Singleton
class GenRepositoryImpl_857_ @Inject constructor() : GenRepository_857_ {
    private val store = mutableMapOf<Long, GenModel_857_>()
    override suspend fun getAll(): List<GenModel_857_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_857_? = store[id]
    override suspend fun save(model: GenModel_857_): GenModel_857_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_857_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_857_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_857_ @Inject constructor(
    private val repository: GenRepositoryImpl_857_
) : GenUseCase_857_<Unit, List<GenModel_857_>> {
    override suspend fun invoke(params: Unit): List<GenModel_857_> = repository.getAll()
}

class GenSaveUseCase_857_ @Inject constructor(
    private val repository: GenRepositoryImpl_857_
) : GenUseCase_857_<GenModel_857_, GenModel_857_> {
    override suspend fun invoke(params: GenModel_857_): GenModel_857_ = repository.save(params)
}

class GenDeleteUseCase_857_ @Inject constructor(
    private val repository: GenRepositoryImpl_857_
) : GenUseCase_857_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_857_ @Inject constructor(
    private val repository: GenRepositoryImpl_857_
) : GenUseCase_857_<String, List<GenModel_857_>> {
    override suspend fun invoke(params: String): List<GenModel_857_> = repository.search(params)
}

abstract class GenMapper_857_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_857_ : GenMapper_857_<GenModel_857_, String>() {
    override fun map(input: GenModel_857_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_857_ : GenMapper_857_<String, GenModel_857_>() {
    override fun map(input: String): GenModel_857_ {
        val parts = input.split(":")
        return GenModel_857_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_857_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_857_,
    private val saveUseCase: GenSaveUseCase_857_,
    private val deleteUseCase: GenDeleteUseCase_857_,
    private val searchUseCase: GenSearchUseCase_857_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_857_>(GenState_857_.Idle)
    val state: StateFlow<GenState_857_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_857_) {
        when (event) {
            is GenEvent_857_.Load -> loadAll()
            is GenEvent_857_.Update -> save(event.model)
            is GenEvent_857_.Delete -> delete(event.id)
            is GenEvent_857_.Refresh -> loadAll()
            is GenEvent_857_.Search -> search(event.query)
            is GenEvent_857_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_857_.Loading; _state.value = GenState_857_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_857_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_857_.Success(searchUseCase(query)) } }
}
