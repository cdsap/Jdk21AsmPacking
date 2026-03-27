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

data class GenModel_1827_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1827_ {
    data class Load(val id: Long) : GenEvent_1827_()
    data class Update(val model: GenModel_1827_) : GenEvent_1827_()
    data class Delete(val id: Long) : GenEvent_1827_()
    data object Refresh : GenEvent_1827_()
    data class Search(val query: String) : GenEvent_1827_()
    data class Filter(val predicate: String) : GenEvent_1827_()
}

sealed class GenState_1827_ {
    data object Idle : GenState_1827_()
    data object Loading : GenState_1827_()
    data class Success(val items: List<GenModel_1827_>) : GenState_1827_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1827_()
    data class Partial(val items: List<GenModel_1827_>, val hasMore: Boolean) : GenState_1827_()
}

interface GenRepository_1827_ {
    suspend fun getAll(): List<GenModel_1827_>
    suspend fun getById(id: Long): GenModel_1827_?
    suspend fun save(model: GenModel_1827_): GenModel_1827_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1827_>
}

@Singleton
class GenRepositoryImpl_1827_ @Inject constructor() : GenRepository_1827_ {
    private val store = mutableMapOf<Long, GenModel_1827_>()
    override suspend fun getAll(): List<GenModel_1827_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1827_? = store[id]
    override suspend fun save(model: GenModel_1827_): GenModel_1827_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1827_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1827_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1827_ @Inject constructor(
    private val repository: GenRepositoryImpl_1827_
) : GenUseCase_1827_<Unit, List<GenModel_1827_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1827_> = repository.getAll()
}

class GenSaveUseCase_1827_ @Inject constructor(
    private val repository: GenRepositoryImpl_1827_
) : GenUseCase_1827_<GenModel_1827_, GenModel_1827_> {
    override suspend fun invoke(params: GenModel_1827_): GenModel_1827_ = repository.save(params)
}

class GenDeleteUseCase_1827_ @Inject constructor(
    private val repository: GenRepositoryImpl_1827_
) : GenUseCase_1827_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1827_ @Inject constructor(
    private val repository: GenRepositoryImpl_1827_
) : GenUseCase_1827_<String, List<GenModel_1827_>> {
    override suspend fun invoke(params: String): List<GenModel_1827_> = repository.search(params)
}

abstract class GenMapper_1827_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1827_ : GenMapper_1827_<GenModel_1827_, String>() {
    override fun map(input: GenModel_1827_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1827_ : GenMapper_1827_<String, GenModel_1827_>() {
    override fun map(input: String): GenModel_1827_ {
        val parts = input.split(":")
        return GenModel_1827_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1827_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1827_,
    private val saveUseCase: GenSaveUseCase_1827_,
    private val deleteUseCase: GenDeleteUseCase_1827_,
    private val searchUseCase: GenSearchUseCase_1827_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1827_>(GenState_1827_.Idle)
    val state: StateFlow<GenState_1827_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1827_) {
        when (event) {
            is GenEvent_1827_.Load -> loadAll()
            is GenEvent_1827_.Update -> save(event.model)
            is GenEvent_1827_.Delete -> delete(event.id)
            is GenEvent_1827_.Refresh -> loadAll()
            is GenEvent_1827_.Search -> search(event.query)
            is GenEvent_1827_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1827_.Loading; _state.value = GenState_1827_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1827_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1827_.Success(searchUseCase(query)) } }
}
