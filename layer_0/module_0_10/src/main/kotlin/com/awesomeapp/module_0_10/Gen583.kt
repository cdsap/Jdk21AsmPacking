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

data class GenModel_583_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_583_ {
    data class Load(val id: Long) : GenEvent_583_()
    data class Update(val model: GenModel_583_) : GenEvent_583_()
    data class Delete(val id: Long) : GenEvent_583_()
    data object Refresh : GenEvent_583_()
    data class Search(val query: String) : GenEvent_583_()
    data class Filter(val predicate: String) : GenEvent_583_()
}

sealed class GenState_583_ {
    data object Idle : GenState_583_()
    data object Loading : GenState_583_()
    data class Success(val items: List<GenModel_583_>) : GenState_583_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_583_()
    data class Partial(val items: List<GenModel_583_>, val hasMore: Boolean) : GenState_583_()
}

interface GenRepository_583_ {
    suspend fun getAll(): List<GenModel_583_>
    suspend fun getById(id: Long): GenModel_583_?
    suspend fun save(model: GenModel_583_): GenModel_583_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_583_>
}

@Singleton
class GenRepositoryImpl_583_ @Inject constructor() : GenRepository_583_ {
    private val store = mutableMapOf<Long, GenModel_583_>()
    override suspend fun getAll(): List<GenModel_583_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_583_? = store[id]
    override suspend fun save(model: GenModel_583_): GenModel_583_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_583_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_583_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_583_ @Inject constructor(
    private val repository: GenRepositoryImpl_583_
) : GenUseCase_583_<Unit, List<GenModel_583_>> {
    override suspend fun invoke(params: Unit): List<GenModel_583_> = repository.getAll()
}

class GenSaveUseCase_583_ @Inject constructor(
    private val repository: GenRepositoryImpl_583_
) : GenUseCase_583_<GenModel_583_, GenModel_583_> {
    override suspend fun invoke(params: GenModel_583_): GenModel_583_ = repository.save(params)
}

class GenDeleteUseCase_583_ @Inject constructor(
    private val repository: GenRepositoryImpl_583_
) : GenUseCase_583_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_583_ @Inject constructor(
    private val repository: GenRepositoryImpl_583_
) : GenUseCase_583_<String, List<GenModel_583_>> {
    override suspend fun invoke(params: String): List<GenModel_583_> = repository.search(params)
}

abstract class GenMapper_583_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_583_ : GenMapper_583_<GenModel_583_, String>() {
    override fun map(input: GenModel_583_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_583_ : GenMapper_583_<String, GenModel_583_>() {
    override fun map(input: String): GenModel_583_ {
        val parts = input.split(":")
        return GenModel_583_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_583_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_583_,
    private val saveUseCase: GenSaveUseCase_583_,
    private val deleteUseCase: GenDeleteUseCase_583_,
    private val searchUseCase: GenSearchUseCase_583_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_583_>(GenState_583_.Idle)
    val state: StateFlow<GenState_583_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_583_) {
        when (event) {
            is GenEvent_583_.Load -> loadAll()
            is GenEvent_583_.Update -> save(event.model)
            is GenEvent_583_.Delete -> delete(event.id)
            is GenEvent_583_.Refresh -> loadAll()
            is GenEvent_583_.Search -> search(event.query)
            is GenEvent_583_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_583_.Loading; _state.value = GenState_583_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_583_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_583_.Success(searchUseCase(query)) } }
}
