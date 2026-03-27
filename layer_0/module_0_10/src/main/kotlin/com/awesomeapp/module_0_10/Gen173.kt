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

data class GenModel_173_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_173_ {
    data class Load(val id: Long) : GenEvent_173_()
    data class Update(val model: GenModel_173_) : GenEvent_173_()
    data class Delete(val id: Long) : GenEvent_173_()
    data object Refresh : GenEvent_173_()
    data class Search(val query: String) : GenEvent_173_()
    data class Filter(val predicate: String) : GenEvent_173_()
}

sealed class GenState_173_ {
    data object Idle : GenState_173_()
    data object Loading : GenState_173_()
    data class Success(val items: List<GenModel_173_>) : GenState_173_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_173_()
    data class Partial(val items: List<GenModel_173_>, val hasMore: Boolean) : GenState_173_()
}

interface GenRepository_173_ {
    suspend fun getAll(): List<GenModel_173_>
    suspend fun getById(id: Long): GenModel_173_?
    suspend fun save(model: GenModel_173_): GenModel_173_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_173_>
}

@Singleton
class GenRepositoryImpl_173_ @Inject constructor() : GenRepository_173_ {
    private val store = mutableMapOf<Long, GenModel_173_>()
    override suspend fun getAll(): List<GenModel_173_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_173_? = store[id]
    override suspend fun save(model: GenModel_173_): GenModel_173_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_173_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_173_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_173_ @Inject constructor(
    private val repository: GenRepositoryImpl_173_
) : GenUseCase_173_<Unit, List<GenModel_173_>> {
    override suspend fun invoke(params: Unit): List<GenModel_173_> = repository.getAll()
}

class GenSaveUseCase_173_ @Inject constructor(
    private val repository: GenRepositoryImpl_173_
) : GenUseCase_173_<GenModel_173_, GenModel_173_> {
    override suspend fun invoke(params: GenModel_173_): GenModel_173_ = repository.save(params)
}

class GenDeleteUseCase_173_ @Inject constructor(
    private val repository: GenRepositoryImpl_173_
) : GenUseCase_173_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_173_ @Inject constructor(
    private val repository: GenRepositoryImpl_173_
) : GenUseCase_173_<String, List<GenModel_173_>> {
    override suspend fun invoke(params: String): List<GenModel_173_> = repository.search(params)
}

abstract class GenMapper_173_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_173_ : GenMapper_173_<GenModel_173_, String>() {
    override fun map(input: GenModel_173_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_173_ : GenMapper_173_<String, GenModel_173_>() {
    override fun map(input: String): GenModel_173_ {
        val parts = input.split(":")
        return GenModel_173_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_173_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_173_,
    private val saveUseCase: GenSaveUseCase_173_,
    private val deleteUseCase: GenDeleteUseCase_173_,
    private val searchUseCase: GenSearchUseCase_173_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_173_>(GenState_173_.Idle)
    val state: StateFlow<GenState_173_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_173_) {
        when (event) {
            is GenEvent_173_.Load -> loadAll()
            is GenEvent_173_.Update -> save(event.model)
            is GenEvent_173_.Delete -> delete(event.id)
            is GenEvent_173_.Refresh -> loadAll()
            is GenEvent_173_.Search -> search(event.query)
            is GenEvent_173_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_173_.Loading; _state.value = GenState_173_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_173_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_173_.Success(searchUseCase(query)) } }
}
