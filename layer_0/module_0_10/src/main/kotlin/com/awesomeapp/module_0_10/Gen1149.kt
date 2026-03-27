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

data class GenModel_1149_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1149_ {
    data class Load(val id: Long) : GenEvent_1149_()
    data class Update(val model: GenModel_1149_) : GenEvent_1149_()
    data class Delete(val id: Long) : GenEvent_1149_()
    data object Refresh : GenEvent_1149_()
    data class Search(val query: String) : GenEvent_1149_()
    data class Filter(val predicate: String) : GenEvent_1149_()
}

sealed class GenState_1149_ {
    data object Idle : GenState_1149_()
    data object Loading : GenState_1149_()
    data class Success(val items: List<GenModel_1149_>) : GenState_1149_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1149_()
    data class Partial(val items: List<GenModel_1149_>, val hasMore: Boolean) : GenState_1149_()
}

interface GenRepository_1149_ {
    suspend fun getAll(): List<GenModel_1149_>
    suspend fun getById(id: Long): GenModel_1149_?
    suspend fun save(model: GenModel_1149_): GenModel_1149_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1149_>
}

@Singleton
class GenRepositoryImpl_1149_ @Inject constructor() : GenRepository_1149_ {
    private val store = mutableMapOf<Long, GenModel_1149_>()
    override suspend fun getAll(): List<GenModel_1149_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1149_? = store[id]
    override suspend fun save(model: GenModel_1149_): GenModel_1149_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1149_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1149_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1149_ @Inject constructor(
    private val repository: GenRepositoryImpl_1149_
) : GenUseCase_1149_<Unit, List<GenModel_1149_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1149_> = repository.getAll()
}

class GenSaveUseCase_1149_ @Inject constructor(
    private val repository: GenRepositoryImpl_1149_
) : GenUseCase_1149_<GenModel_1149_, GenModel_1149_> {
    override suspend fun invoke(params: GenModel_1149_): GenModel_1149_ = repository.save(params)
}

class GenDeleteUseCase_1149_ @Inject constructor(
    private val repository: GenRepositoryImpl_1149_
) : GenUseCase_1149_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1149_ @Inject constructor(
    private val repository: GenRepositoryImpl_1149_
) : GenUseCase_1149_<String, List<GenModel_1149_>> {
    override suspend fun invoke(params: String): List<GenModel_1149_> = repository.search(params)
}

abstract class GenMapper_1149_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1149_ : GenMapper_1149_<GenModel_1149_, String>() {
    override fun map(input: GenModel_1149_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1149_ : GenMapper_1149_<String, GenModel_1149_>() {
    override fun map(input: String): GenModel_1149_ {
        val parts = input.split(":")
        return GenModel_1149_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1149_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1149_,
    private val saveUseCase: GenSaveUseCase_1149_,
    private val deleteUseCase: GenDeleteUseCase_1149_,
    private val searchUseCase: GenSearchUseCase_1149_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1149_>(GenState_1149_.Idle)
    val state: StateFlow<GenState_1149_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1149_) {
        when (event) {
            is GenEvent_1149_.Load -> loadAll()
            is GenEvent_1149_.Update -> save(event.model)
            is GenEvent_1149_.Delete -> delete(event.id)
            is GenEvent_1149_.Refresh -> loadAll()
            is GenEvent_1149_.Search -> search(event.query)
            is GenEvent_1149_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1149_.Loading; _state.value = GenState_1149_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1149_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1149_.Success(searchUseCase(query)) } }
}
