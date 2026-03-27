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

data class GenModel_45_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_45_ {
    data class Load(val id: Long) : GenEvent_45_()
    data class Update(val model: GenModel_45_) : GenEvent_45_()
    data class Delete(val id: Long) : GenEvent_45_()
    data object Refresh : GenEvent_45_()
    data class Search(val query: String) : GenEvent_45_()
    data class Filter(val predicate: String) : GenEvent_45_()
}

sealed class GenState_45_ {
    data object Idle : GenState_45_()
    data object Loading : GenState_45_()
    data class Success(val items: List<GenModel_45_>) : GenState_45_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_45_()
    data class Partial(val items: List<GenModel_45_>, val hasMore: Boolean) : GenState_45_()
}

interface GenRepository_45_ {
    suspend fun getAll(): List<GenModel_45_>
    suspend fun getById(id: Long): GenModel_45_?
    suspend fun save(model: GenModel_45_): GenModel_45_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_45_>
}

@Singleton
class GenRepositoryImpl_45_ @Inject constructor() : GenRepository_45_ {
    private val store = mutableMapOf<Long, GenModel_45_>()
    override suspend fun getAll(): List<GenModel_45_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_45_? = store[id]
    override suspend fun save(model: GenModel_45_): GenModel_45_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_45_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_45_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_45_ @Inject constructor(
    private val repository: GenRepositoryImpl_45_
) : GenUseCase_45_<Unit, List<GenModel_45_>> {
    override suspend fun invoke(params: Unit): List<GenModel_45_> = repository.getAll()
}

class GenSaveUseCase_45_ @Inject constructor(
    private val repository: GenRepositoryImpl_45_
) : GenUseCase_45_<GenModel_45_, GenModel_45_> {
    override suspend fun invoke(params: GenModel_45_): GenModel_45_ = repository.save(params)
}

class GenDeleteUseCase_45_ @Inject constructor(
    private val repository: GenRepositoryImpl_45_
) : GenUseCase_45_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_45_ @Inject constructor(
    private val repository: GenRepositoryImpl_45_
) : GenUseCase_45_<String, List<GenModel_45_>> {
    override suspend fun invoke(params: String): List<GenModel_45_> = repository.search(params)
}

abstract class GenMapper_45_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_45_ : GenMapper_45_<GenModel_45_, String>() {
    override fun map(input: GenModel_45_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_45_ : GenMapper_45_<String, GenModel_45_>() {
    override fun map(input: String): GenModel_45_ {
        val parts = input.split(":")
        return GenModel_45_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_45_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_45_,
    private val saveUseCase: GenSaveUseCase_45_,
    private val deleteUseCase: GenDeleteUseCase_45_,
    private val searchUseCase: GenSearchUseCase_45_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_45_>(GenState_45_.Idle)
    val state: StateFlow<GenState_45_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_45_) {
        when (event) {
            is GenEvent_45_.Load -> loadAll()
            is GenEvent_45_.Update -> save(event.model)
            is GenEvent_45_.Delete -> delete(event.id)
            is GenEvent_45_.Refresh -> loadAll()
            is GenEvent_45_.Search -> search(event.query)
            is GenEvent_45_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_45_.Loading; _state.value = GenState_45_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_45_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_45_.Success(searchUseCase(query)) } }
}
