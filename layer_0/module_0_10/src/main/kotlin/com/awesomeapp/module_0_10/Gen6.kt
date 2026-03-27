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

data class GenModel_6_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_6_ {
    data class Load(val id: Long) : GenEvent_6_()
    data class Update(val model: GenModel_6_) : GenEvent_6_()
    data class Delete(val id: Long) : GenEvent_6_()
    data object Refresh : GenEvent_6_()
    data class Search(val query: String) : GenEvent_6_()
    data class Filter(val predicate: String) : GenEvent_6_()
}

sealed class GenState_6_ {
    data object Idle : GenState_6_()
    data object Loading : GenState_6_()
    data class Success(val items: List<GenModel_6_>) : GenState_6_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_6_()
    data class Partial(val items: List<GenModel_6_>, val hasMore: Boolean) : GenState_6_()
}

interface GenRepository_6_ {
    suspend fun getAll(): List<GenModel_6_>
    suspend fun getById(id: Long): GenModel_6_?
    suspend fun save(model: GenModel_6_): GenModel_6_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_6_>
}

@Singleton
class GenRepositoryImpl_6_ @Inject constructor() : GenRepository_6_ {
    private val store = mutableMapOf<Long, GenModel_6_>()
    override suspend fun getAll(): List<GenModel_6_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_6_? = store[id]
    override suspend fun save(model: GenModel_6_): GenModel_6_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_6_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_6_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_6_ @Inject constructor(
    private val repository: GenRepositoryImpl_6_
) : GenUseCase_6_<Unit, List<GenModel_6_>> {
    override suspend fun invoke(params: Unit): List<GenModel_6_> = repository.getAll()
}

class GenSaveUseCase_6_ @Inject constructor(
    private val repository: GenRepositoryImpl_6_
) : GenUseCase_6_<GenModel_6_, GenModel_6_> {
    override suspend fun invoke(params: GenModel_6_): GenModel_6_ = repository.save(params)
}

class GenDeleteUseCase_6_ @Inject constructor(
    private val repository: GenRepositoryImpl_6_
) : GenUseCase_6_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_6_ @Inject constructor(
    private val repository: GenRepositoryImpl_6_
) : GenUseCase_6_<String, List<GenModel_6_>> {
    override suspend fun invoke(params: String): List<GenModel_6_> = repository.search(params)
}

abstract class GenMapper_6_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_6_ : GenMapper_6_<GenModel_6_, String>() {
    override fun map(input: GenModel_6_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_6_ : GenMapper_6_<String, GenModel_6_>() {
    override fun map(input: String): GenModel_6_ {
        val parts = input.split(":")
        return GenModel_6_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_6_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_6_,
    private val saveUseCase: GenSaveUseCase_6_,
    private val deleteUseCase: GenDeleteUseCase_6_,
    private val searchUseCase: GenSearchUseCase_6_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_6_>(GenState_6_.Idle)
    val state: StateFlow<GenState_6_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_6_) {
        when (event) {
            is GenEvent_6_.Load -> loadAll()
            is GenEvent_6_.Update -> save(event.model)
            is GenEvent_6_.Delete -> delete(event.id)
            is GenEvent_6_.Refresh -> loadAll()
            is GenEvent_6_.Search -> search(event.query)
            is GenEvent_6_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_6_.Loading; _state.value = GenState_6_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_6_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_6_.Success(searchUseCase(query)) } }
}
