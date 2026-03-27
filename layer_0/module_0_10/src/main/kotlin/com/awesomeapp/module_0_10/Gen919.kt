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

data class GenModel_919_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_919_ {
    data class Load(val id: Long) : GenEvent_919_()
    data class Update(val model: GenModel_919_) : GenEvent_919_()
    data class Delete(val id: Long) : GenEvent_919_()
    data object Refresh : GenEvent_919_()
    data class Search(val query: String) : GenEvent_919_()
    data class Filter(val predicate: String) : GenEvent_919_()
}

sealed class GenState_919_ {
    data object Idle : GenState_919_()
    data object Loading : GenState_919_()
    data class Success(val items: List<GenModel_919_>) : GenState_919_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_919_()
    data class Partial(val items: List<GenModel_919_>, val hasMore: Boolean) : GenState_919_()
}

interface GenRepository_919_ {
    suspend fun getAll(): List<GenModel_919_>
    suspend fun getById(id: Long): GenModel_919_?
    suspend fun save(model: GenModel_919_): GenModel_919_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_919_>
}

@Singleton
class GenRepositoryImpl_919_ @Inject constructor() : GenRepository_919_ {
    private val store = mutableMapOf<Long, GenModel_919_>()
    override suspend fun getAll(): List<GenModel_919_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_919_? = store[id]
    override suspend fun save(model: GenModel_919_): GenModel_919_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_919_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_919_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_919_ @Inject constructor(
    private val repository: GenRepositoryImpl_919_
) : GenUseCase_919_<Unit, List<GenModel_919_>> {
    override suspend fun invoke(params: Unit): List<GenModel_919_> = repository.getAll()
}

class GenSaveUseCase_919_ @Inject constructor(
    private val repository: GenRepositoryImpl_919_
) : GenUseCase_919_<GenModel_919_, GenModel_919_> {
    override suspend fun invoke(params: GenModel_919_): GenModel_919_ = repository.save(params)
}

class GenDeleteUseCase_919_ @Inject constructor(
    private val repository: GenRepositoryImpl_919_
) : GenUseCase_919_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_919_ @Inject constructor(
    private val repository: GenRepositoryImpl_919_
) : GenUseCase_919_<String, List<GenModel_919_>> {
    override suspend fun invoke(params: String): List<GenModel_919_> = repository.search(params)
}

abstract class GenMapper_919_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_919_ : GenMapper_919_<GenModel_919_, String>() {
    override fun map(input: GenModel_919_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_919_ : GenMapper_919_<String, GenModel_919_>() {
    override fun map(input: String): GenModel_919_ {
        val parts = input.split(":")
        return GenModel_919_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_919_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_919_,
    private val saveUseCase: GenSaveUseCase_919_,
    private val deleteUseCase: GenDeleteUseCase_919_,
    private val searchUseCase: GenSearchUseCase_919_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_919_>(GenState_919_.Idle)
    val state: StateFlow<GenState_919_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_919_) {
        when (event) {
            is GenEvent_919_.Load -> loadAll()
            is GenEvent_919_.Update -> save(event.model)
            is GenEvent_919_.Delete -> delete(event.id)
            is GenEvent_919_.Refresh -> loadAll()
            is GenEvent_919_.Search -> search(event.query)
            is GenEvent_919_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_919_.Loading; _state.value = GenState_919_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_919_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_919_.Success(searchUseCase(query)) } }
}
