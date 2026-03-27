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

data class GenModel_600_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_600_ {
    data class Load(val id: Long) : GenEvent_600_()
    data class Update(val model: GenModel_600_) : GenEvent_600_()
    data class Delete(val id: Long) : GenEvent_600_()
    data object Refresh : GenEvent_600_()
    data class Search(val query: String) : GenEvent_600_()
    data class Filter(val predicate: String) : GenEvent_600_()
}

sealed class GenState_600_ {
    data object Idle : GenState_600_()
    data object Loading : GenState_600_()
    data class Success(val items: List<GenModel_600_>) : GenState_600_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_600_()
    data class Partial(val items: List<GenModel_600_>, val hasMore: Boolean) : GenState_600_()
}

interface GenRepository_600_ {
    suspend fun getAll(): List<GenModel_600_>
    suspend fun getById(id: Long): GenModel_600_?
    suspend fun save(model: GenModel_600_): GenModel_600_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_600_>
}

@Singleton
class GenRepositoryImpl_600_ @Inject constructor() : GenRepository_600_ {
    private val store = mutableMapOf<Long, GenModel_600_>()
    override suspend fun getAll(): List<GenModel_600_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_600_? = store[id]
    override suspend fun save(model: GenModel_600_): GenModel_600_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_600_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_600_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_600_ @Inject constructor(
    private val repository: GenRepositoryImpl_600_
) : GenUseCase_600_<Unit, List<GenModel_600_>> {
    override suspend fun invoke(params: Unit): List<GenModel_600_> = repository.getAll()
}

class GenSaveUseCase_600_ @Inject constructor(
    private val repository: GenRepositoryImpl_600_
) : GenUseCase_600_<GenModel_600_, GenModel_600_> {
    override suspend fun invoke(params: GenModel_600_): GenModel_600_ = repository.save(params)
}

class GenDeleteUseCase_600_ @Inject constructor(
    private val repository: GenRepositoryImpl_600_
) : GenUseCase_600_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_600_ @Inject constructor(
    private val repository: GenRepositoryImpl_600_
) : GenUseCase_600_<String, List<GenModel_600_>> {
    override suspend fun invoke(params: String): List<GenModel_600_> = repository.search(params)
}

abstract class GenMapper_600_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_600_ : GenMapper_600_<GenModel_600_, String>() {
    override fun map(input: GenModel_600_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_600_ : GenMapper_600_<String, GenModel_600_>() {
    override fun map(input: String): GenModel_600_ {
        val parts = input.split(":")
        return GenModel_600_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_600_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_600_,
    private val saveUseCase: GenSaveUseCase_600_,
    private val deleteUseCase: GenDeleteUseCase_600_,
    private val searchUseCase: GenSearchUseCase_600_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_600_>(GenState_600_.Idle)
    val state: StateFlow<GenState_600_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_600_) {
        when (event) {
            is GenEvent_600_.Load -> loadAll()
            is GenEvent_600_.Update -> save(event.model)
            is GenEvent_600_.Delete -> delete(event.id)
            is GenEvent_600_.Refresh -> loadAll()
            is GenEvent_600_.Search -> search(event.query)
            is GenEvent_600_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_600_.Loading; _state.value = GenState_600_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_600_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_600_.Success(searchUseCase(query)) } }
}
