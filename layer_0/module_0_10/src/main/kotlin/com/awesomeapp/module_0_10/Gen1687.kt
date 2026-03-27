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

data class GenModel_1687_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1687_ {
    data class Load(val id: Long) : GenEvent_1687_()
    data class Update(val model: GenModel_1687_) : GenEvent_1687_()
    data class Delete(val id: Long) : GenEvent_1687_()
    data object Refresh : GenEvent_1687_()
    data class Search(val query: String) : GenEvent_1687_()
    data class Filter(val predicate: String) : GenEvent_1687_()
}

sealed class GenState_1687_ {
    data object Idle : GenState_1687_()
    data object Loading : GenState_1687_()
    data class Success(val items: List<GenModel_1687_>) : GenState_1687_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1687_()
    data class Partial(val items: List<GenModel_1687_>, val hasMore: Boolean) : GenState_1687_()
}

interface GenRepository_1687_ {
    suspend fun getAll(): List<GenModel_1687_>
    suspend fun getById(id: Long): GenModel_1687_?
    suspend fun save(model: GenModel_1687_): GenModel_1687_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1687_>
}

@Singleton
class GenRepositoryImpl_1687_ @Inject constructor() : GenRepository_1687_ {
    private val store = mutableMapOf<Long, GenModel_1687_>()
    override suspend fun getAll(): List<GenModel_1687_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1687_? = store[id]
    override suspend fun save(model: GenModel_1687_): GenModel_1687_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1687_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1687_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1687_ @Inject constructor(
    private val repository: GenRepositoryImpl_1687_
) : GenUseCase_1687_<Unit, List<GenModel_1687_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1687_> = repository.getAll()
}

class GenSaveUseCase_1687_ @Inject constructor(
    private val repository: GenRepositoryImpl_1687_
) : GenUseCase_1687_<GenModel_1687_, GenModel_1687_> {
    override suspend fun invoke(params: GenModel_1687_): GenModel_1687_ = repository.save(params)
}

class GenDeleteUseCase_1687_ @Inject constructor(
    private val repository: GenRepositoryImpl_1687_
) : GenUseCase_1687_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1687_ @Inject constructor(
    private val repository: GenRepositoryImpl_1687_
) : GenUseCase_1687_<String, List<GenModel_1687_>> {
    override suspend fun invoke(params: String): List<GenModel_1687_> = repository.search(params)
}

abstract class GenMapper_1687_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1687_ : GenMapper_1687_<GenModel_1687_, String>() {
    override fun map(input: GenModel_1687_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1687_ : GenMapper_1687_<String, GenModel_1687_>() {
    override fun map(input: String): GenModel_1687_ {
        val parts = input.split(":")
        return GenModel_1687_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1687_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1687_,
    private val saveUseCase: GenSaveUseCase_1687_,
    private val deleteUseCase: GenDeleteUseCase_1687_,
    private val searchUseCase: GenSearchUseCase_1687_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1687_>(GenState_1687_.Idle)
    val state: StateFlow<GenState_1687_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1687_) {
        when (event) {
            is GenEvent_1687_.Load -> loadAll()
            is GenEvent_1687_.Update -> save(event.model)
            is GenEvent_1687_.Delete -> delete(event.id)
            is GenEvent_1687_.Refresh -> loadAll()
            is GenEvent_1687_.Search -> search(event.query)
            is GenEvent_1687_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1687_.Loading; _state.value = GenState_1687_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1687_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1687_.Success(searchUseCase(query)) } }
}
