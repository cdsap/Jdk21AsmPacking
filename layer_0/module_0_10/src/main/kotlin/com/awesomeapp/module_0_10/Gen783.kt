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

data class GenModel_783_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_783_ {
    data class Load(val id: Long) : GenEvent_783_()
    data class Update(val model: GenModel_783_) : GenEvent_783_()
    data class Delete(val id: Long) : GenEvent_783_()
    data object Refresh : GenEvent_783_()
    data class Search(val query: String) : GenEvent_783_()
    data class Filter(val predicate: String) : GenEvent_783_()
}

sealed class GenState_783_ {
    data object Idle : GenState_783_()
    data object Loading : GenState_783_()
    data class Success(val items: List<GenModel_783_>) : GenState_783_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_783_()
    data class Partial(val items: List<GenModel_783_>, val hasMore: Boolean) : GenState_783_()
}

interface GenRepository_783_ {
    suspend fun getAll(): List<GenModel_783_>
    suspend fun getById(id: Long): GenModel_783_?
    suspend fun save(model: GenModel_783_): GenModel_783_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_783_>
}

@Singleton
class GenRepositoryImpl_783_ @Inject constructor() : GenRepository_783_ {
    private val store = mutableMapOf<Long, GenModel_783_>()
    override suspend fun getAll(): List<GenModel_783_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_783_? = store[id]
    override suspend fun save(model: GenModel_783_): GenModel_783_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_783_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_783_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_783_ @Inject constructor(
    private val repository: GenRepositoryImpl_783_
) : GenUseCase_783_<Unit, List<GenModel_783_>> {
    override suspend fun invoke(params: Unit): List<GenModel_783_> = repository.getAll()
}

class GenSaveUseCase_783_ @Inject constructor(
    private val repository: GenRepositoryImpl_783_
) : GenUseCase_783_<GenModel_783_, GenModel_783_> {
    override suspend fun invoke(params: GenModel_783_): GenModel_783_ = repository.save(params)
}

class GenDeleteUseCase_783_ @Inject constructor(
    private val repository: GenRepositoryImpl_783_
) : GenUseCase_783_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_783_ @Inject constructor(
    private val repository: GenRepositoryImpl_783_
) : GenUseCase_783_<String, List<GenModel_783_>> {
    override suspend fun invoke(params: String): List<GenModel_783_> = repository.search(params)
}

abstract class GenMapper_783_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_783_ : GenMapper_783_<GenModel_783_, String>() {
    override fun map(input: GenModel_783_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_783_ : GenMapper_783_<String, GenModel_783_>() {
    override fun map(input: String): GenModel_783_ {
        val parts = input.split(":")
        return GenModel_783_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_783_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_783_,
    private val saveUseCase: GenSaveUseCase_783_,
    private val deleteUseCase: GenDeleteUseCase_783_,
    private val searchUseCase: GenSearchUseCase_783_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_783_>(GenState_783_.Idle)
    val state: StateFlow<GenState_783_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_783_) {
        when (event) {
            is GenEvent_783_.Load -> loadAll()
            is GenEvent_783_.Update -> save(event.model)
            is GenEvent_783_.Delete -> delete(event.id)
            is GenEvent_783_.Refresh -> loadAll()
            is GenEvent_783_.Search -> search(event.query)
            is GenEvent_783_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_783_.Loading; _state.value = GenState_783_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_783_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_783_.Success(searchUseCase(query)) } }
}
