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

data class GenModel_1887_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1887_ {
    data class Load(val id: Long) : GenEvent_1887_()
    data class Update(val model: GenModel_1887_) : GenEvent_1887_()
    data class Delete(val id: Long) : GenEvent_1887_()
    data object Refresh : GenEvent_1887_()
    data class Search(val query: String) : GenEvent_1887_()
    data class Filter(val predicate: String) : GenEvent_1887_()
}

sealed class GenState_1887_ {
    data object Idle : GenState_1887_()
    data object Loading : GenState_1887_()
    data class Success(val items: List<GenModel_1887_>) : GenState_1887_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1887_()
    data class Partial(val items: List<GenModel_1887_>, val hasMore: Boolean) : GenState_1887_()
}

interface GenRepository_1887_ {
    suspend fun getAll(): List<GenModel_1887_>
    suspend fun getById(id: Long): GenModel_1887_?
    suspend fun save(model: GenModel_1887_): GenModel_1887_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1887_>
}

@Singleton
class GenRepositoryImpl_1887_ @Inject constructor() : GenRepository_1887_ {
    private val store = mutableMapOf<Long, GenModel_1887_>()
    override suspend fun getAll(): List<GenModel_1887_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1887_? = store[id]
    override suspend fun save(model: GenModel_1887_): GenModel_1887_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1887_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1887_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1887_ @Inject constructor(
    private val repository: GenRepositoryImpl_1887_
) : GenUseCase_1887_<Unit, List<GenModel_1887_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1887_> = repository.getAll()
}

class GenSaveUseCase_1887_ @Inject constructor(
    private val repository: GenRepositoryImpl_1887_
) : GenUseCase_1887_<GenModel_1887_, GenModel_1887_> {
    override suspend fun invoke(params: GenModel_1887_): GenModel_1887_ = repository.save(params)
}

class GenDeleteUseCase_1887_ @Inject constructor(
    private val repository: GenRepositoryImpl_1887_
) : GenUseCase_1887_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1887_ @Inject constructor(
    private val repository: GenRepositoryImpl_1887_
) : GenUseCase_1887_<String, List<GenModel_1887_>> {
    override suspend fun invoke(params: String): List<GenModel_1887_> = repository.search(params)
}

abstract class GenMapper_1887_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1887_ : GenMapper_1887_<GenModel_1887_, String>() {
    override fun map(input: GenModel_1887_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1887_ : GenMapper_1887_<String, GenModel_1887_>() {
    override fun map(input: String): GenModel_1887_ {
        val parts = input.split(":")
        return GenModel_1887_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1887_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1887_,
    private val saveUseCase: GenSaveUseCase_1887_,
    private val deleteUseCase: GenDeleteUseCase_1887_,
    private val searchUseCase: GenSearchUseCase_1887_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1887_>(GenState_1887_.Idle)
    val state: StateFlow<GenState_1887_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1887_) {
        when (event) {
            is GenEvent_1887_.Load -> loadAll()
            is GenEvent_1887_.Update -> save(event.model)
            is GenEvent_1887_.Delete -> delete(event.id)
            is GenEvent_1887_.Refresh -> loadAll()
            is GenEvent_1887_.Search -> search(event.query)
            is GenEvent_1887_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1887_.Loading; _state.value = GenState_1887_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1887_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1887_.Success(searchUseCase(query)) } }
}
