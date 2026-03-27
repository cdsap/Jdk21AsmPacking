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

data class GenModel_202_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_202_ {
    data class Load(val id: Long) : GenEvent_202_()
    data class Update(val model: GenModel_202_) : GenEvent_202_()
    data class Delete(val id: Long) : GenEvent_202_()
    data object Refresh : GenEvent_202_()
    data class Search(val query: String) : GenEvent_202_()
    data class Filter(val predicate: String) : GenEvent_202_()
}

sealed class GenState_202_ {
    data object Idle : GenState_202_()
    data object Loading : GenState_202_()
    data class Success(val items: List<GenModel_202_>) : GenState_202_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_202_()
    data class Partial(val items: List<GenModel_202_>, val hasMore: Boolean) : GenState_202_()
}

interface GenRepository_202_ {
    suspend fun getAll(): List<GenModel_202_>
    suspend fun getById(id: Long): GenModel_202_?
    suspend fun save(model: GenModel_202_): GenModel_202_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_202_>
}

@Singleton
class GenRepositoryImpl_202_ @Inject constructor() : GenRepository_202_ {
    private val store = mutableMapOf<Long, GenModel_202_>()
    override suspend fun getAll(): List<GenModel_202_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_202_? = store[id]
    override suspend fun save(model: GenModel_202_): GenModel_202_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_202_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_202_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_202_ @Inject constructor(
    private val repository: GenRepositoryImpl_202_
) : GenUseCase_202_<Unit, List<GenModel_202_>> {
    override suspend fun invoke(params: Unit): List<GenModel_202_> = repository.getAll()
}

class GenSaveUseCase_202_ @Inject constructor(
    private val repository: GenRepositoryImpl_202_
) : GenUseCase_202_<GenModel_202_, GenModel_202_> {
    override suspend fun invoke(params: GenModel_202_): GenModel_202_ = repository.save(params)
}

class GenDeleteUseCase_202_ @Inject constructor(
    private val repository: GenRepositoryImpl_202_
) : GenUseCase_202_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_202_ @Inject constructor(
    private val repository: GenRepositoryImpl_202_
) : GenUseCase_202_<String, List<GenModel_202_>> {
    override suspend fun invoke(params: String): List<GenModel_202_> = repository.search(params)
}

abstract class GenMapper_202_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_202_ : GenMapper_202_<GenModel_202_, String>() {
    override fun map(input: GenModel_202_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_202_ : GenMapper_202_<String, GenModel_202_>() {
    override fun map(input: String): GenModel_202_ {
        val parts = input.split(":")
        return GenModel_202_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_202_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_202_,
    private val saveUseCase: GenSaveUseCase_202_,
    private val deleteUseCase: GenDeleteUseCase_202_,
    private val searchUseCase: GenSearchUseCase_202_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_202_>(GenState_202_.Idle)
    val state: StateFlow<GenState_202_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_202_) {
        when (event) {
            is GenEvent_202_.Load -> loadAll()
            is GenEvent_202_.Update -> save(event.model)
            is GenEvent_202_.Delete -> delete(event.id)
            is GenEvent_202_.Refresh -> loadAll()
            is GenEvent_202_.Search -> search(event.query)
            is GenEvent_202_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_202_.Loading; _state.value = GenState_202_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_202_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_202_.Success(searchUseCase(query)) } }
}
