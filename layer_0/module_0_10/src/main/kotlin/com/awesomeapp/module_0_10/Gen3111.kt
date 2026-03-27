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

data class GenModel_3111_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3111_ {
    data class Load(val id: Long) : GenEvent_3111_()
    data class Update(val model: GenModel_3111_) : GenEvent_3111_()
    data class Delete(val id: Long) : GenEvent_3111_()
    data object Refresh : GenEvent_3111_()
    data class Search(val query: String) : GenEvent_3111_()
    data class Filter(val predicate: String) : GenEvent_3111_()
}

sealed class GenState_3111_ {
    data object Idle : GenState_3111_()
    data object Loading : GenState_3111_()
    data class Success(val items: List<GenModel_3111_>) : GenState_3111_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3111_()
    data class Partial(val items: List<GenModel_3111_>, val hasMore: Boolean) : GenState_3111_()
}

interface GenRepository_3111_ {
    suspend fun getAll(): List<GenModel_3111_>
    suspend fun getById(id: Long): GenModel_3111_?
    suspend fun save(model: GenModel_3111_): GenModel_3111_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3111_>
}

@Singleton
class GenRepositoryImpl_3111_ @Inject constructor() : GenRepository_3111_ {
    private val store = mutableMapOf<Long, GenModel_3111_>()
    override suspend fun getAll(): List<GenModel_3111_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3111_? = store[id]
    override suspend fun save(model: GenModel_3111_): GenModel_3111_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3111_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3111_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3111_ @Inject constructor(
    private val repository: GenRepositoryImpl_3111_
) : GenUseCase_3111_<Unit, List<GenModel_3111_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3111_> = repository.getAll()
}

class GenSaveUseCase_3111_ @Inject constructor(
    private val repository: GenRepositoryImpl_3111_
) : GenUseCase_3111_<GenModel_3111_, GenModel_3111_> {
    override suspend fun invoke(params: GenModel_3111_): GenModel_3111_ = repository.save(params)
}

class GenDeleteUseCase_3111_ @Inject constructor(
    private val repository: GenRepositoryImpl_3111_
) : GenUseCase_3111_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3111_ @Inject constructor(
    private val repository: GenRepositoryImpl_3111_
) : GenUseCase_3111_<String, List<GenModel_3111_>> {
    override suspend fun invoke(params: String): List<GenModel_3111_> = repository.search(params)
}

abstract class GenMapper_3111_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3111_ : GenMapper_3111_<GenModel_3111_, String>() {
    override fun map(input: GenModel_3111_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3111_ : GenMapper_3111_<String, GenModel_3111_>() {
    override fun map(input: String): GenModel_3111_ {
        val parts = input.split(":")
        return GenModel_3111_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3111_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3111_,
    private val saveUseCase: GenSaveUseCase_3111_,
    private val deleteUseCase: GenDeleteUseCase_3111_,
    private val searchUseCase: GenSearchUseCase_3111_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3111_>(GenState_3111_.Idle)
    val state: StateFlow<GenState_3111_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3111_) {
        when (event) {
            is GenEvent_3111_.Load -> loadAll()
            is GenEvent_3111_.Update -> save(event.model)
            is GenEvent_3111_.Delete -> delete(event.id)
            is GenEvent_3111_.Refresh -> loadAll()
            is GenEvent_3111_.Search -> search(event.query)
            is GenEvent_3111_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3111_.Loading; _state.value = GenState_3111_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3111_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3111_.Success(searchUseCase(query)) } }
}
