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

data class GenModel_443_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_443_ {
    data class Load(val id: Long) : GenEvent_443_()
    data class Update(val model: GenModel_443_) : GenEvent_443_()
    data class Delete(val id: Long) : GenEvent_443_()
    data object Refresh : GenEvent_443_()
    data class Search(val query: String) : GenEvent_443_()
    data class Filter(val predicate: String) : GenEvent_443_()
}

sealed class GenState_443_ {
    data object Idle : GenState_443_()
    data object Loading : GenState_443_()
    data class Success(val items: List<GenModel_443_>) : GenState_443_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_443_()
    data class Partial(val items: List<GenModel_443_>, val hasMore: Boolean) : GenState_443_()
}

interface GenRepository_443_ {
    suspend fun getAll(): List<GenModel_443_>
    suspend fun getById(id: Long): GenModel_443_?
    suspend fun save(model: GenModel_443_): GenModel_443_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_443_>
}

@Singleton
class GenRepositoryImpl_443_ @Inject constructor() : GenRepository_443_ {
    private val store = mutableMapOf<Long, GenModel_443_>()
    override suspend fun getAll(): List<GenModel_443_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_443_? = store[id]
    override suspend fun save(model: GenModel_443_): GenModel_443_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_443_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_443_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_443_ @Inject constructor(
    private val repository: GenRepositoryImpl_443_
) : GenUseCase_443_<Unit, List<GenModel_443_>> {
    override suspend fun invoke(params: Unit): List<GenModel_443_> = repository.getAll()
}

class GenSaveUseCase_443_ @Inject constructor(
    private val repository: GenRepositoryImpl_443_
) : GenUseCase_443_<GenModel_443_, GenModel_443_> {
    override suspend fun invoke(params: GenModel_443_): GenModel_443_ = repository.save(params)
}

class GenDeleteUseCase_443_ @Inject constructor(
    private val repository: GenRepositoryImpl_443_
) : GenUseCase_443_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_443_ @Inject constructor(
    private val repository: GenRepositoryImpl_443_
) : GenUseCase_443_<String, List<GenModel_443_>> {
    override suspend fun invoke(params: String): List<GenModel_443_> = repository.search(params)
}

abstract class GenMapper_443_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_443_ : GenMapper_443_<GenModel_443_, String>() {
    override fun map(input: GenModel_443_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_443_ : GenMapper_443_<String, GenModel_443_>() {
    override fun map(input: String): GenModel_443_ {
        val parts = input.split(":")
        return GenModel_443_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_443_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_443_,
    private val saveUseCase: GenSaveUseCase_443_,
    private val deleteUseCase: GenDeleteUseCase_443_,
    private val searchUseCase: GenSearchUseCase_443_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_443_>(GenState_443_.Idle)
    val state: StateFlow<GenState_443_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_443_) {
        when (event) {
            is GenEvent_443_.Load -> loadAll()
            is GenEvent_443_.Update -> save(event.model)
            is GenEvent_443_.Delete -> delete(event.id)
            is GenEvent_443_.Refresh -> loadAll()
            is GenEvent_443_.Search -> search(event.query)
            is GenEvent_443_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_443_.Loading; _state.value = GenState_443_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_443_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_443_.Success(searchUseCase(query)) } }
}
