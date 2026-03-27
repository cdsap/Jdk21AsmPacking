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

data class GenModel_2177_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2177_ {
    data class Load(val id: Long) : GenEvent_2177_()
    data class Update(val model: GenModel_2177_) : GenEvent_2177_()
    data class Delete(val id: Long) : GenEvent_2177_()
    data object Refresh : GenEvent_2177_()
    data class Search(val query: String) : GenEvent_2177_()
    data class Filter(val predicate: String) : GenEvent_2177_()
}

sealed class GenState_2177_ {
    data object Idle : GenState_2177_()
    data object Loading : GenState_2177_()
    data class Success(val items: List<GenModel_2177_>) : GenState_2177_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2177_()
    data class Partial(val items: List<GenModel_2177_>, val hasMore: Boolean) : GenState_2177_()
}

interface GenRepository_2177_ {
    suspend fun getAll(): List<GenModel_2177_>
    suspend fun getById(id: Long): GenModel_2177_?
    suspend fun save(model: GenModel_2177_): GenModel_2177_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2177_>
}

@Singleton
class GenRepositoryImpl_2177_ @Inject constructor() : GenRepository_2177_ {
    private val store = mutableMapOf<Long, GenModel_2177_>()
    override suspend fun getAll(): List<GenModel_2177_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2177_? = store[id]
    override suspend fun save(model: GenModel_2177_): GenModel_2177_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2177_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2177_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2177_ @Inject constructor(
    private val repository: GenRepositoryImpl_2177_
) : GenUseCase_2177_<Unit, List<GenModel_2177_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2177_> = repository.getAll()
}

class GenSaveUseCase_2177_ @Inject constructor(
    private val repository: GenRepositoryImpl_2177_
) : GenUseCase_2177_<GenModel_2177_, GenModel_2177_> {
    override suspend fun invoke(params: GenModel_2177_): GenModel_2177_ = repository.save(params)
}

class GenDeleteUseCase_2177_ @Inject constructor(
    private val repository: GenRepositoryImpl_2177_
) : GenUseCase_2177_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2177_ @Inject constructor(
    private val repository: GenRepositoryImpl_2177_
) : GenUseCase_2177_<String, List<GenModel_2177_>> {
    override suspend fun invoke(params: String): List<GenModel_2177_> = repository.search(params)
}

abstract class GenMapper_2177_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2177_ : GenMapper_2177_<GenModel_2177_, String>() {
    override fun map(input: GenModel_2177_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2177_ : GenMapper_2177_<String, GenModel_2177_>() {
    override fun map(input: String): GenModel_2177_ {
        val parts = input.split(":")
        return GenModel_2177_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2177_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2177_,
    private val saveUseCase: GenSaveUseCase_2177_,
    private val deleteUseCase: GenDeleteUseCase_2177_,
    private val searchUseCase: GenSearchUseCase_2177_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2177_>(GenState_2177_.Idle)
    val state: StateFlow<GenState_2177_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2177_) {
        when (event) {
            is GenEvent_2177_.Load -> loadAll()
            is GenEvent_2177_.Update -> save(event.model)
            is GenEvent_2177_.Delete -> delete(event.id)
            is GenEvent_2177_.Refresh -> loadAll()
            is GenEvent_2177_.Search -> search(event.query)
            is GenEvent_2177_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2177_.Loading; _state.value = GenState_2177_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2177_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2177_.Success(searchUseCase(query)) } }
}
