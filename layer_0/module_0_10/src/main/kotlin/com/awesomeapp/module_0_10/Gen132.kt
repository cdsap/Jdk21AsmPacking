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

data class GenModel_132_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_132_ {
    data class Load(val id: Long) : GenEvent_132_()
    data class Update(val model: GenModel_132_) : GenEvent_132_()
    data class Delete(val id: Long) : GenEvent_132_()
    data object Refresh : GenEvent_132_()
    data class Search(val query: String) : GenEvent_132_()
    data class Filter(val predicate: String) : GenEvent_132_()
}

sealed class GenState_132_ {
    data object Idle : GenState_132_()
    data object Loading : GenState_132_()
    data class Success(val items: List<GenModel_132_>) : GenState_132_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_132_()
    data class Partial(val items: List<GenModel_132_>, val hasMore: Boolean) : GenState_132_()
}

interface GenRepository_132_ {
    suspend fun getAll(): List<GenModel_132_>
    suspend fun getById(id: Long): GenModel_132_?
    suspend fun save(model: GenModel_132_): GenModel_132_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_132_>
}

@Singleton
class GenRepositoryImpl_132_ @Inject constructor() : GenRepository_132_ {
    private val store = mutableMapOf<Long, GenModel_132_>()
    override suspend fun getAll(): List<GenModel_132_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_132_? = store[id]
    override suspend fun save(model: GenModel_132_): GenModel_132_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_132_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_132_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_132_ @Inject constructor(
    private val repository: GenRepositoryImpl_132_
) : GenUseCase_132_<Unit, List<GenModel_132_>> {
    override suspend fun invoke(params: Unit): List<GenModel_132_> = repository.getAll()
}

class GenSaveUseCase_132_ @Inject constructor(
    private val repository: GenRepositoryImpl_132_
) : GenUseCase_132_<GenModel_132_, GenModel_132_> {
    override suspend fun invoke(params: GenModel_132_): GenModel_132_ = repository.save(params)
}

class GenDeleteUseCase_132_ @Inject constructor(
    private val repository: GenRepositoryImpl_132_
) : GenUseCase_132_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_132_ @Inject constructor(
    private val repository: GenRepositoryImpl_132_
) : GenUseCase_132_<String, List<GenModel_132_>> {
    override suspend fun invoke(params: String): List<GenModel_132_> = repository.search(params)
}

abstract class GenMapper_132_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_132_ : GenMapper_132_<GenModel_132_, String>() {
    override fun map(input: GenModel_132_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_132_ : GenMapper_132_<String, GenModel_132_>() {
    override fun map(input: String): GenModel_132_ {
        val parts = input.split(":")
        return GenModel_132_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_132_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_132_,
    private val saveUseCase: GenSaveUseCase_132_,
    private val deleteUseCase: GenDeleteUseCase_132_,
    private val searchUseCase: GenSearchUseCase_132_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_132_>(GenState_132_.Idle)
    val state: StateFlow<GenState_132_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_132_) {
        when (event) {
            is GenEvent_132_.Load -> loadAll()
            is GenEvent_132_.Update -> save(event.model)
            is GenEvent_132_.Delete -> delete(event.id)
            is GenEvent_132_.Refresh -> loadAll()
            is GenEvent_132_.Search -> search(event.query)
            is GenEvent_132_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_132_.Loading; _state.value = GenState_132_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_132_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_132_.Success(searchUseCase(query)) } }
}
