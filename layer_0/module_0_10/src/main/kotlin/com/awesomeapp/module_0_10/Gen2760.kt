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

data class GenModel_2760_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2760_ {
    data class Load(val id: Long) : GenEvent_2760_()
    data class Update(val model: GenModel_2760_) : GenEvent_2760_()
    data class Delete(val id: Long) : GenEvent_2760_()
    data object Refresh : GenEvent_2760_()
    data class Search(val query: String) : GenEvent_2760_()
    data class Filter(val predicate: String) : GenEvent_2760_()
}

sealed class GenState_2760_ {
    data object Idle : GenState_2760_()
    data object Loading : GenState_2760_()
    data class Success(val items: List<GenModel_2760_>) : GenState_2760_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2760_()
    data class Partial(val items: List<GenModel_2760_>, val hasMore: Boolean) : GenState_2760_()
}

interface GenRepository_2760_ {
    suspend fun getAll(): List<GenModel_2760_>
    suspend fun getById(id: Long): GenModel_2760_?
    suspend fun save(model: GenModel_2760_): GenModel_2760_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2760_>
}

@Singleton
class GenRepositoryImpl_2760_ @Inject constructor() : GenRepository_2760_ {
    private val store = mutableMapOf<Long, GenModel_2760_>()
    override suspend fun getAll(): List<GenModel_2760_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2760_? = store[id]
    override suspend fun save(model: GenModel_2760_): GenModel_2760_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2760_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2760_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2760_ @Inject constructor(
    private val repository: GenRepositoryImpl_2760_
) : GenUseCase_2760_<Unit, List<GenModel_2760_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2760_> = repository.getAll()
}

class GenSaveUseCase_2760_ @Inject constructor(
    private val repository: GenRepositoryImpl_2760_
) : GenUseCase_2760_<GenModel_2760_, GenModel_2760_> {
    override suspend fun invoke(params: GenModel_2760_): GenModel_2760_ = repository.save(params)
}

class GenDeleteUseCase_2760_ @Inject constructor(
    private val repository: GenRepositoryImpl_2760_
) : GenUseCase_2760_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2760_ @Inject constructor(
    private val repository: GenRepositoryImpl_2760_
) : GenUseCase_2760_<String, List<GenModel_2760_>> {
    override suspend fun invoke(params: String): List<GenModel_2760_> = repository.search(params)
}

abstract class GenMapper_2760_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2760_ : GenMapper_2760_<GenModel_2760_, String>() {
    override fun map(input: GenModel_2760_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2760_ : GenMapper_2760_<String, GenModel_2760_>() {
    override fun map(input: String): GenModel_2760_ {
        val parts = input.split(":")
        return GenModel_2760_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2760_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2760_,
    private val saveUseCase: GenSaveUseCase_2760_,
    private val deleteUseCase: GenDeleteUseCase_2760_,
    private val searchUseCase: GenSearchUseCase_2760_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2760_>(GenState_2760_.Idle)
    val state: StateFlow<GenState_2760_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2760_) {
        when (event) {
            is GenEvent_2760_.Load -> loadAll()
            is GenEvent_2760_.Update -> save(event.model)
            is GenEvent_2760_.Delete -> delete(event.id)
            is GenEvent_2760_.Refresh -> loadAll()
            is GenEvent_2760_.Search -> search(event.query)
            is GenEvent_2760_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2760_.Loading; _state.value = GenState_2760_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2760_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2760_.Success(searchUseCase(query)) } }
}
