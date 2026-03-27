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

data class GenModel_964_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_964_ {
    data class Load(val id: Long) : GenEvent_964_()
    data class Update(val model: GenModel_964_) : GenEvent_964_()
    data class Delete(val id: Long) : GenEvent_964_()
    data object Refresh : GenEvent_964_()
    data class Search(val query: String) : GenEvent_964_()
    data class Filter(val predicate: String) : GenEvent_964_()
}

sealed class GenState_964_ {
    data object Idle : GenState_964_()
    data object Loading : GenState_964_()
    data class Success(val items: List<GenModel_964_>) : GenState_964_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_964_()
    data class Partial(val items: List<GenModel_964_>, val hasMore: Boolean) : GenState_964_()
}

interface GenRepository_964_ {
    suspend fun getAll(): List<GenModel_964_>
    suspend fun getById(id: Long): GenModel_964_?
    suspend fun save(model: GenModel_964_): GenModel_964_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_964_>
}

@Singleton
class GenRepositoryImpl_964_ @Inject constructor() : GenRepository_964_ {
    private val store = mutableMapOf<Long, GenModel_964_>()
    override suspend fun getAll(): List<GenModel_964_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_964_? = store[id]
    override suspend fun save(model: GenModel_964_): GenModel_964_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_964_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_964_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_964_ @Inject constructor(
    private val repository: GenRepositoryImpl_964_
) : GenUseCase_964_<Unit, List<GenModel_964_>> {
    override suspend fun invoke(params: Unit): List<GenModel_964_> = repository.getAll()
}

class GenSaveUseCase_964_ @Inject constructor(
    private val repository: GenRepositoryImpl_964_
) : GenUseCase_964_<GenModel_964_, GenModel_964_> {
    override suspend fun invoke(params: GenModel_964_): GenModel_964_ = repository.save(params)
}

class GenDeleteUseCase_964_ @Inject constructor(
    private val repository: GenRepositoryImpl_964_
) : GenUseCase_964_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_964_ @Inject constructor(
    private val repository: GenRepositoryImpl_964_
) : GenUseCase_964_<String, List<GenModel_964_>> {
    override suspend fun invoke(params: String): List<GenModel_964_> = repository.search(params)
}

abstract class GenMapper_964_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_964_ : GenMapper_964_<GenModel_964_, String>() {
    override fun map(input: GenModel_964_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_964_ : GenMapper_964_<String, GenModel_964_>() {
    override fun map(input: String): GenModel_964_ {
        val parts = input.split(":")
        return GenModel_964_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_964_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_964_,
    private val saveUseCase: GenSaveUseCase_964_,
    private val deleteUseCase: GenDeleteUseCase_964_,
    private val searchUseCase: GenSearchUseCase_964_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_964_>(GenState_964_.Idle)
    val state: StateFlow<GenState_964_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_964_) {
        when (event) {
            is GenEvent_964_.Load -> loadAll()
            is GenEvent_964_.Update -> save(event.model)
            is GenEvent_964_.Delete -> delete(event.id)
            is GenEvent_964_.Refresh -> loadAll()
            is GenEvent_964_.Search -> search(event.query)
            is GenEvent_964_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_964_.Loading; _state.value = GenState_964_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_964_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_964_.Success(searchUseCase(query)) } }
}
