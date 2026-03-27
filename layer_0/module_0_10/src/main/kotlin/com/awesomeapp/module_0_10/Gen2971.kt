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

data class GenModel_2971_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2971_ {
    data class Load(val id: Long) : GenEvent_2971_()
    data class Update(val model: GenModel_2971_) : GenEvent_2971_()
    data class Delete(val id: Long) : GenEvent_2971_()
    data object Refresh : GenEvent_2971_()
    data class Search(val query: String) : GenEvent_2971_()
    data class Filter(val predicate: String) : GenEvent_2971_()
}

sealed class GenState_2971_ {
    data object Idle : GenState_2971_()
    data object Loading : GenState_2971_()
    data class Success(val items: List<GenModel_2971_>) : GenState_2971_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2971_()
    data class Partial(val items: List<GenModel_2971_>, val hasMore: Boolean) : GenState_2971_()
}

interface GenRepository_2971_ {
    suspend fun getAll(): List<GenModel_2971_>
    suspend fun getById(id: Long): GenModel_2971_?
    suspend fun save(model: GenModel_2971_): GenModel_2971_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2971_>
}

@Singleton
class GenRepositoryImpl_2971_ @Inject constructor() : GenRepository_2971_ {
    private val store = mutableMapOf<Long, GenModel_2971_>()
    override suspend fun getAll(): List<GenModel_2971_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2971_? = store[id]
    override suspend fun save(model: GenModel_2971_): GenModel_2971_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2971_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2971_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2971_ @Inject constructor(
    private val repository: GenRepositoryImpl_2971_
) : GenUseCase_2971_<Unit, List<GenModel_2971_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2971_> = repository.getAll()
}

class GenSaveUseCase_2971_ @Inject constructor(
    private val repository: GenRepositoryImpl_2971_
) : GenUseCase_2971_<GenModel_2971_, GenModel_2971_> {
    override suspend fun invoke(params: GenModel_2971_): GenModel_2971_ = repository.save(params)
}

class GenDeleteUseCase_2971_ @Inject constructor(
    private val repository: GenRepositoryImpl_2971_
) : GenUseCase_2971_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2971_ @Inject constructor(
    private val repository: GenRepositoryImpl_2971_
) : GenUseCase_2971_<String, List<GenModel_2971_>> {
    override suspend fun invoke(params: String): List<GenModel_2971_> = repository.search(params)
}

abstract class GenMapper_2971_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2971_ : GenMapper_2971_<GenModel_2971_, String>() {
    override fun map(input: GenModel_2971_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2971_ : GenMapper_2971_<String, GenModel_2971_>() {
    override fun map(input: String): GenModel_2971_ {
        val parts = input.split(":")
        return GenModel_2971_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2971_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2971_,
    private val saveUseCase: GenSaveUseCase_2971_,
    private val deleteUseCase: GenDeleteUseCase_2971_,
    private val searchUseCase: GenSearchUseCase_2971_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2971_>(GenState_2971_.Idle)
    val state: StateFlow<GenState_2971_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2971_) {
        when (event) {
            is GenEvent_2971_.Load -> loadAll()
            is GenEvent_2971_.Update -> save(event.model)
            is GenEvent_2971_.Delete -> delete(event.id)
            is GenEvent_2971_.Refresh -> loadAll()
            is GenEvent_2971_.Search -> search(event.query)
            is GenEvent_2971_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2971_.Loading; _state.value = GenState_2971_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2971_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2971_.Success(searchUseCase(query)) } }
}
