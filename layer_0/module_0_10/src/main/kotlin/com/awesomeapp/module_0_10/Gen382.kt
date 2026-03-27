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

data class GenModel_382_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_382_ {
    data class Load(val id: Long) : GenEvent_382_()
    data class Update(val model: GenModel_382_) : GenEvent_382_()
    data class Delete(val id: Long) : GenEvent_382_()
    data object Refresh : GenEvent_382_()
    data class Search(val query: String) : GenEvent_382_()
    data class Filter(val predicate: String) : GenEvent_382_()
}

sealed class GenState_382_ {
    data object Idle : GenState_382_()
    data object Loading : GenState_382_()
    data class Success(val items: List<GenModel_382_>) : GenState_382_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_382_()
    data class Partial(val items: List<GenModel_382_>, val hasMore: Boolean) : GenState_382_()
}

interface GenRepository_382_ {
    suspend fun getAll(): List<GenModel_382_>
    suspend fun getById(id: Long): GenModel_382_?
    suspend fun save(model: GenModel_382_): GenModel_382_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_382_>
}

@Singleton
class GenRepositoryImpl_382_ @Inject constructor() : GenRepository_382_ {
    private val store = mutableMapOf<Long, GenModel_382_>()
    override suspend fun getAll(): List<GenModel_382_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_382_? = store[id]
    override suspend fun save(model: GenModel_382_): GenModel_382_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_382_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_382_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_382_ @Inject constructor(
    private val repository: GenRepositoryImpl_382_
) : GenUseCase_382_<Unit, List<GenModel_382_>> {
    override suspend fun invoke(params: Unit): List<GenModel_382_> = repository.getAll()
}

class GenSaveUseCase_382_ @Inject constructor(
    private val repository: GenRepositoryImpl_382_
) : GenUseCase_382_<GenModel_382_, GenModel_382_> {
    override suspend fun invoke(params: GenModel_382_): GenModel_382_ = repository.save(params)
}

class GenDeleteUseCase_382_ @Inject constructor(
    private val repository: GenRepositoryImpl_382_
) : GenUseCase_382_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_382_ @Inject constructor(
    private val repository: GenRepositoryImpl_382_
) : GenUseCase_382_<String, List<GenModel_382_>> {
    override suspend fun invoke(params: String): List<GenModel_382_> = repository.search(params)
}

abstract class GenMapper_382_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_382_ : GenMapper_382_<GenModel_382_, String>() {
    override fun map(input: GenModel_382_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_382_ : GenMapper_382_<String, GenModel_382_>() {
    override fun map(input: String): GenModel_382_ {
        val parts = input.split(":")
        return GenModel_382_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_382_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_382_,
    private val saveUseCase: GenSaveUseCase_382_,
    private val deleteUseCase: GenDeleteUseCase_382_,
    private val searchUseCase: GenSearchUseCase_382_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_382_>(GenState_382_.Idle)
    val state: StateFlow<GenState_382_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_382_) {
        when (event) {
            is GenEvent_382_.Load -> loadAll()
            is GenEvent_382_.Update -> save(event.model)
            is GenEvent_382_.Delete -> delete(event.id)
            is GenEvent_382_.Refresh -> loadAll()
            is GenEvent_382_.Search -> search(event.query)
            is GenEvent_382_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_382_.Loading; _state.value = GenState_382_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_382_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_382_.Success(searchUseCase(query)) } }
}
