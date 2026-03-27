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

data class GenModel_966_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_966_ {
    data class Load(val id: Long) : GenEvent_966_()
    data class Update(val model: GenModel_966_) : GenEvent_966_()
    data class Delete(val id: Long) : GenEvent_966_()
    data object Refresh : GenEvent_966_()
    data class Search(val query: String) : GenEvent_966_()
    data class Filter(val predicate: String) : GenEvent_966_()
}

sealed class GenState_966_ {
    data object Idle : GenState_966_()
    data object Loading : GenState_966_()
    data class Success(val items: List<GenModel_966_>) : GenState_966_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_966_()
    data class Partial(val items: List<GenModel_966_>, val hasMore: Boolean) : GenState_966_()
}

interface GenRepository_966_ {
    suspend fun getAll(): List<GenModel_966_>
    suspend fun getById(id: Long): GenModel_966_?
    suspend fun save(model: GenModel_966_): GenModel_966_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_966_>
}

@Singleton
class GenRepositoryImpl_966_ @Inject constructor() : GenRepository_966_ {
    private val store = mutableMapOf<Long, GenModel_966_>()
    override suspend fun getAll(): List<GenModel_966_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_966_? = store[id]
    override suspend fun save(model: GenModel_966_): GenModel_966_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_966_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_966_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_966_ @Inject constructor(
    private val repository: GenRepositoryImpl_966_
) : GenUseCase_966_<Unit, List<GenModel_966_>> {
    override suspend fun invoke(params: Unit): List<GenModel_966_> = repository.getAll()
}

class GenSaveUseCase_966_ @Inject constructor(
    private val repository: GenRepositoryImpl_966_
) : GenUseCase_966_<GenModel_966_, GenModel_966_> {
    override suspend fun invoke(params: GenModel_966_): GenModel_966_ = repository.save(params)
}

class GenDeleteUseCase_966_ @Inject constructor(
    private val repository: GenRepositoryImpl_966_
) : GenUseCase_966_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_966_ @Inject constructor(
    private val repository: GenRepositoryImpl_966_
) : GenUseCase_966_<String, List<GenModel_966_>> {
    override suspend fun invoke(params: String): List<GenModel_966_> = repository.search(params)
}

abstract class GenMapper_966_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_966_ : GenMapper_966_<GenModel_966_, String>() {
    override fun map(input: GenModel_966_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_966_ : GenMapper_966_<String, GenModel_966_>() {
    override fun map(input: String): GenModel_966_ {
        val parts = input.split(":")
        return GenModel_966_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_966_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_966_,
    private val saveUseCase: GenSaveUseCase_966_,
    private val deleteUseCase: GenDeleteUseCase_966_,
    private val searchUseCase: GenSearchUseCase_966_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_966_>(GenState_966_.Idle)
    val state: StateFlow<GenState_966_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_966_) {
        when (event) {
            is GenEvent_966_.Load -> loadAll()
            is GenEvent_966_.Update -> save(event.model)
            is GenEvent_966_.Delete -> delete(event.id)
            is GenEvent_966_.Refresh -> loadAll()
            is GenEvent_966_.Search -> search(event.query)
            is GenEvent_966_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_966_.Loading; _state.value = GenState_966_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_966_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_966_.Success(searchUseCase(query)) } }
}
