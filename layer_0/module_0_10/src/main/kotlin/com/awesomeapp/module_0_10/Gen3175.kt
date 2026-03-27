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

data class GenModel_3175_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3175_ {
    data class Load(val id: Long) : GenEvent_3175_()
    data class Update(val model: GenModel_3175_) : GenEvent_3175_()
    data class Delete(val id: Long) : GenEvent_3175_()
    data object Refresh : GenEvent_3175_()
    data class Search(val query: String) : GenEvent_3175_()
    data class Filter(val predicate: String) : GenEvent_3175_()
}

sealed class GenState_3175_ {
    data object Idle : GenState_3175_()
    data object Loading : GenState_3175_()
    data class Success(val items: List<GenModel_3175_>) : GenState_3175_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3175_()
    data class Partial(val items: List<GenModel_3175_>, val hasMore: Boolean) : GenState_3175_()
}

interface GenRepository_3175_ {
    suspend fun getAll(): List<GenModel_3175_>
    suspend fun getById(id: Long): GenModel_3175_?
    suspend fun save(model: GenModel_3175_): GenModel_3175_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3175_>
}

@Singleton
class GenRepositoryImpl_3175_ @Inject constructor() : GenRepository_3175_ {
    private val store = mutableMapOf<Long, GenModel_3175_>()
    override suspend fun getAll(): List<GenModel_3175_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3175_? = store[id]
    override suspend fun save(model: GenModel_3175_): GenModel_3175_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3175_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3175_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3175_ @Inject constructor(
    private val repository: GenRepositoryImpl_3175_
) : GenUseCase_3175_<Unit, List<GenModel_3175_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3175_> = repository.getAll()
}

class GenSaveUseCase_3175_ @Inject constructor(
    private val repository: GenRepositoryImpl_3175_
) : GenUseCase_3175_<GenModel_3175_, GenModel_3175_> {
    override suspend fun invoke(params: GenModel_3175_): GenModel_3175_ = repository.save(params)
}

class GenDeleteUseCase_3175_ @Inject constructor(
    private val repository: GenRepositoryImpl_3175_
) : GenUseCase_3175_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3175_ @Inject constructor(
    private val repository: GenRepositoryImpl_3175_
) : GenUseCase_3175_<String, List<GenModel_3175_>> {
    override suspend fun invoke(params: String): List<GenModel_3175_> = repository.search(params)
}

abstract class GenMapper_3175_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3175_ : GenMapper_3175_<GenModel_3175_, String>() {
    override fun map(input: GenModel_3175_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3175_ : GenMapper_3175_<String, GenModel_3175_>() {
    override fun map(input: String): GenModel_3175_ {
        val parts = input.split(":")
        return GenModel_3175_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3175_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3175_,
    private val saveUseCase: GenSaveUseCase_3175_,
    private val deleteUseCase: GenDeleteUseCase_3175_,
    private val searchUseCase: GenSearchUseCase_3175_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3175_>(GenState_3175_.Idle)
    val state: StateFlow<GenState_3175_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3175_) {
        when (event) {
            is GenEvent_3175_.Load -> loadAll()
            is GenEvent_3175_.Update -> save(event.model)
            is GenEvent_3175_.Delete -> delete(event.id)
            is GenEvent_3175_.Refresh -> loadAll()
            is GenEvent_3175_.Search -> search(event.query)
            is GenEvent_3175_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3175_.Loading; _state.value = GenState_3175_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3175_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3175_.Success(searchUseCase(query)) } }
}
