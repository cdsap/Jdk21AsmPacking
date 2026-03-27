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

data class GenModel_838_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_838_ {
    data class Load(val id: Long) : GenEvent_838_()
    data class Update(val model: GenModel_838_) : GenEvent_838_()
    data class Delete(val id: Long) : GenEvent_838_()
    data object Refresh : GenEvent_838_()
    data class Search(val query: String) : GenEvent_838_()
    data class Filter(val predicate: String) : GenEvent_838_()
}

sealed class GenState_838_ {
    data object Idle : GenState_838_()
    data object Loading : GenState_838_()
    data class Success(val items: List<GenModel_838_>) : GenState_838_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_838_()
    data class Partial(val items: List<GenModel_838_>, val hasMore: Boolean) : GenState_838_()
}

interface GenRepository_838_ {
    suspend fun getAll(): List<GenModel_838_>
    suspend fun getById(id: Long): GenModel_838_?
    suspend fun save(model: GenModel_838_): GenModel_838_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_838_>
}

@Singleton
class GenRepositoryImpl_838_ @Inject constructor() : GenRepository_838_ {
    private val store = mutableMapOf<Long, GenModel_838_>()
    override suspend fun getAll(): List<GenModel_838_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_838_? = store[id]
    override suspend fun save(model: GenModel_838_): GenModel_838_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_838_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_838_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_838_ @Inject constructor(
    private val repository: GenRepositoryImpl_838_
) : GenUseCase_838_<Unit, List<GenModel_838_>> {
    override suspend fun invoke(params: Unit): List<GenModel_838_> = repository.getAll()
}

class GenSaveUseCase_838_ @Inject constructor(
    private val repository: GenRepositoryImpl_838_
) : GenUseCase_838_<GenModel_838_, GenModel_838_> {
    override suspend fun invoke(params: GenModel_838_): GenModel_838_ = repository.save(params)
}

class GenDeleteUseCase_838_ @Inject constructor(
    private val repository: GenRepositoryImpl_838_
) : GenUseCase_838_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_838_ @Inject constructor(
    private val repository: GenRepositoryImpl_838_
) : GenUseCase_838_<String, List<GenModel_838_>> {
    override suspend fun invoke(params: String): List<GenModel_838_> = repository.search(params)
}

abstract class GenMapper_838_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_838_ : GenMapper_838_<GenModel_838_, String>() {
    override fun map(input: GenModel_838_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_838_ : GenMapper_838_<String, GenModel_838_>() {
    override fun map(input: String): GenModel_838_ {
        val parts = input.split(":")
        return GenModel_838_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_838_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_838_,
    private val saveUseCase: GenSaveUseCase_838_,
    private val deleteUseCase: GenDeleteUseCase_838_,
    private val searchUseCase: GenSearchUseCase_838_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_838_>(GenState_838_.Idle)
    val state: StateFlow<GenState_838_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_838_) {
        when (event) {
            is GenEvent_838_.Load -> loadAll()
            is GenEvent_838_.Update -> save(event.model)
            is GenEvent_838_.Delete -> delete(event.id)
            is GenEvent_838_.Refresh -> loadAll()
            is GenEvent_838_.Search -> search(event.query)
            is GenEvent_838_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_838_.Loading; _state.value = GenState_838_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_838_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_838_.Success(searchUseCase(query)) } }
}
