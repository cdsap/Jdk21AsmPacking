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

data class GenModel_1573_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1573_ {
    data class Load(val id: Long) : GenEvent_1573_()
    data class Update(val model: GenModel_1573_) : GenEvent_1573_()
    data class Delete(val id: Long) : GenEvent_1573_()
    data object Refresh : GenEvent_1573_()
    data class Search(val query: String) : GenEvent_1573_()
    data class Filter(val predicate: String) : GenEvent_1573_()
}

sealed class GenState_1573_ {
    data object Idle : GenState_1573_()
    data object Loading : GenState_1573_()
    data class Success(val items: List<GenModel_1573_>) : GenState_1573_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1573_()
    data class Partial(val items: List<GenModel_1573_>, val hasMore: Boolean) : GenState_1573_()
}

interface GenRepository_1573_ {
    suspend fun getAll(): List<GenModel_1573_>
    suspend fun getById(id: Long): GenModel_1573_?
    suspend fun save(model: GenModel_1573_): GenModel_1573_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1573_>
}

@Singleton
class GenRepositoryImpl_1573_ @Inject constructor() : GenRepository_1573_ {
    private val store = mutableMapOf<Long, GenModel_1573_>()
    override suspend fun getAll(): List<GenModel_1573_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1573_? = store[id]
    override suspend fun save(model: GenModel_1573_): GenModel_1573_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1573_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1573_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1573_ @Inject constructor(
    private val repository: GenRepositoryImpl_1573_
) : GenUseCase_1573_<Unit, List<GenModel_1573_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1573_> = repository.getAll()
}

class GenSaveUseCase_1573_ @Inject constructor(
    private val repository: GenRepositoryImpl_1573_
) : GenUseCase_1573_<GenModel_1573_, GenModel_1573_> {
    override suspend fun invoke(params: GenModel_1573_): GenModel_1573_ = repository.save(params)
}

class GenDeleteUseCase_1573_ @Inject constructor(
    private val repository: GenRepositoryImpl_1573_
) : GenUseCase_1573_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1573_ @Inject constructor(
    private val repository: GenRepositoryImpl_1573_
) : GenUseCase_1573_<String, List<GenModel_1573_>> {
    override suspend fun invoke(params: String): List<GenModel_1573_> = repository.search(params)
}

abstract class GenMapper_1573_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1573_ : GenMapper_1573_<GenModel_1573_, String>() {
    override fun map(input: GenModel_1573_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1573_ : GenMapper_1573_<String, GenModel_1573_>() {
    override fun map(input: String): GenModel_1573_ {
        val parts = input.split(":")
        return GenModel_1573_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1573_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1573_,
    private val saveUseCase: GenSaveUseCase_1573_,
    private val deleteUseCase: GenDeleteUseCase_1573_,
    private val searchUseCase: GenSearchUseCase_1573_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1573_>(GenState_1573_.Idle)
    val state: StateFlow<GenState_1573_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1573_) {
        when (event) {
            is GenEvent_1573_.Load -> loadAll()
            is GenEvent_1573_.Update -> save(event.model)
            is GenEvent_1573_.Delete -> delete(event.id)
            is GenEvent_1573_.Refresh -> loadAll()
            is GenEvent_1573_.Search -> search(event.query)
            is GenEvent_1573_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1573_.Loading; _state.value = GenState_1573_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1573_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1573_.Success(searchUseCase(query)) } }
}
