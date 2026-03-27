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

data class GenModel_512_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_512_ {
    data class Load(val id: Long) : GenEvent_512_()
    data class Update(val model: GenModel_512_) : GenEvent_512_()
    data class Delete(val id: Long) : GenEvent_512_()
    data object Refresh : GenEvent_512_()
    data class Search(val query: String) : GenEvent_512_()
    data class Filter(val predicate: String) : GenEvent_512_()
}

sealed class GenState_512_ {
    data object Idle : GenState_512_()
    data object Loading : GenState_512_()
    data class Success(val items: List<GenModel_512_>) : GenState_512_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_512_()
    data class Partial(val items: List<GenModel_512_>, val hasMore: Boolean) : GenState_512_()
}

interface GenRepository_512_ {
    suspend fun getAll(): List<GenModel_512_>
    suspend fun getById(id: Long): GenModel_512_?
    suspend fun save(model: GenModel_512_): GenModel_512_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_512_>
}

@Singleton
class GenRepositoryImpl_512_ @Inject constructor() : GenRepository_512_ {
    private val store = mutableMapOf<Long, GenModel_512_>()
    override suspend fun getAll(): List<GenModel_512_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_512_? = store[id]
    override suspend fun save(model: GenModel_512_): GenModel_512_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_512_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_512_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_512_ @Inject constructor(
    private val repository: GenRepositoryImpl_512_
) : GenUseCase_512_<Unit, List<GenModel_512_>> {
    override suspend fun invoke(params: Unit): List<GenModel_512_> = repository.getAll()
}

class GenSaveUseCase_512_ @Inject constructor(
    private val repository: GenRepositoryImpl_512_
) : GenUseCase_512_<GenModel_512_, GenModel_512_> {
    override suspend fun invoke(params: GenModel_512_): GenModel_512_ = repository.save(params)
}

class GenDeleteUseCase_512_ @Inject constructor(
    private val repository: GenRepositoryImpl_512_
) : GenUseCase_512_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_512_ @Inject constructor(
    private val repository: GenRepositoryImpl_512_
) : GenUseCase_512_<String, List<GenModel_512_>> {
    override suspend fun invoke(params: String): List<GenModel_512_> = repository.search(params)
}

abstract class GenMapper_512_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_512_ : GenMapper_512_<GenModel_512_, String>() {
    override fun map(input: GenModel_512_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_512_ : GenMapper_512_<String, GenModel_512_>() {
    override fun map(input: String): GenModel_512_ {
        val parts = input.split(":")
        return GenModel_512_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_512_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_512_,
    private val saveUseCase: GenSaveUseCase_512_,
    private val deleteUseCase: GenDeleteUseCase_512_,
    private val searchUseCase: GenSearchUseCase_512_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_512_>(GenState_512_.Idle)
    val state: StateFlow<GenState_512_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_512_) {
        when (event) {
            is GenEvent_512_.Load -> loadAll()
            is GenEvent_512_.Update -> save(event.model)
            is GenEvent_512_.Delete -> delete(event.id)
            is GenEvent_512_.Refresh -> loadAll()
            is GenEvent_512_.Search -> search(event.query)
            is GenEvent_512_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_512_.Loading; _state.value = GenState_512_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_512_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_512_.Success(searchUseCase(query)) } }
}
