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

data class GenModel_404_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_404_ {
    data class Load(val id: Long) : GenEvent_404_()
    data class Update(val model: GenModel_404_) : GenEvent_404_()
    data class Delete(val id: Long) : GenEvent_404_()
    data object Refresh : GenEvent_404_()
    data class Search(val query: String) : GenEvent_404_()
    data class Filter(val predicate: String) : GenEvent_404_()
}

sealed class GenState_404_ {
    data object Idle : GenState_404_()
    data object Loading : GenState_404_()
    data class Success(val items: List<GenModel_404_>) : GenState_404_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_404_()
    data class Partial(val items: List<GenModel_404_>, val hasMore: Boolean) : GenState_404_()
}

interface GenRepository_404_ {
    suspend fun getAll(): List<GenModel_404_>
    suspend fun getById(id: Long): GenModel_404_?
    suspend fun save(model: GenModel_404_): GenModel_404_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_404_>
}

@Singleton
class GenRepositoryImpl_404_ @Inject constructor() : GenRepository_404_ {
    private val store = mutableMapOf<Long, GenModel_404_>()
    override suspend fun getAll(): List<GenModel_404_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_404_? = store[id]
    override suspend fun save(model: GenModel_404_): GenModel_404_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_404_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_404_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_404_ @Inject constructor(
    private val repository: GenRepositoryImpl_404_
) : GenUseCase_404_<Unit, List<GenModel_404_>> {
    override suspend fun invoke(params: Unit): List<GenModel_404_> = repository.getAll()
}

class GenSaveUseCase_404_ @Inject constructor(
    private val repository: GenRepositoryImpl_404_
) : GenUseCase_404_<GenModel_404_, GenModel_404_> {
    override suspend fun invoke(params: GenModel_404_): GenModel_404_ = repository.save(params)
}

class GenDeleteUseCase_404_ @Inject constructor(
    private val repository: GenRepositoryImpl_404_
) : GenUseCase_404_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_404_ @Inject constructor(
    private val repository: GenRepositoryImpl_404_
) : GenUseCase_404_<String, List<GenModel_404_>> {
    override suspend fun invoke(params: String): List<GenModel_404_> = repository.search(params)
}

abstract class GenMapper_404_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_404_ : GenMapper_404_<GenModel_404_, String>() {
    override fun map(input: GenModel_404_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_404_ : GenMapper_404_<String, GenModel_404_>() {
    override fun map(input: String): GenModel_404_ {
        val parts = input.split(":")
        return GenModel_404_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_404_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_404_,
    private val saveUseCase: GenSaveUseCase_404_,
    private val deleteUseCase: GenDeleteUseCase_404_,
    private val searchUseCase: GenSearchUseCase_404_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_404_>(GenState_404_.Idle)
    val state: StateFlow<GenState_404_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_404_) {
        when (event) {
            is GenEvent_404_.Load -> loadAll()
            is GenEvent_404_.Update -> save(event.model)
            is GenEvent_404_.Delete -> delete(event.id)
            is GenEvent_404_.Refresh -> loadAll()
            is GenEvent_404_.Search -> search(event.query)
            is GenEvent_404_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_404_.Loading; _state.value = GenState_404_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_404_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_404_.Success(searchUseCase(query)) } }
}
