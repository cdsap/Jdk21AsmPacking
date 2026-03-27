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

data class GenModel_827_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_827_ {
    data class Load(val id: Long) : GenEvent_827_()
    data class Update(val model: GenModel_827_) : GenEvent_827_()
    data class Delete(val id: Long) : GenEvent_827_()
    data object Refresh : GenEvent_827_()
    data class Search(val query: String) : GenEvent_827_()
    data class Filter(val predicate: String) : GenEvent_827_()
}

sealed class GenState_827_ {
    data object Idle : GenState_827_()
    data object Loading : GenState_827_()
    data class Success(val items: List<GenModel_827_>) : GenState_827_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_827_()
    data class Partial(val items: List<GenModel_827_>, val hasMore: Boolean) : GenState_827_()
}

interface GenRepository_827_ {
    suspend fun getAll(): List<GenModel_827_>
    suspend fun getById(id: Long): GenModel_827_?
    suspend fun save(model: GenModel_827_): GenModel_827_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_827_>
}

@Singleton
class GenRepositoryImpl_827_ @Inject constructor() : GenRepository_827_ {
    private val store = mutableMapOf<Long, GenModel_827_>()
    override suspend fun getAll(): List<GenModel_827_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_827_? = store[id]
    override suspend fun save(model: GenModel_827_): GenModel_827_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_827_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_827_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_827_ @Inject constructor(
    private val repository: GenRepositoryImpl_827_
) : GenUseCase_827_<Unit, List<GenModel_827_>> {
    override suspend fun invoke(params: Unit): List<GenModel_827_> = repository.getAll()
}

class GenSaveUseCase_827_ @Inject constructor(
    private val repository: GenRepositoryImpl_827_
) : GenUseCase_827_<GenModel_827_, GenModel_827_> {
    override suspend fun invoke(params: GenModel_827_): GenModel_827_ = repository.save(params)
}

class GenDeleteUseCase_827_ @Inject constructor(
    private val repository: GenRepositoryImpl_827_
) : GenUseCase_827_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_827_ @Inject constructor(
    private val repository: GenRepositoryImpl_827_
) : GenUseCase_827_<String, List<GenModel_827_>> {
    override suspend fun invoke(params: String): List<GenModel_827_> = repository.search(params)
}

abstract class GenMapper_827_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_827_ : GenMapper_827_<GenModel_827_, String>() {
    override fun map(input: GenModel_827_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_827_ : GenMapper_827_<String, GenModel_827_>() {
    override fun map(input: String): GenModel_827_ {
        val parts = input.split(":")
        return GenModel_827_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_827_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_827_,
    private val saveUseCase: GenSaveUseCase_827_,
    private val deleteUseCase: GenDeleteUseCase_827_,
    private val searchUseCase: GenSearchUseCase_827_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_827_>(GenState_827_.Idle)
    val state: StateFlow<GenState_827_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_827_) {
        when (event) {
            is GenEvent_827_.Load -> loadAll()
            is GenEvent_827_.Update -> save(event.model)
            is GenEvent_827_.Delete -> delete(event.id)
            is GenEvent_827_.Refresh -> loadAll()
            is GenEvent_827_.Search -> search(event.query)
            is GenEvent_827_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_827_.Loading; _state.value = GenState_827_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_827_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_827_.Success(searchUseCase(query)) } }
}
