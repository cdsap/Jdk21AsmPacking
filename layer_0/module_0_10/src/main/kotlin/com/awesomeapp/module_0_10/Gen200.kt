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

data class GenModel_200_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_200_ {
    data class Load(val id: Long) : GenEvent_200_()
    data class Update(val model: GenModel_200_) : GenEvent_200_()
    data class Delete(val id: Long) : GenEvent_200_()
    data object Refresh : GenEvent_200_()
    data class Search(val query: String) : GenEvent_200_()
    data class Filter(val predicate: String) : GenEvent_200_()
}

sealed class GenState_200_ {
    data object Idle : GenState_200_()
    data object Loading : GenState_200_()
    data class Success(val items: List<GenModel_200_>) : GenState_200_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_200_()
    data class Partial(val items: List<GenModel_200_>, val hasMore: Boolean) : GenState_200_()
}

interface GenRepository_200_ {
    suspend fun getAll(): List<GenModel_200_>
    suspend fun getById(id: Long): GenModel_200_?
    suspend fun save(model: GenModel_200_): GenModel_200_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_200_>
}

@Singleton
class GenRepositoryImpl_200_ @Inject constructor() : GenRepository_200_ {
    private val store = mutableMapOf<Long, GenModel_200_>()
    override suspend fun getAll(): List<GenModel_200_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_200_? = store[id]
    override suspend fun save(model: GenModel_200_): GenModel_200_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_200_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_200_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_200_ @Inject constructor(
    private val repository: GenRepositoryImpl_200_
) : GenUseCase_200_<Unit, List<GenModel_200_>> {
    override suspend fun invoke(params: Unit): List<GenModel_200_> = repository.getAll()
}

class GenSaveUseCase_200_ @Inject constructor(
    private val repository: GenRepositoryImpl_200_
) : GenUseCase_200_<GenModel_200_, GenModel_200_> {
    override suspend fun invoke(params: GenModel_200_): GenModel_200_ = repository.save(params)
}

class GenDeleteUseCase_200_ @Inject constructor(
    private val repository: GenRepositoryImpl_200_
) : GenUseCase_200_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_200_ @Inject constructor(
    private val repository: GenRepositoryImpl_200_
) : GenUseCase_200_<String, List<GenModel_200_>> {
    override suspend fun invoke(params: String): List<GenModel_200_> = repository.search(params)
}

abstract class GenMapper_200_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_200_ : GenMapper_200_<GenModel_200_, String>() {
    override fun map(input: GenModel_200_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_200_ : GenMapper_200_<String, GenModel_200_>() {
    override fun map(input: String): GenModel_200_ {
        val parts = input.split(":")
        return GenModel_200_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_200_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_200_,
    private val saveUseCase: GenSaveUseCase_200_,
    private val deleteUseCase: GenDeleteUseCase_200_,
    private val searchUseCase: GenSearchUseCase_200_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_200_>(GenState_200_.Idle)
    val state: StateFlow<GenState_200_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_200_) {
        when (event) {
            is GenEvent_200_.Load -> loadAll()
            is GenEvent_200_.Update -> save(event.model)
            is GenEvent_200_.Delete -> delete(event.id)
            is GenEvent_200_.Refresh -> loadAll()
            is GenEvent_200_.Search -> search(event.query)
            is GenEvent_200_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_200_.Loading; _state.value = GenState_200_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_200_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_200_.Success(searchUseCase(query)) } }
}
