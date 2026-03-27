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

data class GenModel_98_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_98_ {
    data class Load(val id: Long) : GenEvent_98_()
    data class Update(val model: GenModel_98_) : GenEvent_98_()
    data class Delete(val id: Long) : GenEvent_98_()
    data object Refresh : GenEvent_98_()
    data class Search(val query: String) : GenEvent_98_()
    data class Filter(val predicate: String) : GenEvent_98_()
}

sealed class GenState_98_ {
    data object Idle : GenState_98_()
    data object Loading : GenState_98_()
    data class Success(val items: List<GenModel_98_>) : GenState_98_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_98_()
    data class Partial(val items: List<GenModel_98_>, val hasMore: Boolean) : GenState_98_()
}

interface GenRepository_98_ {
    suspend fun getAll(): List<GenModel_98_>
    suspend fun getById(id: Long): GenModel_98_?
    suspend fun save(model: GenModel_98_): GenModel_98_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_98_>
}

@Singleton
class GenRepositoryImpl_98_ @Inject constructor() : GenRepository_98_ {
    private val store = mutableMapOf<Long, GenModel_98_>()
    override suspend fun getAll(): List<GenModel_98_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_98_? = store[id]
    override suspend fun save(model: GenModel_98_): GenModel_98_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_98_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_98_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_98_ @Inject constructor(
    private val repository: GenRepositoryImpl_98_
) : GenUseCase_98_<Unit, List<GenModel_98_>> {
    override suspend fun invoke(params: Unit): List<GenModel_98_> = repository.getAll()
}

class GenSaveUseCase_98_ @Inject constructor(
    private val repository: GenRepositoryImpl_98_
) : GenUseCase_98_<GenModel_98_, GenModel_98_> {
    override suspend fun invoke(params: GenModel_98_): GenModel_98_ = repository.save(params)
}

class GenDeleteUseCase_98_ @Inject constructor(
    private val repository: GenRepositoryImpl_98_
) : GenUseCase_98_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_98_ @Inject constructor(
    private val repository: GenRepositoryImpl_98_
) : GenUseCase_98_<String, List<GenModel_98_>> {
    override suspend fun invoke(params: String): List<GenModel_98_> = repository.search(params)
}

abstract class GenMapper_98_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_98_ : GenMapper_98_<GenModel_98_, String>() {
    override fun map(input: GenModel_98_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_98_ : GenMapper_98_<String, GenModel_98_>() {
    override fun map(input: String): GenModel_98_ {
        val parts = input.split(":")
        return GenModel_98_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_98_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_98_,
    private val saveUseCase: GenSaveUseCase_98_,
    private val deleteUseCase: GenDeleteUseCase_98_,
    private val searchUseCase: GenSearchUseCase_98_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_98_>(GenState_98_.Idle)
    val state: StateFlow<GenState_98_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_98_) {
        when (event) {
            is GenEvent_98_.Load -> loadAll()
            is GenEvent_98_.Update -> save(event.model)
            is GenEvent_98_.Delete -> delete(event.id)
            is GenEvent_98_.Refresh -> loadAll()
            is GenEvent_98_.Search -> search(event.query)
            is GenEvent_98_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_98_.Loading; _state.value = GenState_98_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_98_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_98_.Success(searchUseCase(query)) } }
}
