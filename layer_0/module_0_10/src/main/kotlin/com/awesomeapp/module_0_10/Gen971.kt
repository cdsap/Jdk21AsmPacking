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

data class GenModel_971_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_971_ {
    data class Load(val id: Long) : GenEvent_971_()
    data class Update(val model: GenModel_971_) : GenEvent_971_()
    data class Delete(val id: Long) : GenEvent_971_()
    data object Refresh : GenEvent_971_()
    data class Search(val query: String) : GenEvent_971_()
    data class Filter(val predicate: String) : GenEvent_971_()
}

sealed class GenState_971_ {
    data object Idle : GenState_971_()
    data object Loading : GenState_971_()
    data class Success(val items: List<GenModel_971_>) : GenState_971_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_971_()
    data class Partial(val items: List<GenModel_971_>, val hasMore: Boolean) : GenState_971_()
}

interface GenRepository_971_ {
    suspend fun getAll(): List<GenModel_971_>
    suspend fun getById(id: Long): GenModel_971_?
    suspend fun save(model: GenModel_971_): GenModel_971_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_971_>
}

@Singleton
class GenRepositoryImpl_971_ @Inject constructor() : GenRepository_971_ {
    private val store = mutableMapOf<Long, GenModel_971_>()
    override suspend fun getAll(): List<GenModel_971_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_971_? = store[id]
    override suspend fun save(model: GenModel_971_): GenModel_971_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_971_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_971_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_971_ @Inject constructor(
    private val repository: GenRepositoryImpl_971_
) : GenUseCase_971_<Unit, List<GenModel_971_>> {
    override suspend fun invoke(params: Unit): List<GenModel_971_> = repository.getAll()
}

class GenSaveUseCase_971_ @Inject constructor(
    private val repository: GenRepositoryImpl_971_
) : GenUseCase_971_<GenModel_971_, GenModel_971_> {
    override suspend fun invoke(params: GenModel_971_): GenModel_971_ = repository.save(params)
}

class GenDeleteUseCase_971_ @Inject constructor(
    private val repository: GenRepositoryImpl_971_
) : GenUseCase_971_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_971_ @Inject constructor(
    private val repository: GenRepositoryImpl_971_
) : GenUseCase_971_<String, List<GenModel_971_>> {
    override suspend fun invoke(params: String): List<GenModel_971_> = repository.search(params)
}

abstract class GenMapper_971_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_971_ : GenMapper_971_<GenModel_971_, String>() {
    override fun map(input: GenModel_971_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_971_ : GenMapper_971_<String, GenModel_971_>() {
    override fun map(input: String): GenModel_971_ {
        val parts = input.split(":")
        return GenModel_971_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_971_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_971_,
    private val saveUseCase: GenSaveUseCase_971_,
    private val deleteUseCase: GenDeleteUseCase_971_,
    private val searchUseCase: GenSearchUseCase_971_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_971_>(GenState_971_.Idle)
    val state: StateFlow<GenState_971_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_971_) {
        when (event) {
            is GenEvent_971_.Load -> loadAll()
            is GenEvent_971_.Update -> save(event.model)
            is GenEvent_971_.Delete -> delete(event.id)
            is GenEvent_971_.Refresh -> loadAll()
            is GenEvent_971_.Search -> search(event.query)
            is GenEvent_971_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_971_.Loading; _state.value = GenState_971_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_971_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_971_.Success(searchUseCase(query)) } }
}
