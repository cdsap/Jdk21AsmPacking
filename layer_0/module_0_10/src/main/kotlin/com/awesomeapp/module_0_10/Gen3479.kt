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

data class GenModel_3479_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3479_ {
    data class Load(val id: Long) : GenEvent_3479_()
    data class Update(val model: GenModel_3479_) : GenEvent_3479_()
    data class Delete(val id: Long) : GenEvent_3479_()
    data object Refresh : GenEvent_3479_()
    data class Search(val query: String) : GenEvent_3479_()
    data class Filter(val predicate: String) : GenEvent_3479_()
}

sealed class GenState_3479_ {
    data object Idle : GenState_3479_()
    data object Loading : GenState_3479_()
    data class Success(val items: List<GenModel_3479_>) : GenState_3479_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3479_()
    data class Partial(val items: List<GenModel_3479_>, val hasMore: Boolean) : GenState_3479_()
}

interface GenRepository_3479_ {
    suspend fun getAll(): List<GenModel_3479_>
    suspend fun getById(id: Long): GenModel_3479_?
    suspend fun save(model: GenModel_3479_): GenModel_3479_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3479_>
}

@Singleton
class GenRepositoryImpl_3479_ @Inject constructor() : GenRepository_3479_ {
    private val store = mutableMapOf<Long, GenModel_3479_>()
    override suspend fun getAll(): List<GenModel_3479_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3479_? = store[id]
    override suspend fun save(model: GenModel_3479_): GenModel_3479_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3479_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3479_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3479_ @Inject constructor(
    private val repository: GenRepositoryImpl_3479_
) : GenUseCase_3479_<Unit, List<GenModel_3479_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3479_> = repository.getAll()
}

class GenSaveUseCase_3479_ @Inject constructor(
    private val repository: GenRepositoryImpl_3479_
) : GenUseCase_3479_<GenModel_3479_, GenModel_3479_> {
    override suspend fun invoke(params: GenModel_3479_): GenModel_3479_ = repository.save(params)
}

class GenDeleteUseCase_3479_ @Inject constructor(
    private val repository: GenRepositoryImpl_3479_
) : GenUseCase_3479_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3479_ @Inject constructor(
    private val repository: GenRepositoryImpl_3479_
) : GenUseCase_3479_<String, List<GenModel_3479_>> {
    override suspend fun invoke(params: String): List<GenModel_3479_> = repository.search(params)
}

abstract class GenMapper_3479_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3479_ : GenMapper_3479_<GenModel_3479_, String>() {
    override fun map(input: GenModel_3479_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3479_ : GenMapper_3479_<String, GenModel_3479_>() {
    override fun map(input: String): GenModel_3479_ {
        val parts = input.split(":")
        return GenModel_3479_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3479_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3479_,
    private val saveUseCase: GenSaveUseCase_3479_,
    private val deleteUseCase: GenDeleteUseCase_3479_,
    private val searchUseCase: GenSearchUseCase_3479_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3479_>(GenState_3479_.Idle)
    val state: StateFlow<GenState_3479_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3479_) {
        when (event) {
            is GenEvent_3479_.Load -> loadAll()
            is GenEvent_3479_.Update -> save(event.model)
            is GenEvent_3479_.Delete -> delete(event.id)
            is GenEvent_3479_.Refresh -> loadAll()
            is GenEvent_3479_.Search -> search(event.query)
            is GenEvent_3479_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3479_.Loading; _state.value = GenState_3479_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3479_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3479_.Success(searchUseCase(query)) } }
}
