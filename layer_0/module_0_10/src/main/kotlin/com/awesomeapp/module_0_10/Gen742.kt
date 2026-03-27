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

data class GenModel_742_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_742_ {
    data class Load(val id: Long) : GenEvent_742_()
    data class Update(val model: GenModel_742_) : GenEvent_742_()
    data class Delete(val id: Long) : GenEvent_742_()
    data object Refresh : GenEvent_742_()
    data class Search(val query: String) : GenEvent_742_()
    data class Filter(val predicate: String) : GenEvent_742_()
}

sealed class GenState_742_ {
    data object Idle : GenState_742_()
    data object Loading : GenState_742_()
    data class Success(val items: List<GenModel_742_>) : GenState_742_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_742_()
    data class Partial(val items: List<GenModel_742_>, val hasMore: Boolean) : GenState_742_()
}

interface GenRepository_742_ {
    suspend fun getAll(): List<GenModel_742_>
    suspend fun getById(id: Long): GenModel_742_?
    suspend fun save(model: GenModel_742_): GenModel_742_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_742_>
}

@Singleton
class GenRepositoryImpl_742_ @Inject constructor() : GenRepository_742_ {
    private val store = mutableMapOf<Long, GenModel_742_>()
    override suspend fun getAll(): List<GenModel_742_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_742_? = store[id]
    override suspend fun save(model: GenModel_742_): GenModel_742_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_742_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_742_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_742_ @Inject constructor(
    private val repository: GenRepositoryImpl_742_
) : GenUseCase_742_<Unit, List<GenModel_742_>> {
    override suspend fun invoke(params: Unit): List<GenModel_742_> = repository.getAll()
}

class GenSaveUseCase_742_ @Inject constructor(
    private val repository: GenRepositoryImpl_742_
) : GenUseCase_742_<GenModel_742_, GenModel_742_> {
    override suspend fun invoke(params: GenModel_742_): GenModel_742_ = repository.save(params)
}

class GenDeleteUseCase_742_ @Inject constructor(
    private val repository: GenRepositoryImpl_742_
) : GenUseCase_742_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_742_ @Inject constructor(
    private val repository: GenRepositoryImpl_742_
) : GenUseCase_742_<String, List<GenModel_742_>> {
    override suspend fun invoke(params: String): List<GenModel_742_> = repository.search(params)
}

abstract class GenMapper_742_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_742_ : GenMapper_742_<GenModel_742_, String>() {
    override fun map(input: GenModel_742_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_742_ : GenMapper_742_<String, GenModel_742_>() {
    override fun map(input: String): GenModel_742_ {
        val parts = input.split(":")
        return GenModel_742_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_742_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_742_,
    private val saveUseCase: GenSaveUseCase_742_,
    private val deleteUseCase: GenDeleteUseCase_742_,
    private val searchUseCase: GenSearchUseCase_742_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_742_>(GenState_742_.Idle)
    val state: StateFlow<GenState_742_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_742_) {
        when (event) {
            is GenEvent_742_.Load -> loadAll()
            is GenEvent_742_.Update -> save(event.model)
            is GenEvent_742_.Delete -> delete(event.id)
            is GenEvent_742_.Refresh -> loadAll()
            is GenEvent_742_.Search -> search(event.query)
            is GenEvent_742_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_742_.Loading; _state.value = GenState_742_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_742_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_742_.Success(searchUseCase(query)) } }
}
