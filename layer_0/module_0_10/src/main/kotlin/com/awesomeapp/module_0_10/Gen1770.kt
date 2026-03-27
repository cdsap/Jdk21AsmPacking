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

data class GenModel_1770_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1770_ {
    data class Load(val id: Long) : GenEvent_1770_()
    data class Update(val model: GenModel_1770_) : GenEvent_1770_()
    data class Delete(val id: Long) : GenEvent_1770_()
    data object Refresh : GenEvent_1770_()
    data class Search(val query: String) : GenEvent_1770_()
    data class Filter(val predicate: String) : GenEvent_1770_()
}

sealed class GenState_1770_ {
    data object Idle : GenState_1770_()
    data object Loading : GenState_1770_()
    data class Success(val items: List<GenModel_1770_>) : GenState_1770_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1770_()
    data class Partial(val items: List<GenModel_1770_>, val hasMore: Boolean) : GenState_1770_()
}

interface GenRepository_1770_ {
    suspend fun getAll(): List<GenModel_1770_>
    suspend fun getById(id: Long): GenModel_1770_?
    suspend fun save(model: GenModel_1770_): GenModel_1770_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1770_>
}

@Singleton
class GenRepositoryImpl_1770_ @Inject constructor() : GenRepository_1770_ {
    private val store = mutableMapOf<Long, GenModel_1770_>()
    override suspend fun getAll(): List<GenModel_1770_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1770_? = store[id]
    override suspend fun save(model: GenModel_1770_): GenModel_1770_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1770_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1770_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1770_ @Inject constructor(
    private val repository: GenRepositoryImpl_1770_
) : GenUseCase_1770_<Unit, List<GenModel_1770_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1770_> = repository.getAll()
}

class GenSaveUseCase_1770_ @Inject constructor(
    private val repository: GenRepositoryImpl_1770_
) : GenUseCase_1770_<GenModel_1770_, GenModel_1770_> {
    override suspend fun invoke(params: GenModel_1770_): GenModel_1770_ = repository.save(params)
}

class GenDeleteUseCase_1770_ @Inject constructor(
    private val repository: GenRepositoryImpl_1770_
) : GenUseCase_1770_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1770_ @Inject constructor(
    private val repository: GenRepositoryImpl_1770_
) : GenUseCase_1770_<String, List<GenModel_1770_>> {
    override suspend fun invoke(params: String): List<GenModel_1770_> = repository.search(params)
}

abstract class GenMapper_1770_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1770_ : GenMapper_1770_<GenModel_1770_, String>() {
    override fun map(input: GenModel_1770_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1770_ : GenMapper_1770_<String, GenModel_1770_>() {
    override fun map(input: String): GenModel_1770_ {
        val parts = input.split(":")
        return GenModel_1770_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1770_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1770_,
    private val saveUseCase: GenSaveUseCase_1770_,
    private val deleteUseCase: GenDeleteUseCase_1770_,
    private val searchUseCase: GenSearchUseCase_1770_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1770_>(GenState_1770_.Idle)
    val state: StateFlow<GenState_1770_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1770_) {
        when (event) {
            is GenEvent_1770_.Load -> loadAll()
            is GenEvent_1770_.Update -> save(event.model)
            is GenEvent_1770_.Delete -> delete(event.id)
            is GenEvent_1770_.Refresh -> loadAll()
            is GenEvent_1770_.Search -> search(event.query)
            is GenEvent_1770_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1770_.Loading; _state.value = GenState_1770_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1770_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1770_.Success(searchUseCase(query)) } }
}
