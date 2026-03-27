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

data class GenModel_882_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_882_ {
    data class Load(val id: Long) : GenEvent_882_()
    data class Update(val model: GenModel_882_) : GenEvent_882_()
    data class Delete(val id: Long) : GenEvent_882_()
    data object Refresh : GenEvent_882_()
    data class Search(val query: String) : GenEvent_882_()
    data class Filter(val predicate: String) : GenEvent_882_()
}

sealed class GenState_882_ {
    data object Idle : GenState_882_()
    data object Loading : GenState_882_()
    data class Success(val items: List<GenModel_882_>) : GenState_882_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_882_()
    data class Partial(val items: List<GenModel_882_>, val hasMore: Boolean) : GenState_882_()
}

interface GenRepository_882_ {
    suspend fun getAll(): List<GenModel_882_>
    suspend fun getById(id: Long): GenModel_882_?
    suspend fun save(model: GenModel_882_): GenModel_882_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_882_>
}

@Singleton
class GenRepositoryImpl_882_ @Inject constructor() : GenRepository_882_ {
    private val store = mutableMapOf<Long, GenModel_882_>()
    override suspend fun getAll(): List<GenModel_882_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_882_? = store[id]
    override suspend fun save(model: GenModel_882_): GenModel_882_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_882_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_882_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_882_ @Inject constructor(
    private val repository: GenRepositoryImpl_882_
) : GenUseCase_882_<Unit, List<GenModel_882_>> {
    override suspend fun invoke(params: Unit): List<GenModel_882_> = repository.getAll()
}

class GenSaveUseCase_882_ @Inject constructor(
    private val repository: GenRepositoryImpl_882_
) : GenUseCase_882_<GenModel_882_, GenModel_882_> {
    override suspend fun invoke(params: GenModel_882_): GenModel_882_ = repository.save(params)
}

class GenDeleteUseCase_882_ @Inject constructor(
    private val repository: GenRepositoryImpl_882_
) : GenUseCase_882_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_882_ @Inject constructor(
    private val repository: GenRepositoryImpl_882_
) : GenUseCase_882_<String, List<GenModel_882_>> {
    override suspend fun invoke(params: String): List<GenModel_882_> = repository.search(params)
}

abstract class GenMapper_882_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_882_ : GenMapper_882_<GenModel_882_, String>() {
    override fun map(input: GenModel_882_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_882_ : GenMapper_882_<String, GenModel_882_>() {
    override fun map(input: String): GenModel_882_ {
        val parts = input.split(":")
        return GenModel_882_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_882_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_882_,
    private val saveUseCase: GenSaveUseCase_882_,
    private val deleteUseCase: GenDeleteUseCase_882_,
    private val searchUseCase: GenSearchUseCase_882_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_882_>(GenState_882_.Idle)
    val state: StateFlow<GenState_882_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_882_) {
        when (event) {
            is GenEvent_882_.Load -> loadAll()
            is GenEvent_882_.Update -> save(event.model)
            is GenEvent_882_.Delete -> delete(event.id)
            is GenEvent_882_.Refresh -> loadAll()
            is GenEvent_882_.Search -> search(event.query)
            is GenEvent_882_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_882_.Loading; _state.value = GenState_882_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_882_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_882_.Success(searchUseCase(query)) } }
}
