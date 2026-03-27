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

data class GenModel_955_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_955_ {
    data class Load(val id: Long) : GenEvent_955_()
    data class Update(val model: GenModel_955_) : GenEvent_955_()
    data class Delete(val id: Long) : GenEvent_955_()
    data object Refresh : GenEvent_955_()
    data class Search(val query: String) : GenEvent_955_()
    data class Filter(val predicate: String) : GenEvent_955_()
}

sealed class GenState_955_ {
    data object Idle : GenState_955_()
    data object Loading : GenState_955_()
    data class Success(val items: List<GenModel_955_>) : GenState_955_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_955_()
    data class Partial(val items: List<GenModel_955_>, val hasMore: Boolean) : GenState_955_()
}

interface GenRepository_955_ {
    suspend fun getAll(): List<GenModel_955_>
    suspend fun getById(id: Long): GenModel_955_?
    suspend fun save(model: GenModel_955_): GenModel_955_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_955_>
}

@Singleton
class GenRepositoryImpl_955_ @Inject constructor() : GenRepository_955_ {
    private val store = mutableMapOf<Long, GenModel_955_>()
    override suspend fun getAll(): List<GenModel_955_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_955_? = store[id]
    override suspend fun save(model: GenModel_955_): GenModel_955_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_955_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_955_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_955_ @Inject constructor(
    private val repository: GenRepositoryImpl_955_
) : GenUseCase_955_<Unit, List<GenModel_955_>> {
    override suspend fun invoke(params: Unit): List<GenModel_955_> = repository.getAll()
}

class GenSaveUseCase_955_ @Inject constructor(
    private val repository: GenRepositoryImpl_955_
) : GenUseCase_955_<GenModel_955_, GenModel_955_> {
    override suspend fun invoke(params: GenModel_955_): GenModel_955_ = repository.save(params)
}

class GenDeleteUseCase_955_ @Inject constructor(
    private val repository: GenRepositoryImpl_955_
) : GenUseCase_955_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_955_ @Inject constructor(
    private val repository: GenRepositoryImpl_955_
) : GenUseCase_955_<String, List<GenModel_955_>> {
    override suspend fun invoke(params: String): List<GenModel_955_> = repository.search(params)
}

abstract class GenMapper_955_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_955_ : GenMapper_955_<GenModel_955_, String>() {
    override fun map(input: GenModel_955_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_955_ : GenMapper_955_<String, GenModel_955_>() {
    override fun map(input: String): GenModel_955_ {
        val parts = input.split(":")
        return GenModel_955_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_955_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_955_,
    private val saveUseCase: GenSaveUseCase_955_,
    private val deleteUseCase: GenDeleteUseCase_955_,
    private val searchUseCase: GenSearchUseCase_955_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_955_>(GenState_955_.Idle)
    val state: StateFlow<GenState_955_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_955_) {
        when (event) {
            is GenEvent_955_.Load -> loadAll()
            is GenEvent_955_.Update -> save(event.model)
            is GenEvent_955_.Delete -> delete(event.id)
            is GenEvent_955_.Refresh -> loadAll()
            is GenEvent_955_.Search -> search(event.query)
            is GenEvent_955_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_955_.Loading; _state.value = GenState_955_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_955_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_955_.Success(searchUseCase(query)) } }
}
