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

data class GenModel_851_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_851_ {
    data class Load(val id: Long) : GenEvent_851_()
    data class Update(val model: GenModel_851_) : GenEvent_851_()
    data class Delete(val id: Long) : GenEvent_851_()
    data object Refresh : GenEvent_851_()
    data class Search(val query: String) : GenEvent_851_()
    data class Filter(val predicate: String) : GenEvent_851_()
}

sealed class GenState_851_ {
    data object Idle : GenState_851_()
    data object Loading : GenState_851_()
    data class Success(val items: List<GenModel_851_>) : GenState_851_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_851_()
    data class Partial(val items: List<GenModel_851_>, val hasMore: Boolean) : GenState_851_()
}

interface GenRepository_851_ {
    suspend fun getAll(): List<GenModel_851_>
    suspend fun getById(id: Long): GenModel_851_?
    suspend fun save(model: GenModel_851_): GenModel_851_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_851_>
}

@Singleton
class GenRepositoryImpl_851_ @Inject constructor() : GenRepository_851_ {
    private val store = mutableMapOf<Long, GenModel_851_>()
    override suspend fun getAll(): List<GenModel_851_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_851_? = store[id]
    override suspend fun save(model: GenModel_851_): GenModel_851_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_851_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_851_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_851_ @Inject constructor(
    private val repository: GenRepositoryImpl_851_
) : GenUseCase_851_<Unit, List<GenModel_851_>> {
    override suspend fun invoke(params: Unit): List<GenModel_851_> = repository.getAll()
}

class GenSaveUseCase_851_ @Inject constructor(
    private val repository: GenRepositoryImpl_851_
) : GenUseCase_851_<GenModel_851_, GenModel_851_> {
    override suspend fun invoke(params: GenModel_851_): GenModel_851_ = repository.save(params)
}

class GenDeleteUseCase_851_ @Inject constructor(
    private val repository: GenRepositoryImpl_851_
) : GenUseCase_851_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_851_ @Inject constructor(
    private val repository: GenRepositoryImpl_851_
) : GenUseCase_851_<String, List<GenModel_851_>> {
    override suspend fun invoke(params: String): List<GenModel_851_> = repository.search(params)
}

abstract class GenMapper_851_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_851_ : GenMapper_851_<GenModel_851_, String>() {
    override fun map(input: GenModel_851_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_851_ : GenMapper_851_<String, GenModel_851_>() {
    override fun map(input: String): GenModel_851_ {
        val parts = input.split(":")
        return GenModel_851_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_851_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_851_,
    private val saveUseCase: GenSaveUseCase_851_,
    private val deleteUseCase: GenDeleteUseCase_851_,
    private val searchUseCase: GenSearchUseCase_851_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_851_>(GenState_851_.Idle)
    val state: StateFlow<GenState_851_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_851_) {
        when (event) {
            is GenEvent_851_.Load -> loadAll()
            is GenEvent_851_.Update -> save(event.model)
            is GenEvent_851_.Delete -> delete(event.id)
            is GenEvent_851_.Refresh -> loadAll()
            is GenEvent_851_.Search -> search(event.query)
            is GenEvent_851_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_851_.Loading; _state.value = GenState_851_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_851_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_851_.Success(searchUseCase(query)) } }
}
