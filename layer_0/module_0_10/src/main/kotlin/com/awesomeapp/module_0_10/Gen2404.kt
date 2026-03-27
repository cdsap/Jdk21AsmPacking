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

data class GenModel_2404_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2404_ {
    data class Load(val id: Long) : GenEvent_2404_()
    data class Update(val model: GenModel_2404_) : GenEvent_2404_()
    data class Delete(val id: Long) : GenEvent_2404_()
    data object Refresh : GenEvent_2404_()
    data class Search(val query: String) : GenEvent_2404_()
    data class Filter(val predicate: String) : GenEvent_2404_()
}

sealed class GenState_2404_ {
    data object Idle : GenState_2404_()
    data object Loading : GenState_2404_()
    data class Success(val items: List<GenModel_2404_>) : GenState_2404_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2404_()
    data class Partial(val items: List<GenModel_2404_>, val hasMore: Boolean) : GenState_2404_()
}

interface GenRepository_2404_ {
    suspend fun getAll(): List<GenModel_2404_>
    suspend fun getById(id: Long): GenModel_2404_?
    suspend fun save(model: GenModel_2404_): GenModel_2404_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2404_>
}

@Singleton
class GenRepositoryImpl_2404_ @Inject constructor() : GenRepository_2404_ {
    private val store = mutableMapOf<Long, GenModel_2404_>()
    override suspend fun getAll(): List<GenModel_2404_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2404_? = store[id]
    override suspend fun save(model: GenModel_2404_): GenModel_2404_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2404_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2404_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2404_ @Inject constructor(
    private val repository: GenRepositoryImpl_2404_
) : GenUseCase_2404_<Unit, List<GenModel_2404_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2404_> = repository.getAll()
}

class GenSaveUseCase_2404_ @Inject constructor(
    private val repository: GenRepositoryImpl_2404_
) : GenUseCase_2404_<GenModel_2404_, GenModel_2404_> {
    override suspend fun invoke(params: GenModel_2404_): GenModel_2404_ = repository.save(params)
}

class GenDeleteUseCase_2404_ @Inject constructor(
    private val repository: GenRepositoryImpl_2404_
) : GenUseCase_2404_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2404_ @Inject constructor(
    private val repository: GenRepositoryImpl_2404_
) : GenUseCase_2404_<String, List<GenModel_2404_>> {
    override suspend fun invoke(params: String): List<GenModel_2404_> = repository.search(params)
}

abstract class GenMapper_2404_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2404_ : GenMapper_2404_<GenModel_2404_, String>() {
    override fun map(input: GenModel_2404_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2404_ : GenMapper_2404_<String, GenModel_2404_>() {
    override fun map(input: String): GenModel_2404_ {
        val parts = input.split(":")
        return GenModel_2404_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2404_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2404_,
    private val saveUseCase: GenSaveUseCase_2404_,
    private val deleteUseCase: GenDeleteUseCase_2404_,
    private val searchUseCase: GenSearchUseCase_2404_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2404_>(GenState_2404_.Idle)
    val state: StateFlow<GenState_2404_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2404_) {
        when (event) {
            is GenEvent_2404_.Load -> loadAll()
            is GenEvent_2404_.Update -> save(event.model)
            is GenEvent_2404_.Delete -> delete(event.id)
            is GenEvent_2404_.Refresh -> loadAll()
            is GenEvent_2404_.Search -> search(event.query)
            is GenEvent_2404_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2404_.Loading; _state.value = GenState_2404_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2404_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2404_.Success(searchUseCase(query)) } }
}
