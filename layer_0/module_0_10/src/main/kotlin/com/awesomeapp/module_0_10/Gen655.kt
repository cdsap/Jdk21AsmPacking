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

data class GenModel_655_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_655_ {
    data class Load(val id: Long) : GenEvent_655_()
    data class Update(val model: GenModel_655_) : GenEvent_655_()
    data class Delete(val id: Long) : GenEvent_655_()
    data object Refresh : GenEvent_655_()
    data class Search(val query: String) : GenEvent_655_()
    data class Filter(val predicate: String) : GenEvent_655_()
}

sealed class GenState_655_ {
    data object Idle : GenState_655_()
    data object Loading : GenState_655_()
    data class Success(val items: List<GenModel_655_>) : GenState_655_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_655_()
    data class Partial(val items: List<GenModel_655_>, val hasMore: Boolean) : GenState_655_()
}

interface GenRepository_655_ {
    suspend fun getAll(): List<GenModel_655_>
    suspend fun getById(id: Long): GenModel_655_?
    suspend fun save(model: GenModel_655_): GenModel_655_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_655_>
}

@Singleton
class GenRepositoryImpl_655_ @Inject constructor() : GenRepository_655_ {
    private val store = mutableMapOf<Long, GenModel_655_>()
    override suspend fun getAll(): List<GenModel_655_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_655_? = store[id]
    override suspend fun save(model: GenModel_655_): GenModel_655_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_655_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_655_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_655_ @Inject constructor(
    private val repository: GenRepositoryImpl_655_
) : GenUseCase_655_<Unit, List<GenModel_655_>> {
    override suspend fun invoke(params: Unit): List<GenModel_655_> = repository.getAll()
}

class GenSaveUseCase_655_ @Inject constructor(
    private val repository: GenRepositoryImpl_655_
) : GenUseCase_655_<GenModel_655_, GenModel_655_> {
    override suspend fun invoke(params: GenModel_655_): GenModel_655_ = repository.save(params)
}

class GenDeleteUseCase_655_ @Inject constructor(
    private val repository: GenRepositoryImpl_655_
) : GenUseCase_655_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_655_ @Inject constructor(
    private val repository: GenRepositoryImpl_655_
) : GenUseCase_655_<String, List<GenModel_655_>> {
    override suspend fun invoke(params: String): List<GenModel_655_> = repository.search(params)
}

abstract class GenMapper_655_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_655_ : GenMapper_655_<GenModel_655_, String>() {
    override fun map(input: GenModel_655_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_655_ : GenMapper_655_<String, GenModel_655_>() {
    override fun map(input: String): GenModel_655_ {
        val parts = input.split(":")
        return GenModel_655_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_655_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_655_,
    private val saveUseCase: GenSaveUseCase_655_,
    private val deleteUseCase: GenDeleteUseCase_655_,
    private val searchUseCase: GenSearchUseCase_655_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_655_>(GenState_655_.Idle)
    val state: StateFlow<GenState_655_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_655_) {
        when (event) {
            is GenEvent_655_.Load -> loadAll()
            is GenEvent_655_.Update -> save(event.model)
            is GenEvent_655_.Delete -> delete(event.id)
            is GenEvent_655_.Refresh -> loadAll()
            is GenEvent_655_.Search -> search(event.query)
            is GenEvent_655_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_655_.Loading; _state.value = GenState_655_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_655_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_655_.Success(searchUseCase(query)) } }
}
