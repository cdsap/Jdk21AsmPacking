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

data class GenModel_1669_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1669_ {
    data class Load(val id: Long) : GenEvent_1669_()
    data class Update(val model: GenModel_1669_) : GenEvent_1669_()
    data class Delete(val id: Long) : GenEvent_1669_()
    data object Refresh : GenEvent_1669_()
    data class Search(val query: String) : GenEvent_1669_()
    data class Filter(val predicate: String) : GenEvent_1669_()
}

sealed class GenState_1669_ {
    data object Idle : GenState_1669_()
    data object Loading : GenState_1669_()
    data class Success(val items: List<GenModel_1669_>) : GenState_1669_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1669_()
    data class Partial(val items: List<GenModel_1669_>, val hasMore: Boolean) : GenState_1669_()
}

interface GenRepository_1669_ {
    suspend fun getAll(): List<GenModel_1669_>
    suspend fun getById(id: Long): GenModel_1669_?
    suspend fun save(model: GenModel_1669_): GenModel_1669_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1669_>
}

@Singleton
class GenRepositoryImpl_1669_ @Inject constructor() : GenRepository_1669_ {
    private val store = mutableMapOf<Long, GenModel_1669_>()
    override suspend fun getAll(): List<GenModel_1669_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1669_? = store[id]
    override suspend fun save(model: GenModel_1669_): GenModel_1669_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1669_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1669_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1669_ @Inject constructor(
    private val repository: GenRepositoryImpl_1669_
) : GenUseCase_1669_<Unit, List<GenModel_1669_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1669_> = repository.getAll()
}

class GenSaveUseCase_1669_ @Inject constructor(
    private val repository: GenRepositoryImpl_1669_
) : GenUseCase_1669_<GenModel_1669_, GenModel_1669_> {
    override suspend fun invoke(params: GenModel_1669_): GenModel_1669_ = repository.save(params)
}

class GenDeleteUseCase_1669_ @Inject constructor(
    private val repository: GenRepositoryImpl_1669_
) : GenUseCase_1669_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1669_ @Inject constructor(
    private val repository: GenRepositoryImpl_1669_
) : GenUseCase_1669_<String, List<GenModel_1669_>> {
    override suspend fun invoke(params: String): List<GenModel_1669_> = repository.search(params)
}

abstract class GenMapper_1669_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1669_ : GenMapper_1669_<GenModel_1669_, String>() {
    override fun map(input: GenModel_1669_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1669_ : GenMapper_1669_<String, GenModel_1669_>() {
    override fun map(input: String): GenModel_1669_ {
        val parts = input.split(":")
        return GenModel_1669_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1669_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1669_,
    private val saveUseCase: GenSaveUseCase_1669_,
    private val deleteUseCase: GenDeleteUseCase_1669_,
    private val searchUseCase: GenSearchUseCase_1669_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1669_>(GenState_1669_.Idle)
    val state: StateFlow<GenState_1669_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1669_) {
        when (event) {
            is GenEvent_1669_.Load -> loadAll()
            is GenEvent_1669_.Update -> save(event.model)
            is GenEvent_1669_.Delete -> delete(event.id)
            is GenEvent_1669_.Refresh -> loadAll()
            is GenEvent_1669_.Search -> search(event.query)
            is GenEvent_1669_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1669_.Loading; _state.value = GenState_1669_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1669_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1669_.Success(searchUseCase(query)) } }
}
