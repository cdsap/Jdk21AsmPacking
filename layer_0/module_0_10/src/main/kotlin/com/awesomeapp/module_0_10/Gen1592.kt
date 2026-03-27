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

data class GenModel_1592_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1592_ {
    data class Load(val id: Long) : GenEvent_1592_()
    data class Update(val model: GenModel_1592_) : GenEvent_1592_()
    data class Delete(val id: Long) : GenEvent_1592_()
    data object Refresh : GenEvent_1592_()
    data class Search(val query: String) : GenEvent_1592_()
    data class Filter(val predicate: String) : GenEvent_1592_()
}

sealed class GenState_1592_ {
    data object Idle : GenState_1592_()
    data object Loading : GenState_1592_()
    data class Success(val items: List<GenModel_1592_>) : GenState_1592_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1592_()
    data class Partial(val items: List<GenModel_1592_>, val hasMore: Boolean) : GenState_1592_()
}

interface GenRepository_1592_ {
    suspend fun getAll(): List<GenModel_1592_>
    suspend fun getById(id: Long): GenModel_1592_?
    suspend fun save(model: GenModel_1592_): GenModel_1592_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1592_>
}

@Singleton
class GenRepositoryImpl_1592_ @Inject constructor() : GenRepository_1592_ {
    private val store = mutableMapOf<Long, GenModel_1592_>()
    override suspend fun getAll(): List<GenModel_1592_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1592_? = store[id]
    override suspend fun save(model: GenModel_1592_): GenModel_1592_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1592_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1592_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1592_ @Inject constructor(
    private val repository: GenRepositoryImpl_1592_
) : GenUseCase_1592_<Unit, List<GenModel_1592_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1592_> = repository.getAll()
}

class GenSaveUseCase_1592_ @Inject constructor(
    private val repository: GenRepositoryImpl_1592_
) : GenUseCase_1592_<GenModel_1592_, GenModel_1592_> {
    override suspend fun invoke(params: GenModel_1592_): GenModel_1592_ = repository.save(params)
}

class GenDeleteUseCase_1592_ @Inject constructor(
    private val repository: GenRepositoryImpl_1592_
) : GenUseCase_1592_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1592_ @Inject constructor(
    private val repository: GenRepositoryImpl_1592_
) : GenUseCase_1592_<String, List<GenModel_1592_>> {
    override suspend fun invoke(params: String): List<GenModel_1592_> = repository.search(params)
}

abstract class GenMapper_1592_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1592_ : GenMapper_1592_<GenModel_1592_, String>() {
    override fun map(input: GenModel_1592_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1592_ : GenMapper_1592_<String, GenModel_1592_>() {
    override fun map(input: String): GenModel_1592_ {
        val parts = input.split(":")
        return GenModel_1592_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1592_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1592_,
    private val saveUseCase: GenSaveUseCase_1592_,
    private val deleteUseCase: GenDeleteUseCase_1592_,
    private val searchUseCase: GenSearchUseCase_1592_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1592_>(GenState_1592_.Idle)
    val state: StateFlow<GenState_1592_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1592_) {
        when (event) {
            is GenEvent_1592_.Load -> loadAll()
            is GenEvent_1592_.Update -> save(event.model)
            is GenEvent_1592_.Delete -> delete(event.id)
            is GenEvent_1592_.Refresh -> loadAll()
            is GenEvent_1592_.Search -> search(event.query)
            is GenEvent_1592_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1592_.Loading; _state.value = GenState_1592_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1592_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1592_.Success(searchUseCase(query)) } }
}
