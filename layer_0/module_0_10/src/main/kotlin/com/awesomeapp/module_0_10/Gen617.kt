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

data class GenModel_617_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_617_ {
    data class Load(val id: Long) : GenEvent_617_()
    data class Update(val model: GenModel_617_) : GenEvent_617_()
    data class Delete(val id: Long) : GenEvent_617_()
    data object Refresh : GenEvent_617_()
    data class Search(val query: String) : GenEvent_617_()
    data class Filter(val predicate: String) : GenEvent_617_()
}

sealed class GenState_617_ {
    data object Idle : GenState_617_()
    data object Loading : GenState_617_()
    data class Success(val items: List<GenModel_617_>) : GenState_617_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_617_()
    data class Partial(val items: List<GenModel_617_>, val hasMore: Boolean) : GenState_617_()
}

interface GenRepository_617_ {
    suspend fun getAll(): List<GenModel_617_>
    suspend fun getById(id: Long): GenModel_617_?
    suspend fun save(model: GenModel_617_): GenModel_617_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_617_>
}

@Singleton
class GenRepositoryImpl_617_ @Inject constructor() : GenRepository_617_ {
    private val store = mutableMapOf<Long, GenModel_617_>()
    override suspend fun getAll(): List<GenModel_617_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_617_? = store[id]
    override suspend fun save(model: GenModel_617_): GenModel_617_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_617_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_617_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_617_ @Inject constructor(
    private val repository: GenRepositoryImpl_617_
) : GenUseCase_617_<Unit, List<GenModel_617_>> {
    override suspend fun invoke(params: Unit): List<GenModel_617_> = repository.getAll()
}

class GenSaveUseCase_617_ @Inject constructor(
    private val repository: GenRepositoryImpl_617_
) : GenUseCase_617_<GenModel_617_, GenModel_617_> {
    override suspend fun invoke(params: GenModel_617_): GenModel_617_ = repository.save(params)
}

class GenDeleteUseCase_617_ @Inject constructor(
    private val repository: GenRepositoryImpl_617_
) : GenUseCase_617_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_617_ @Inject constructor(
    private val repository: GenRepositoryImpl_617_
) : GenUseCase_617_<String, List<GenModel_617_>> {
    override suspend fun invoke(params: String): List<GenModel_617_> = repository.search(params)
}

abstract class GenMapper_617_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_617_ : GenMapper_617_<GenModel_617_, String>() {
    override fun map(input: GenModel_617_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_617_ : GenMapper_617_<String, GenModel_617_>() {
    override fun map(input: String): GenModel_617_ {
        val parts = input.split(":")
        return GenModel_617_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_617_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_617_,
    private val saveUseCase: GenSaveUseCase_617_,
    private val deleteUseCase: GenDeleteUseCase_617_,
    private val searchUseCase: GenSearchUseCase_617_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_617_>(GenState_617_.Idle)
    val state: StateFlow<GenState_617_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_617_) {
        when (event) {
            is GenEvent_617_.Load -> loadAll()
            is GenEvent_617_.Update -> save(event.model)
            is GenEvent_617_.Delete -> delete(event.id)
            is GenEvent_617_.Refresh -> loadAll()
            is GenEvent_617_.Search -> search(event.query)
            is GenEvent_617_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_617_.Loading; _state.value = GenState_617_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_617_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_617_.Success(searchUseCase(query)) } }
}
