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

data class GenModel_188_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_188_ {
    data class Load(val id: Long) : GenEvent_188_()
    data class Update(val model: GenModel_188_) : GenEvent_188_()
    data class Delete(val id: Long) : GenEvent_188_()
    data object Refresh : GenEvent_188_()
    data class Search(val query: String) : GenEvent_188_()
    data class Filter(val predicate: String) : GenEvent_188_()
}

sealed class GenState_188_ {
    data object Idle : GenState_188_()
    data object Loading : GenState_188_()
    data class Success(val items: List<GenModel_188_>) : GenState_188_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_188_()
    data class Partial(val items: List<GenModel_188_>, val hasMore: Boolean) : GenState_188_()
}

interface GenRepository_188_ {
    suspend fun getAll(): List<GenModel_188_>
    suspend fun getById(id: Long): GenModel_188_?
    suspend fun save(model: GenModel_188_): GenModel_188_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_188_>
}

@Singleton
class GenRepositoryImpl_188_ @Inject constructor() : GenRepository_188_ {
    private val store = mutableMapOf<Long, GenModel_188_>()
    override suspend fun getAll(): List<GenModel_188_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_188_? = store[id]
    override suspend fun save(model: GenModel_188_): GenModel_188_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_188_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_188_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_188_ @Inject constructor(
    private val repository: GenRepositoryImpl_188_
) : GenUseCase_188_<Unit, List<GenModel_188_>> {
    override suspend fun invoke(params: Unit): List<GenModel_188_> = repository.getAll()
}

class GenSaveUseCase_188_ @Inject constructor(
    private val repository: GenRepositoryImpl_188_
) : GenUseCase_188_<GenModel_188_, GenModel_188_> {
    override suspend fun invoke(params: GenModel_188_): GenModel_188_ = repository.save(params)
}

class GenDeleteUseCase_188_ @Inject constructor(
    private val repository: GenRepositoryImpl_188_
) : GenUseCase_188_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_188_ @Inject constructor(
    private val repository: GenRepositoryImpl_188_
) : GenUseCase_188_<String, List<GenModel_188_>> {
    override suspend fun invoke(params: String): List<GenModel_188_> = repository.search(params)
}

abstract class GenMapper_188_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_188_ : GenMapper_188_<GenModel_188_, String>() {
    override fun map(input: GenModel_188_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_188_ : GenMapper_188_<String, GenModel_188_>() {
    override fun map(input: String): GenModel_188_ {
        val parts = input.split(":")
        return GenModel_188_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_188_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_188_,
    private val saveUseCase: GenSaveUseCase_188_,
    private val deleteUseCase: GenDeleteUseCase_188_,
    private val searchUseCase: GenSearchUseCase_188_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_188_>(GenState_188_.Idle)
    val state: StateFlow<GenState_188_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_188_) {
        when (event) {
            is GenEvent_188_.Load -> loadAll()
            is GenEvent_188_.Update -> save(event.model)
            is GenEvent_188_.Delete -> delete(event.id)
            is GenEvent_188_.Refresh -> loadAll()
            is GenEvent_188_.Search -> search(event.query)
            is GenEvent_188_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_188_.Loading; _state.value = GenState_188_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_188_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_188_.Success(searchUseCase(query)) } }
}
