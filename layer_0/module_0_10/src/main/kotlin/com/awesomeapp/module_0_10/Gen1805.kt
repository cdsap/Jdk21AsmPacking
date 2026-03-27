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

data class GenModel_1805_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1805_ {
    data class Load(val id: Long) : GenEvent_1805_()
    data class Update(val model: GenModel_1805_) : GenEvent_1805_()
    data class Delete(val id: Long) : GenEvent_1805_()
    data object Refresh : GenEvent_1805_()
    data class Search(val query: String) : GenEvent_1805_()
    data class Filter(val predicate: String) : GenEvent_1805_()
}

sealed class GenState_1805_ {
    data object Idle : GenState_1805_()
    data object Loading : GenState_1805_()
    data class Success(val items: List<GenModel_1805_>) : GenState_1805_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1805_()
    data class Partial(val items: List<GenModel_1805_>, val hasMore: Boolean) : GenState_1805_()
}

interface GenRepository_1805_ {
    suspend fun getAll(): List<GenModel_1805_>
    suspend fun getById(id: Long): GenModel_1805_?
    suspend fun save(model: GenModel_1805_): GenModel_1805_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1805_>
}

@Singleton
class GenRepositoryImpl_1805_ @Inject constructor() : GenRepository_1805_ {
    private val store = mutableMapOf<Long, GenModel_1805_>()
    override suspend fun getAll(): List<GenModel_1805_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1805_? = store[id]
    override suspend fun save(model: GenModel_1805_): GenModel_1805_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1805_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1805_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1805_ @Inject constructor(
    private val repository: GenRepositoryImpl_1805_
) : GenUseCase_1805_<Unit, List<GenModel_1805_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1805_> = repository.getAll()
}

class GenSaveUseCase_1805_ @Inject constructor(
    private val repository: GenRepositoryImpl_1805_
) : GenUseCase_1805_<GenModel_1805_, GenModel_1805_> {
    override suspend fun invoke(params: GenModel_1805_): GenModel_1805_ = repository.save(params)
}

class GenDeleteUseCase_1805_ @Inject constructor(
    private val repository: GenRepositoryImpl_1805_
) : GenUseCase_1805_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1805_ @Inject constructor(
    private val repository: GenRepositoryImpl_1805_
) : GenUseCase_1805_<String, List<GenModel_1805_>> {
    override suspend fun invoke(params: String): List<GenModel_1805_> = repository.search(params)
}

abstract class GenMapper_1805_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1805_ : GenMapper_1805_<GenModel_1805_, String>() {
    override fun map(input: GenModel_1805_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1805_ : GenMapper_1805_<String, GenModel_1805_>() {
    override fun map(input: String): GenModel_1805_ {
        val parts = input.split(":")
        return GenModel_1805_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1805_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1805_,
    private val saveUseCase: GenSaveUseCase_1805_,
    private val deleteUseCase: GenDeleteUseCase_1805_,
    private val searchUseCase: GenSearchUseCase_1805_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1805_>(GenState_1805_.Idle)
    val state: StateFlow<GenState_1805_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1805_) {
        when (event) {
            is GenEvent_1805_.Load -> loadAll()
            is GenEvent_1805_.Update -> save(event.model)
            is GenEvent_1805_.Delete -> delete(event.id)
            is GenEvent_1805_.Refresh -> loadAll()
            is GenEvent_1805_.Search -> search(event.query)
            is GenEvent_1805_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1805_.Loading; _state.value = GenState_1805_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1805_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1805_.Success(searchUseCase(query)) } }
}
