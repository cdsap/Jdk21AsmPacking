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

data class GenModel_829_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_829_ {
    data class Load(val id: Long) : GenEvent_829_()
    data class Update(val model: GenModel_829_) : GenEvent_829_()
    data class Delete(val id: Long) : GenEvent_829_()
    data object Refresh : GenEvent_829_()
    data class Search(val query: String) : GenEvent_829_()
    data class Filter(val predicate: String) : GenEvent_829_()
}

sealed class GenState_829_ {
    data object Idle : GenState_829_()
    data object Loading : GenState_829_()
    data class Success(val items: List<GenModel_829_>) : GenState_829_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_829_()
    data class Partial(val items: List<GenModel_829_>, val hasMore: Boolean) : GenState_829_()
}

interface GenRepository_829_ {
    suspend fun getAll(): List<GenModel_829_>
    suspend fun getById(id: Long): GenModel_829_?
    suspend fun save(model: GenModel_829_): GenModel_829_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_829_>
}

@Singleton
class GenRepositoryImpl_829_ @Inject constructor() : GenRepository_829_ {
    private val store = mutableMapOf<Long, GenModel_829_>()
    override suspend fun getAll(): List<GenModel_829_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_829_? = store[id]
    override suspend fun save(model: GenModel_829_): GenModel_829_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_829_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_829_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_829_ @Inject constructor(
    private val repository: GenRepositoryImpl_829_
) : GenUseCase_829_<Unit, List<GenModel_829_>> {
    override suspend fun invoke(params: Unit): List<GenModel_829_> = repository.getAll()
}

class GenSaveUseCase_829_ @Inject constructor(
    private val repository: GenRepositoryImpl_829_
) : GenUseCase_829_<GenModel_829_, GenModel_829_> {
    override suspend fun invoke(params: GenModel_829_): GenModel_829_ = repository.save(params)
}

class GenDeleteUseCase_829_ @Inject constructor(
    private val repository: GenRepositoryImpl_829_
) : GenUseCase_829_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_829_ @Inject constructor(
    private val repository: GenRepositoryImpl_829_
) : GenUseCase_829_<String, List<GenModel_829_>> {
    override suspend fun invoke(params: String): List<GenModel_829_> = repository.search(params)
}

abstract class GenMapper_829_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_829_ : GenMapper_829_<GenModel_829_, String>() {
    override fun map(input: GenModel_829_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_829_ : GenMapper_829_<String, GenModel_829_>() {
    override fun map(input: String): GenModel_829_ {
        val parts = input.split(":")
        return GenModel_829_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_829_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_829_,
    private val saveUseCase: GenSaveUseCase_829_,
    private val deleteUseCase: GenDeleteUseCase_829_,
    private val searchUseCase: GenSearchUseCase_829_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_829_>(GenState_829_.Idle)
    val state: StateFlow<GenState_829_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_829_) {
        when (event) {
            is GenEvent_829_.Load -> loadAll()
            is GenEvent_829_.Update -> save(event.model)
            is GenEvent_829_.Delete -> delete(event.id)
            is GenEvent_829_.Refresh -> loadAll()
            is GenEvent_829_.Search -> search(event.query)
            is GenEvent_829_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_829_.Loading; _state.value = GenState_829_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_829_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_829_.Success(searchUseCase(query)) } }
}
