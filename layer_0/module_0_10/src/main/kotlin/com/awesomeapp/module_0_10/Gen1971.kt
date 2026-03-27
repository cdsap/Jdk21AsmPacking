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

data class GenModel_1971_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1971_ {
    data class Load(val id: Long) : GenEvent_1971_()
    data class Update(val model: GenModel_1971_) : GenEvent_1971_()
    data class Delete(val id: Long) : GenEvent_1971_()
    data object Refresh : GenEvent_1971_()
    data class Search(val query: String) : GenEvent_1971_()
    data class Filter(val predicate: String) : GenEvent_1971_()
}

sealed class GenState_1971_ {
    data object Idle : GenState_1971_()
    data object Loading : GenState_1971_()
    data class Success(val items: List<GenModel_1971_>) : GenState_1971_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1971_()
    data class Partial(val items: List<GenModel_1971_>, val hasMore: Boolean) : GenState_1971_()
}

interface GenRepository_1971_ {
    suspend fun getAll(): List<GenModel_1971_>
    suspend fun getById(id: Long): GenModel_1971_?
    suspend fun save(model: GenModel_1971_): GenModel_1971_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1971_>
}

@Singleton
class GenRepositoryImpl_1971_ @Inject constructor() : GenRepository_1971_ {
    private val store = mutableMapOf<Long, GenModel_1971_>()
    override suspend fun getAll(): List<GenModel_1971_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1971_? = store[id]
    override suspend fun save(model: GenModel_1971_): GenModel_1971_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1971_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1971_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1971_ @Inject constructor(
    private val repository: GenRepositoryImpl_1971_
) : GenUseCase_1971_<Unit, List<GenModel_1971_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1971_> = repository.getAll()
}

class GenSaveUseCase_1971_ @Inject constructor(
    private val repository: GenRepositoryImpl_1971_
) : GenUseCase_1971_<GenModel_1971_, GenModel_1971_> {
    override suspend fun invoke(params: GenModel_1971_): GenModel_1971_ = repository.save(params)
}

class GenDeleteUseCase_1971_ @Inject constructor(
    private val repository: GenRepositoryImpl_1971_
) : GenUseCase_1971_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1971_ @Inject constructor(
    private val repository: GenRepositoryImpl_1971_
) : GenUseCase_1971_<String, List<GenModel_1971_>> {
    override suspend fun invoke(params: String): List<GenModel_1971_> = repository.search(params)
}

abstract class GenMapper_1971_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1971_ : GenMapper_1971_<GenModel_1971_, String>() {
    override fun map(input: GenModel_1971_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1971_ : GenMapper_1971_<String, GenModel_1971_>() {
    override fun map(input: String): GenModel_1971_ {
        val parts = input.split(":")
        return GenModel_1971_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1971_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1971_,
    private val saveUseCase: GenSaveUseCase_1971_,
    private val deleteUseCase: GenDeleteUseCase_1971_,
    private val searchUseCase: GenSearchUseCase_1971_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1971_>(GenState_1971_.Idle)
    val state: StateFlow<GenState_1971_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1971_) {
        when (event) {
            is GenEvent_1971_.Load -> loadAll()
            is GenEvent_1971_.Update -> save(event.model)
            is GenEvent_1971_.Delete -> delete(event.id)
            is GenEvent_1971_.Refresh -> loadAll()
            is GenEvent_1971_.Search -> search(event.query)
            is GenEvent_1971_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1971_.Loading; _state.value = GenState_1971_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1971_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1971_.Success(searchUseCase(query)) } }
}
