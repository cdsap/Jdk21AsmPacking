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

data class GenModel_573_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_573_ {
    data class Load(val id: Long) : GenEvent_573_()
    data class Update(val model: GenModel_573_) : GenEvent_573_()
    data class Delete(val id: Long) : GenEvent_573_()
    data object Refresh : GenEvent_573_()
    data class Search(val query: String) : GenEvent_573_()
    data class Filter(val predicate: String) : GenEvent_573_()
}

sealed class GenState_573_ {
    data object Idle : GenState_573_()
    data object Loading : GenState_573_()
    data class Success(val items: List<GenModel_573_>) : GenState_573_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_573_()
    data class Partial(val items: List<GenModel_573_>, val hasMore: Boolean) : GenState_573_()
}

interface GenRepository_573_ {
    suspend fun getAll(): List<GenModel_573_>
    suspend fun getById(id: Long): GenModel_573_?
    suspend fun save(model: GenModel_573_): GenModel_573_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_573_>
}

@Singleton
class GenRepositoryImpl_573_ @Inject constructor() : GenRepository_573_ {
    private val store = mutableMapOf<Long, GenModel_573_>()
    override suspend fun getAll(): List<GenModel_573_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_573_? = store[id]
    override suspend fun save(model: GenModel_573_): GenModel_573_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_573_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_573_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_573_ @Inject constructor(
    private val repository: GenRepositoryImpl_573_
) : GenUseCase_573_<Unit, List<GenModel_573_>> {
    override suspend fun invoke(params: Unit): List<GenModel_573_> = repository.getAll()
}

class GenSaveUseCase_573_ @Inject constructor(
    private val repository: GenRepositoryImpl_573_
) : GenUseCase_573_<GenModel_573_, GenModel_573_> {
    override suspend fun invoke(params: GenModel_573_): GenModel_573_ = repository.save(params)
}

class GenDeleteUseCase_573_ @Inject constructor(
    private val repository: GenRepositoryImpl_573_
) : GenUseCase_573_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_573_ @Inject constructor(
    private val repository: GenRepositoryImpl_573_
) : GenUseCase_573_<String, List<GenModel_573_>> {
    override suspend fun invoke(params: String): List<GenModel_573_> = repository.search(params)
}

abstract class GenMapper_573_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_573_ : GenMapper_573_<GenModel_573_, String>() {
    override fun map(input: GenModel_573_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_573_ : GenMapper_573_<String, GenModel_573_>() {
    override fun map(input: String): GenModel_573_ {
        val parts = input.split(":")
        return GenModel_573_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_573_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_573_,
    private val saveUseCase: GenSaveUseCase_573_,
    private val deleteUseCase: GenDeleteUseCase_573_,
    private val searchUseCase: GenSearchUseCase_573_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_573_>(GenState_573_.Idle)
    val state: StateFlow<GenState_573_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_573_) {
        when (event) {
            is GenEvent_573_.Load -> loadAll()
            is GenEvent_573_.Update -> save(event.model)
            is GenEvent_573_.Delete -> delete(event.id)
            is GenEvent_573_.Refresh -> loadAll()
            is GenEvent_573_.Search -> search(event.query)
            is GenEvent_573_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_573_.Loading; _state.value = GenState_573_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_573_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_573_.Success(searchUseCase(query)) } }
}
