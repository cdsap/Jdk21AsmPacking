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

data class GenModel_277_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_277_ {
    data class Load(val id: Long) : GenEvent_277_()
    data class Update(val model: GenModel_277_) : GenEvent_277_()
    data class Delete(val id: Long) : GenEvent_277_()
    data object Refresh : GenEvent_277_()
    data class Search(val query: String) : GenEvent_277_()
    data class Filter(val predicate: String) : GenEvent_277_()
}

sealed class GenState_277_ {
    data object Idle : GenState_277_()
    data object Loading : GenState_277_()
    data class Success(val items: List<GenModel_277_>) : GenState_277_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_277_()
    data class Partial(val items: List<GenModel_277_>, val hasMore: Boolean) : GenState_277_()
}

interface GenRepository_277_ {
    suspend fun getAll(): List<GenModel_277_>
    suspend fun getById(id: Long): GenModel_277_?
    suspend fun save(model: GenModel_277_): GenModel_277_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_277_>
}

@Singleton
class GenRepositoryImpl_277_ @Inject constructor() : GenRepository_277_ {
    private val store = mutableMapOf<Long, GenModel_277_>()
    override suspend fun getAll(): List<GenModel_277_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_277_? = store[id]
    override suspend fun save(model: GenModel_277_): GenModel_277_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_277_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_277_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_277_ @Inject constructor(
    private val repository: GenRepositoryImpl_277_
) : GenUseCase_277_<Unit, List<GenModel_277_>> {
    override suspend fun invoke(params: Unit): List<GenModel_277_> = repository.getAll()
}

class GenSaveUseCase_277_ @Inject constructor(
    private val repository: GenRepositoryImpl_277_
) : GenUseCase_277_<GenModel_277_, GenModel_277_> {
    override suspend fun invoke(params: GenModel_277_): GenModel_277_ = repository.save(params)
}

class GenDeleteUseCase_277_ @Inject constructor(
    private val repository: GenRepositoryImpl_277_
) : GenUseCase_277_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_277_ @Inject constructor(
    private val repository: GenRepositoryImpl_277_
) : GenUseCase_277_<String, List<GenModel_277_>> {
    override suspend fun invoke(params: String): List<GenModel_277_> = repository.search(params)
}

abstract class GenMapper_277_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_277_ : GenMapper_277_<GenModel_277_, String>() {
    override fun map(input: GenModel_277_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_277_ : GenMapper_277_<String, GenModel_277_>() {
    override fun map(input: String): GenModel_277_ {
        val parts = input.split(":")
        return GenModel_277_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_277_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_277_,
    private val saveUseCase: GenSaveUseCase_277_,
    private val deleteUseCase: GenDeleteUseCase_277_,
    private val searchUseCase: GenSearchUseCase_277_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_277_>(GenState_277_.Idle)
    val state: StateFlow<GenState_277_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_277_) {
        when (event) {
            is GenEvent_277_.Load -> loadAll()
            is GenEvent_277_.Update -> save(event.model)
            is GenEvent_277_.Delete -> delete(event.id)
            is GenEvent_277_.Refresh -> loadAll()
            is GenEvent_277_.Search -> search(event.query)
            is GenEvent_277_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_277_.Loading; _state.value = GenState_277_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_277_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_277_.Success(searchUseCase(query)) } }
}
