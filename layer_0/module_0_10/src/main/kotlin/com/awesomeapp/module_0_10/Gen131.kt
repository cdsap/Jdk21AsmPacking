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

data class GenModel_131_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_131_ {
    data class Load(val id: Long) : GenEvent_131_()
    data class Update(val model: GenModel_131_) : GenEvent_131_()
    data class Delete(val id: Long) : GenEvent_131_()
    data object Refresh : GenEvent_131_()
    data class Search(val query: String) : GenEvent_131_()
    data class Filter(val predicate: String) : GenEvent_131_()
}

sealed class GenState_131_ {
    data object Idle : GenState_131_()
    data object Loading : GenState_131_()
    data class Success(val items: List<GenModel_131_>) : GenState_131_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_131_()
    data class Partial(val items: List<GenModel_131_>, val hasMore: Boolean) : GenState_131_()
}

interface GenRepository_131_ {
    suspend fun getAll(): List<GenModel_131_>
    suspend fun getById(id: Long): GenModel_131_?
    suspend fun save(model: GenModel_131_): GenModel_131_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_131_>
}

@Singleton
class GenRepositoryImpl_131_ @Inject constructor() : GenRepository_131_ {
    private val store = mutableMapOf<Long, GenModel_131_>()
    override suspend fun getAll(): List<GenModel_131_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_131_? = store[id]
    override suspend fun save(model: GenModel_131_): GenModel_131_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_131_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_131_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_131_ @Inject constructor(
    private val repository: GenRepositoryImpl_131_
) : GenUseCase_131_<Unit, List<GenModel_131_>> {
    override suspend fun invoke(params: Unit): List<GenModel_131_> = repository.getAll()
}

class GenSaveUseCase_131_ @Inject constructor(
    private val repository: GenRepositoryImpl_131_
) : GenUseCase_131_<GenModel_131_, GenModel_131_> {
    override suspend fun invoke(params: GenModel_131_): GenModel_131_ = repository.save(params)
}

class GenDeleteUseCase_131_ @Inject constructor(
    private val repository: GenRepositoryImpl_131_
) : GenUseCase_131_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_131_ @Inject constructor(
    private val repository: GenRepositoryImpl_131_
) : GenUseCase_131_<String, List<GenModel_131_>> {
    override suspend fun invoke(params: String): List<GenModel_131_> = repository.search(params)
}

abstract class GenMapper_131_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_131_ : GenMapper_131_<GenModel_131_, String>() {
    override fun map(input: GenModel_131_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_131_ : GenMapper_131_<String, GenModel_131_>() {
    override fun map(input: String): GenModel_131_ {
        val parts = input.split(":")
        return GenModel_131_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_131_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_131_,
    private val saveUseCase: GenSaveUseCase_131_,
    private val deleteUseCase: GenDeleteUseCase_131_,
    private val searchUseCase: GenSearchUseCase_131_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_131_>(GenState_131_.Idle)
    val state: StateFlow<GenState_131_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_131_) {
        when (event) {
            is GenEvent_131_.Load -> loadAll()
            is GenEvent_131_.Update -> save(event.model)
            is GenEvent_131_.Delete -> delete(event.id)
            is GenEvent_131_.Refresh -> loadAll()
            is GenEvent_131_.Search -> search(event.query)
            is GenEvent_131_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_131_.Loading; _state.value = GenState_131_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_131_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_131_.Success(searchUseCase(query)) } }
}
