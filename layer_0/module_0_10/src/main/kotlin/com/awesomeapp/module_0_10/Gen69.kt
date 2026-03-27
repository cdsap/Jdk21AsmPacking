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

data class GenModel_69_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_69_ {
    data class Load(val id: Long) : GenEvent_69_()
    data class Update(val model: GenModel_69_) : GenEvent_69_()
    data class Delete(val id: Long) : GenEvent_69_()
    data object Refresh : GenEvent_69_()
    data class Search(val query: String) : GenEvent_69_()
    data class Filter(val predicate: String) : GenEvent_69_()
}

sealed class GenState_69_ {
    data object Idle : GenState_69_()
    data object Loading : GenState_69_()
    data class Success(val items: List<GenModel_69_>) : GenState_69_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_69_()
    data class Partial(val items: List<GenModel_69_>, val hasMore: Boolean) : GenState_69_()
}

interface GenRepository_69_ {
    suspend fun getAll(): List<GenModel_69_>
    suspend fun getById(id: Long): GenModel_69_?
    suspend fun save(model: GenModel_69_): GenModel_69_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_69_>
}

@Singleton
class GenRepositoryImpl_69_ @Inject constructor() : GenRepository_69_ {
    private val store = mutableMapOf<Long, GenModel_69_>()
    override suspend fun getAll(): List<GenModel_69_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_69_? = store[id]
    override suspend fun save(model: GenModel_69_): GenModel_69_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_69_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_69_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_69_ @Inject constructor(
    private val repository: GenRepositoryImpl_69_
) : GenUseCase_69_<Unit, List<GenModel_69_>> {
    override suspend fun invoke(params: Unit): List<GenModel_69_> = repository.getAll()
}

class GenSaveUseCase_69_ @Inject constructor(
    private val repository: GenRepositoryImpl_69_
) : GenUseCase_69_<GenModel_69_, GenModel_69_> {
    override suspend fun invoke(params: GenModel_69_): GenModel_69_ = repository.save(params)
}

class GenDeleteUseCase_69_ @Inject constructor(
    private val repository: GenRepositoryImpl_69_
) : GenUseCase_69_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_69_ @Inject constructor(
    private val repository: GenRepositoryImpl_69_
) : GenUseCase_69_<String, List<GenModel_69_>> {
    override suspend fun invoke(params: String): List<GenModel_69_> = repository.search(params)
}

abstract class GenMapper_69_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_69_ : GenMapper_69_<GenModel_69_, String>() {
    override fun map(input: GenModel_69_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_69_ : GenMapper_69_<String, GenModel_69_>() {
    override fun map(input: String): GenModel_69_ {
        val parts = input.split(":")
        return GenModel_69_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_69_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_69_,
    private val saveUseCase: GenSaveUseCase_69_,
    private val deleteUseCase: GenDeleteUseCase_69_,
    private val searchUseCase: GenSearchUseCase_69_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_69_>(GenState_69_.Idle)
    val state: StateFlow<GenState_69_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_69_) {
        when (event) {
            is GenEvent_69_.Load -> loadAll()
            is GenEvent_69_.Update -> save(event.model)
            is GenEvent_69_.Delete -> delete(event.id)
            is GenEvent_69_.Refresh -> loadAll()
            is GenEvent_69_.Search -> search(event.query)
            is GenEvent_69_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_69_.Loading; _state.value = GenState_69_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_69_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_69_.Success(searchUseCase(query)) } }
}
