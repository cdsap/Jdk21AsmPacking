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

data class GenModel_809_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_809_ {
    data class Load(val id: Long) : GenEvent_809_()
    data class Update(val model: GenModel_809_) : GenEvent_809_()
    data class Delete(val id: Long) : GenEvent_809_()
    data object Refresh : GenEvent_809_()
    data class Search(val query: String) : GenEvent_809_()
    data class Filter(val predicate: String) : GenEvent_809_()
}

sealed class GenState_809_ {
    data object Idle : GenState_809_()
    data object Loading : GenState_809_()
    data class Success(val items: List<GenModel_809_>) : GenState_809_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_809_()
    data class Partial(val items: List<GenModel_809_>, val hasMore: Boolean) : GenState_809_()
}

interface GenRepository_809_ {
    suspend fun getAll(): List<GenModel_809_>
    suspend fun getById(id: Long): GenModel_809_?
    suspend fun save(model: GenModel_809_): GenModel_809_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_809_>
}

@Singleton
class GenRepositoryImpl_809_ @Inject constructor() : GenRepository_809_ {
    private val store = mutableMapOf<Long, GenModel_809_>()
    override suspend fun getAll(): List<GenModel_809_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_809_? = store[id]
    override suspend fun save(model: GenModel_809_): GenModel_809_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_809_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_809_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_809_ @Inject constructor(
    private val repository: GenRepositoryImpl_809_
) : GenUseCase_809_<Unit, List<GenModel_809_>> {
    override suspend fun invoke(params: Unit): List<GenModel_809_> = repository.getAll()
}

class GenSaveUseCase_809_ @Inject constructor(
    private val repository: GenRepositoryImpl_809_
) : GenUseCase_809_<GenModel_809_, GenModel_809_> {
    override suspend fun invoke(params: GenModel_809_): GenModel_809_ = repository.save(params)
}

class GenDeleteUseCase_809_ @Inject constructor(
    private val repository: GenRepositoryImpl_809_
) : GenUseCase_809_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_809_ @Inject constructor(
    private val repository: GenRepositoryImpl_809_
) : GenUseCase_809_<String, List<GenModel_809_>> {
    override suspend fun invoke(params: String): List<GenModel_809_> = repository.search(params)
}

abstract class GenMapper_809_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_809_ : GenMapper_809_<GenModel_809_, String>() {
    override fun map(input: GenModel_809_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_809_ : GenMapper_809_<String, GenModel_809_>() {
    override fun map(input: String): GenModel_809_ {
        val parts = input.split(":")
        return GenModel_809_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_809_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_809_,
    private val saveUseCase: GenSaveUseCase_809_,
    private val deleteUseCase: GenDeleteUseCase_809_,
    private val searchUseCase: GenSearchUseCase_809_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_809_>(GenState_809_.Idle)
    val state: StateFlow<GenState_809_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_809_) {
        when (event) {
            is GenEvent_809_.Load -> loadAll()
            is GenEvent_809_.Update -> save(event.model)
            is GenEvent_809_.Delete -> delete(event.id)
            is GenEvent_809_.Refresh -> loadAll()
            is GenEvent_809_.Search -> search(event.query)
            is GenEvent_809_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_809_.Loading; _state.value = GenState_809_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_809_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_809_.Success(searchUseCase(query)) } }
}
