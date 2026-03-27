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

data class GenModel_119_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_119_ {
    data class Load(val id: Long) : GenEvent_119_()
    data class Update(val model: GenModel_119_) : GenEvent_119_()
    data class Delete(val id: Long) : GenEvent_119_()
    data object Refresh : GenEvent_119_()
    data class Search(val query: String) : GenEvent_119_()
    data class Filter(val predicate: String) : GenEvent_119_()
}

sealed class GenState_119_ {
    data object Idle : GenState_119_()
    data object Loading : GenState_119_()
    data class Success(val items: List<GenModel_119_>) : GenState_119_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_119_()
    data class Partial(val items: List<GenModel_119_>, val hasMore: Boolean) : GenState_119_()
}

interface GenRepository_119_ {
    suspend fun getAll(): List<GenModel_119_>
    suspend fun getById(id: Long): GenModel_119_?
    suspend fun save(model: GenModel_119_): GenModel_119_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_119_>
}

@Singleton
class GenRepositoryImpl_119_ @Inject constructor() : GenRepository_119_ {
    private val store = mutableMapOf<Long, GenModel_119_>()
    override suspend fun getAll(): List<GenModel_119_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_119_? = store[id]
    override suspend fun save(model: GenModel_119_): GenModel_119_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_119_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_119_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_119_ @Inject constructor(
    private val repository: GenRepositoryImpl_119_
) : GenUseCase_119_<Unit, List<GenModel_119_>> {
    override suspend fun invoke(params: Unit): List<GenModel_119_> = repository.getAll()
}

class GenSaveUseCase_119_ @Inject constructor(
    private val repository: GenRepositoryImpl_119_
) : GenUseCase_119_<GenModel_119_, GenModel_119_> {
    override suspend fun invoke(params: GenModel_119_): GenModel_119_ = repository.save(params)
}

class GenDeleteUseCase_119_ @Inject constructor(
    private val repository: GenRepositoryImpl_119_
) : GenUseCase_119_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_119_ @Inject constructor(
    private val repository: GenRepositoryImpl_119_
) : GenUseCase_119_<String, List<GenModel_119_>> {
    override suspend fun invoke(params: String): List<GenModel_119_> = repository.search(params)
}

abstract class GenMapper_119_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_119_ : GenMapper_119_<GenModel_119_, String>() {
    override fun map(input: GenModel_119_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_119_ : GenMapper_119_<String, GenModel_119_>() {
    override fun map(input: String): GenModel_119_ {
        val parts = input.split(":")
        return GenModel_119_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_119_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_119_,
    private val saveUseCase: GenSaveUseCase_119_,
    private val deleteUseCase: GenDeleteUseCase_119_,
    private val searchUseCase: GenSearchUseCase_119_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_119_>(GenState_119_.Idle)
    val state: StateFlow<GenState_119_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_119_) {
        when (event) {
            is GenEvent_119_.Load -> loadAll()
            is GenEvent_119_.Update -> save(event.model)
            is GenEvent_119_.Delete -> delete(event.id)
            is GenEvent_119_.Refresh -> loadAll()
            is GenEvent_119_.Search -> search(event.query)
            is GenEvent_119_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_119_.Loading; _state.value = GenState_119_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_119_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_119_.Success(searchUseCase(query)) } }
}
