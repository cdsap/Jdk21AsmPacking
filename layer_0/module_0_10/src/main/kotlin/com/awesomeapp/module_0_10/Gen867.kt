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

data class GenModel_867_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_867_ {
    data class Load(val id: Long) : GenEvent_867_()
    data class Update(val model: GenModel_867_) : GenEvent_867_()
    data class Delete(val id: Long) : GenEvent_867_()
    data object Refresh : GenEvent_867_()
    data class Search(val query: String) : GenEvent_867_()
    data class Filter(val predicate: String) : GenEvent_867_()
}

sealed class GenState_867_ {
    data object Idle : GenState_867_()
    data object Loading : GenState_867_()
    data class Success(val items: List<GenModel_867_>) : GenState_867_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_867_()
    data class Partial(val items: List<GenModel_867_>, val hasMore: Boolean) : GenState_867_()
}

interface GenRepository_867_ {
    suspend fun getAll(): List<GenModel_867_>
    suspend fun getById(id: Long): GenModel_867_?
    suspend fun save(model: GenModel_867_): GenModel_867_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_867_>
}

@Singleton
class GenRepositoryImpl_867_ @Inject constructor() : GenRepository_867_ {
    private val store = mutableMapOf<Long, GenModel_867_>()
    override suspend fun getAll(): List<GenModel_867_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_867_? = store[id]
    override suspend fun save(model: GenModel_867_): GenModel_867_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_867_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_867_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_867_ @Inject constructor(
    private val repository: GenRepositoryImpl_867_
) : GenUseCase_867_<Unit, List<GenModel_867_>> {
    override suspend fun invoke(params: Unit): List<GenModel_867_> = repository.getAll()
}

class GenSaveUseCase_867_ @Inject constructor(
    private val repository: GenRepositoryImpl_867_
) : GenUseCase_867_<GenModel_867_, GenModel_867_> {
    override suspend fun invoke(params: GenModel_867_): GenModel_867_ = repository.save(params)
}

class GenDeleteUseCase_867_ @Inject constructor(
    private val repository: GenRepositoryImpl_867_
) : GenUseCase_867_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_867_ @Inject constructor(
    private val repository: GenRepositoryImpl_867_
) : GenUseCase_867_<String, List<GenModel_867_>> {
    override suspend fun invoke(params: String): List<GenModel_867_> = repository.search(params)
}

abstract class GenMapper_867_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_867_ : GenMapper_867_<GenModel_867_, String>() {
    override fun map(input: GenModel_867_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_867_ : GenMapper_867_<String, GenModel_867_>() {
    override fun map(input: String): GenModel_867_ {
        val parts = input.split(":")
        return GenModel_867_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_867_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_867_,
    private val saveUseCase: GenSaveUseCase_867_,
    private val deleteUseCase: GenDeleteUseCase_867_,
    private val searchUseCase: GenSearchUseCase_867_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_867_>(GenState_867_.Idle)
    val state: StateFlow<GenState_867_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_867_) {
        when (event) {
            is GenEvent_867_.Load -> loadAll()
            is GenEvent_867_.Update -> save(event.model)
            is GenEvent_867_.Delete -> delete(event.id)
            is GenEvent_867_.Refresh -> loadAll()
            is GenEvent_867_.Search -> search(event.query)
            is GenEvent_867_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_867_.Loading; _state.value = GenState_867_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_867_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_867_.Success(searchUseCase(query)) } }
}
