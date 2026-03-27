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

data class GenModel_62_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_62_ {
    data class Load(val id: Long) : GenEvent_62_()
    data class Update(val model: GenModel_62_) : GenEvent_62_()
    data class Delete(val id: Long) : GenEvent_62_()
    data object Refresh : GenEvent_62_()
    data class Search(val query: String) : GenEvent_62_()
    data class Filter(val predicate: String) : GenEvent_62_()
}

sealed class GenState_62_ {
    data object Idle : GenState_62_()
    data object Loading : GenState_62_()
    data class Success(val items: List<GenModel_62_>) : GenState_62_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_62_()
    data class Partial(val items: List<GenModel_62_>, val hasMore: Boolean) : GenState_62_()
}

interface GenRepository_62_ {
    suspend fun getAll(): List<GenModel_62_>
    suspend fun getById(id: Long): GenModel_62_?
    suspend fun save(model: GenModel_62_): GenModel_62_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_62_>
}

@Singleton
class GenRepositoryImpl_62_ @Inject constructor() : GenRepository_62_ {
    private val store = mutableMapOf<Long, GenModel_62_>()
    override suspend fun getAll(): List<GenModel_62_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_62_? = store[id]
    override suspend fun save(model: GenModel_62_): GenModel_62_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_62_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_62_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_62_ @Inject constructor(
    private val repository: GenRepositoryImpl_62_
) : GenUseCase_62_<Unit, List<GenModel_62_>> {
    override suspend fun invoke(params: Unit): List<GenModel_62_> = repository.getAll()
}

class GenSaveUseCase_62_ @Inject constructor(
    private val repository: GenRepositoryImpl_62_
) : GenUseCase_62_<GenModel_62_, GenModel_62_> {
    override suspend fun invoke(params: GenModel_62_): GenModel_62_ = repository.save(params)
}

class GenDeleteUseCase_62_ @Inject constructor(
    private val repository: GenRepositoryImpl_62_
) : GenUseCase_62_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_62_ @Inject constructor(
    private val repository: GenRepositoryImpl_62_
) : GenUseCase_62_<String, List<GenModel_62_>> {
    override suspend fun invoke(params: String): List<GenModel_62_> = repository.search(params)
}

abstract class GenMapper_62_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_62_ : GenMapper_62_<GenModel_62_, String>() {
    override fun map(input: GenModel_62_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_62_ : GenMapper_62_<String, GenModel_62_>() {
    override fun map(input: String): GenModel_62_ {
        val parts = input.split(":")
        return GenModel_62_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_62_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_62_,
    private val saveUseCase: GenSaveUseCase_62_,
    private val deleteUseCase: GenDeleteUseCase_62_,
    private val searchUseCase: GenSearchUseCase_62_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_62_>(GenState_62_.Idle)
    val state: StateFlow<GenState_62_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_62_) {
        when (event) {
            is GenEvent_62_.Load -> loadAll()
            is GenEvent_62_.Update -> save(event.model)
            is GenEvent_62_.Delete -> delete(event.id)
            is GenEvent_62_.Refresh -> loadAll()
            is GenEvent_62_.Search -> search(event.query)
            is GenEvent_62_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_62_.Loading; _state.value = GenState_62_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_62_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_62_.Success(searchUseCase(query)) } }
}
