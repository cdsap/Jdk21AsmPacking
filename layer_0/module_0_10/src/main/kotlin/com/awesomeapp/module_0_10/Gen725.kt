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

data class GenModel_725_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_725_ {
    data class Load(val id: Long) : GenEvent_725_()
    data class Update(val model: GenModel_725_) : GenEvent_725_()
    data class Delete(val id: Long) : GenEvent_725_()
    data object Refresh : GenEvent_725_()
    data class Search(val query: String) : GenEvent_725_()
    data class Filter(val predicate: String) : GenEvent_725_()
}

sealed class GenState_725_ {
    data object Idle : GenState_725_()
    data object Loading : GenState_725_()
    data class Success(val items: List<GenModel_725_>) : GenState_725_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_725_()
    data class Partial(val items: List<GenModel_725_>, val hasMore: Boolean) : GenState_725_()
}

interface GenRepository_725_ {
    suspend fun getAll(): List<GenModel_725_>
    suspend fun getById(id: Long): GenModel_725_?
    suspend fun save(model: GenModel_725_): GenModel_725_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_725_>
}

@Singleton
class GenRepositoryImpl_725_ @Inject constructor() : GenRepository_725_ {
    private val store = mutableMapOf<Long, GenModel_725_>()
    override suspend fun getAll(): List<GenModel_725_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_725_? = store[id]
    override suspend fun save(model: GenModel_725_): GenModel_725_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_725_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_725_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_725_ @Inject constructor(
    private val repository: GenRepositoryImpl_725_
) : GenUseCase_725_<Unit, List<GenModel_725_>> {
    override suspend fun invoke(params: Unit): List<GenModel_725_> = repository.getAll()
}

class GenSaveUseCase_725_ @Inject constructor(
    private val repository: GenRepositoryImpl_725_
) : GenUseCase_725_<GenModel_725_, GenModel_725_> {
    override suspend fun invoke(params: GenModel_725_): GenModel_725_ = repository.save(params)
}

class GenDeleteUseCase_725_ @Inject constructor(
    private val repository: GenRepositoryImpl_725_
) : GenUseCase_725_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_725_ @Inject constructor(
    private val repository: GenRepositoryImpl_725_
) : GenUseCase_725_<String, List<GenModel_725_>> {
    override suspend fun invoke(params: String): List<GenModel_725_> = repository.search(params)
}

abstract class GenMapper_725_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_725_ : GenMapper_725_<GenModel_725_, String>() {
    override fun map(input: GenModel_725_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_725_ : GenMapper_725_<String, GenModel_725_>() {
    override fun map(input: String): GenModel_725_ {
        val parts = input.split(":")
        return GenModel_725_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_725_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_725_,
    private val saveUseCase: GenSaveUseCase_725_,
    private val deleteUseCase: GenDeleteUseCase_725_,
    private val searchUseCase: GenSearchUseCase_725_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_725_>(GenState_725_.Idle)
    val state: StateFlow<GenState_725_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_725_) {
        when (event) {
            is GenEvent_725_.Load -> loadAll()
            is GenEvent_725_.Update -> save(event.model)
            is GenEvent_725_.Delete -> delete(event.id)
            is GenEvent_725_.Refresh -> loadAll()
            is GenEvent_725_.Search -> search(event.query)
            is GenEvent_725_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_725_.Loading; _state.value = GenState_725_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_725_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_725_.Success(searchUseCase(query)) } }
}
