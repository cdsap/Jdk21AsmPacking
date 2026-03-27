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

data class GenModel_3103_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3103_ {
    data class Load(val id: Long) : GenEvent_3103_()
    data class Update(val model: GenModel_3103_) : GenEvent_3103_()
    data class Delete(val id: Long) : GenEvent_3103_()
    data object Refresh : GenEvent_3103_()
    data class Search(val query: String) : GenEvent_3103_()
    data class Filter(val predicate: String) : GenEvent_3103_()
}

sealed class GenState_3103_ {
    data object Idle : GenState_3103_()
    data object Loading : GenState_3103_()
    data class Success(val items: List<GenModel_3103_>) : GenState_3103_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3103_()
    data class Partial(val items: List<GenModel_3103_>, val hasMore: Boolean) : GenState_3103_()
}

interface GenRepository_3103_ {
    suspend fun getAll(): List<GenModel_3103_>
    suspend fun getById(id: Long): GenModel_3103_?
    suspend fun save(model: GenModel_3103_): GenModel_3103_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3103_>
}

@Singleton
class GenRepositoryImpl_3103_ @Inject constructor() : GenRepository_3103_ {
    private val store = mutableMapOf<Long, GenModel_3103_>()
    override suspend fun getAll(): List<GenModel_3103_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3103_? = store[id]
    override suspend fun save(model: GenModel_3103_): GenModel_3103_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3103_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3103_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3103_ @Inject constructor(
    private val repository: GenRepositoryImpl_3103_
) : GenUseCase_3103_<Unit, List<GenModel_3103_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3103_> = repository.getAll()
}

class GenSaveUseCase_3103_ @Inject constructor(
    private val repository: GenRepositoryImpl_3103_
) : GenUseCase_3103_<GenModel_3103_, GenModel_3103_> {
    override suspend fun invoke(params: GenModel_3103_): GenModel_3103_ = repository.save(params)
}

class GenDeleteUseCase_3103_ @Inject constructor(
    private val repository: GenRepositoryImpl_3103_
) : GenUseCase_3103_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3103_ @Inject constructor(
    private val repository: GenRepositoryImpl_3103_
) : GenUseCase_3103_<String, List<GenModel_3103_>> {
    override suspend fun invoke(params: String): List<GenModel_3103_> = repository.search(params)
}

abstract class GenMapper_3103_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3103_ : GenMapper_3103_<GenModel_3103_, String>() {
    override fun map(input: GenModel_3103_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3103_ : GenMapper_3103_<String, GenModel_3103_>() {
    override fun map(input: String): GenModel_3103_ {
        val parts = input.split(":")
        return GenModel_3103_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3103_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3103_,
    private val saveUseCase: GenSaveUseCase_3103_,
    private val deleteUseCase: GenDeleteUseCase_3103_,
    private val searchUseCase: GenSearchUseCase_3103_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3103_>(GenState_3103_.Idle)
    val state: StateFlow<GenState_3103_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3103_) {
        when (event) {
            is GenEvent_3103_.Load -> loadAll()
            is GenEvent_3103_.Update -> save(event.model)
            is GenEvent_3103_.Delete -> delete(event.id)
            is GenEvent_3103_.Refresh -> loadAll()
            is GenEvent_3103_.Search -> search(event.query)
            is GenEvent_3103_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3103_.Loading; _state.value = GenState_3103_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3103_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3103_.Success(searchUseCase(query)) } }
}
