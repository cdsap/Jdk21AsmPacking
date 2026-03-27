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

data class GenModel_352_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_352_ {
    data class Load(val id: Long) : GenEvent_352_()
    data class Update(val model: GenModel_352_) : GenEvent_352_()
    data class Delete(val id: Long) : GenEvent_352_()
    data object Refresh : GenEvent_352_()
    data class Search(val query: String) : GenEvent_352_()
    data class Filter(val predicate: String) : GenEvent_352_()
}

sealed class GenState_352_ {
    data object Idle : GenState_352_()
    data object Loading : GenState_352_()
    data class Success(val items: List<GenModel_352_>) : GenState_352_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_352_()
    data class Partial(val items: List<GenModel_352_>, val hasMore: Boolean) : GenState_352_()
}

interface GenRepository_352_ {
    suspend fun getAll(): List<GenModel_352_>
    suspend fun getById(id: Long): GenModel_352_?
    suspend fun save(model: GenModel_352_): GenModel_352_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_352_>
}

@Singleton
class GenRepositoryImpl_352_ @Inject constructor() : GenRepository_352_ {
    private val store = mutableMapOf<Long, GenModel_352_>()
    override suspend fun getAll(): List<GenModel_352_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_352_? = store[id]
    override suspend fun save(model: GenModel_352_): GenModel_352_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_352_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_352_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_352_ @Inject constructor(
    private val repository: GenRepositoryImpl_352_
) : GenUseCase_352_<Unit, List<GenModel_352_>> {
    override suspend fun invoke(params: Unit): List<GenModel_352_> = repository.getAll()
}

class GenSaveUseCase_352_ @Inject constructor(
    private val repository: GenRepositoryImpl_352_
) : GenUseCase_352_<GenModel_352_, GenModel_352_> {
    override suspend fun invoke(params: GenModel_352_): GenModel_352_ = repository.save(params)
}

class GenDeleteUseCase_352_ @Inject constructor(
    private val repository: GenRepositoryImpl_352_
) : GenUseCase_352_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_352_ @Inject constructor(
    private val repository: GenRepositoryImpl_352_
) : GenUseCase_352_<String, List<GenModel_352_>> {
    override suspend fun invoke(params: String): List<GenModel_352_> = repository.search(params)
}

abstract class GenMapper_352_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_352_ : GenMapper_352_<GenModel_352_, String>() {
    override fun map(input: GenModel_352_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_352_ : GenMapper_352_<String, GenModel_352_>() {
    override fun map(input: String): GenModel_352_ {
        val parts = input.split(":")
        return GenModel_352_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_352_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_352_,
    private val saveUseCase: GenSaveUseCase_352_,
    private val deleteUseCase: GenDeleteUseCase_352_,
    private val searchUseCase: GenSearchUseCase_352_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_352_>(GenState_352_.Idle)
    val state: StateFlow<GenState_352_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_352_) {
        when (event) {
            is GenEvent_352_.Load -> loadAll()
            is GenEvent_352_.Update -> save(event.model)
            is GenEvent_352_.Delete -> delete(event.id)
            is GenEvent_352_.Refresh -> loadAll()
            is GenEvent_352_.Search -> search(event.query)
            is GenEvent_352_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_352_.Loading; _state.value = GenState_352_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_352_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_352_.Success(searchUseCase(query)) } }
}
