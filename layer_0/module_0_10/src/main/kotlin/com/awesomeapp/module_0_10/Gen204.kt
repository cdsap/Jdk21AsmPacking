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

data class GenModel_204_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_204_ {
    data class Load(val id: Long) : GenEvent_204_()
    data class Update(val model: GenModel_204_) : GenEvent_204_()
    data class Delete(val id: Long) : GenEvent_204_()
    data object Refresh : GenEvent_204_()
    data class Search(val query: String) : GenEvent_204_()
    data class Filter(val predicate: String) : GenEvent_204_()
}

sealed class GenState_204_ {
    data object Idle : GenState_204_()
    data object Loading : GenState_204_()
    data class Success(val items: List<GenModel_204_>) : GenState_204_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_204_()
    data class Partial(val items: List<GenModel_204_>, val hasMore: Boolean) : GenState_204_()
}

interface GenRepository_204_ {
    suspend fun getAll(): List<GenModel_204_>
    suspend fun getById(id: Long): GenModel_204_?
    suspend fun save(model: GenModel_204_): GenModel_204_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_204_>
}

@Singleton
class GenRepositoryImpl_204_ @Inject constructor() : GenRepository_204_ {
    private val store = mutableMapOf<Long, GenModel_204_>()
    override suspend fun getAll(): List<GenModel_204_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_204_? = store[id]
    override suspend fun save(model: GenModel_204_): GenModel_204_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_204_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_204_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_204_ @Inject constructor(
    private val repository: GenRepositoryImpl_204_
) : GenUseCase_204_<Unit, List<GenModel_204_>> {
    override suspend fun invoke(params: Unit): List<GenModel_204_> = repository.getAll()
}

class GenSaveUseCase_204_ @Inject constructor(
    private val repository: GenRepositoryImpl_204_
) : GenUseCase_204_<GenModel_204_, GenModel_204_> {
    override suspend fun invoke(params: GenModel_204_): GenModel_204_ = repository.save(params)
}

class GenDeleteUseCase_204_ @Inject constructor(
    private val repository: GenRepositoryImpl_204_
) : GenUseCase_204_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_204_ @Inject constructor(
    private val repository: GenRepositoryImpl_204_
) : GenUseCase_204_<String, List<GenModel_204_>> {
    override suspend fun invoke(params: String): List<GenModel_204_> = repository.search(params)
}

abstract class GenMapper_204_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_204_ : GenMapper_204_<GenModel_204_, String>() {
    override fun map(input: GenModel_204_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_204_ : GenMapper_204_<String, GenModel_204_>() {
    override fun map(input: String): GenModel_204_ {
        val parts = input.split(":")
        return GenModel_204_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_204_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_204_,
    private val saveUseCase: GenSaveUseCase_204_,
    private val deleteUseCase: GenDeleteUseCase_204_,
    private val searchUseCase: GenSearchUseCase_204_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_204_>(GenState_204_.Idle)
    val state: StateFlow<GenState_204_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_204_) {
        when (event) {
            is GenEvent_204_.Load -> loadAll()
            is GenEvent_204_.Update -> save(event.model)
            is GenEvent_204_.Delete -> delete(event.id)
            is GenEvent_204_.Refresh -> loadAll()
            is GenEvent_204_.Search -> search(event.query)
            is GenEvent_204_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_204_.Loading; _state.value = GenState_204_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_204_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_204_.Success(searchUseCase(query)) } }
}
