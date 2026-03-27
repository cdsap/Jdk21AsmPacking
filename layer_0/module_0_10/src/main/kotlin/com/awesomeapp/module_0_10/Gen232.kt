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

data class GenModel_232_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_232_ {
    data class Load(val id: Long) : GenEvent_232_()
    data class Update(val model: GenModel_232_) : GenEvent_232_()
    data class Delete(val id: Long) : GenEvent_232_()
    data object Refresh : GenEvent_232_()
    data class Search(val query: String) : GenEvent_232_()
    data class Filter(val predicate: String) : GenEvent_232_()
}

sealed class GenState_232_ {
    data object Idle : GenState_232_()
    data object Loading : GenState_232_()
    data class Success(val items: List<GenModel_232_>) : GenState_232_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_232_()
    data class Partial(val items: List<GenModel_232_>, val hasMore: Boolean) : GenState_232_()
}

interface GenRepository_232_ {
    suspend fun getAll(): List<GenModel_232_>
    suspend fun getById(id: Long): GenModel_232_?
    suspend fun save(model: GenModel_232_): GenModel_232_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_232_>
}

@Singleton
class GenRepositoryImpl_232_ @Inject constructor() : GenRepository_232_ {
    private val store = mutableMapOf<Long, GenModel_232_>()
    override suspend fun getAll(): List<GenModel_232_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_232_? = store[id]
    override suspend fun save(model: GenModel_232_): GenModel_232_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_232_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_232_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_232_ @Inject constructor(
    private val repository: GenRepositoryImpl_232_
) : GenUseCase_232_<Unit, List<GenModel_232_>> {
    override suspend fun invoke(params: Unit): List<GenModel_232_> = repository.getAll()
}

class GenSaveUseCase_232_ @Inject constructor(
    private val repository: GenRepositoryImpl_232_
) : GenUseCase_232_<GenModel_232_, GenModel_232_> {
    override suspend fun invoke(params: GenModel_232_): GenModel_232_ = repository.save(params)
}

class GenDeleteUseCase_232_ @Inject constructor(
    private val repository: GenRepositoryImpl_232_
) : GenUseCase_232_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_232_ @Inject constructor(
    private val repository: GenRepositoryImpl_232_
) : GenUseCase_232_<String, List<GenModel_232_>> {
    override suspend fun invoke(params: String): List<GenModel_232_> = repository.search(params)
}

abstract class GenMapper_232_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_232_ : GenMapper_232_<GenModel_232_, String>() {
    override fun map(input: GenModel_232_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_232_ : GenMapper_232_<String, GenModel_232_>() {
    override fun map(input: String): GenModel_232_ {
        val parts = input.split(":")
        return GenModel_232_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_232_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_232_,
    private val saveUseCase: GenSaveUseCase_232_,
    private val deleteUseCase: GenDeleteUseCase_232_,
    private val searchUseCase: GenSearchUseCase_232_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_232_>(GenState_232_.Idle)
    val state: StateFlow<GenState_232_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_232_) {
        when (event) {
            is GenEvent_232_.Load -> loadAll()
            is GenEvent_232_.Update -> save(event.model)
            is GenEvent_232_.Delete -> delete(event.id)
            is GenEvent_232_.Refresh -> loadAll()
            is GenEvent_232_.Search -> search(event.query)
            is GenEvent_232_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_232_.Loading; _state.value = GenState_232_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_232_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_232_.Success(searchUseCase(query)) } }
}
