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

data class GenModel_771_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_771_ {
    data class Load(val id: Long) : GenEvent_771_()
    data class Update(val model: GenModel_771_) : GenEvent_771_()
    data class Delete(val id: Long) : GenEvent_771_()
    data object Refresh : GenEvent_771_()
    data class Search(val query: String) : GenEvent_771_()
    data class Filter(val predicate: String) : GenEvent_771_()
}

sealed class GenState_771_ {
    data object Idle : GenState_771_()
    data object Loading : GenState_771_()
    data class Success(val items: List<GenModel_771_>) : GenState_771_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_771_()
    data class Partial(val items: List<GenModel_771_>, val hasMore: Boolean) : GenState_771_()
}

interface GenRepository_771_ {
    suspend fun getAll(): List<GenModel_771_>
    suspend fun getById(id: Long): GenModel_771_?
    suspend fun save(model: GenModel_771_): GenModel_771_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_771_>
}

@Singleton
class GenRepositoryImpl_771_ @Inject constructor() : GenRepository_771_ {
    private val store = mutableMapOf<Long, GenModel_771_>()
    override suspend fun getAll(): List<GenModel_771_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_771_? = store[id]
    override suspend fun save(model: GenModel_771_): GenModel_771_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_771_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_771_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_771_ @Inject constructor(
    private val repository: GenRepositoryImpl_771_
) : GenUseCase_771_<Unit, List<GenModel_771_>> {
    override suspend fun invoke(params: Unit): List<GenModel_771_> = repository.getAll()
}

class GenSaveUseCase_771_ @Inject constructor(
    private val repository: GenRepositoryImpl_771_
) : GenUseCase_771_<GenModel_771_, GenModel_771_> {
    override suspend fun invoke(params: GenModel_771_): GenModel_771_ = repository.save(params)
}

class GenDeleteUseCase_771_ @Inject constructor(
    private val repository: GenRepositoryImpl_771_
) : GenUseCase_771_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_771_ @Inject constructor(
    private val repository: GenRepositoryImpl_771_
) : GenUseCase_771_<String, List<GenModel_771_>> {
    override suspend fun invoke(params: String): List<GenModel_771_> = repository.search(params)
}

abstract class GenMapper_771_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_771_ : GenMapper_771_<GenModel_771_, String>() {
    override fun map(input: GenModel_771_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_771_ : GenMapper_771_<String, GenModel_771_>() {
    override fun map(input: String): GenModel_771_ {
        val parts = input.split(":")
        return GenModel_771_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_771_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_771_,
    private val saveUseCase: GenSaveUseCase_771_,
    private val deleteUseCase: GenDeleteUseCase_771_,
    private val searchUseCase: GenSearchUseCase_771_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_771_>(GenState_771_.Idle)
    val state: StateFlow<GenState_771_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_771_) {
        when (event) {
            is GenEvent_771_.Load -> loadAll()
            is GenEvent_771_.Update -> save(event.model)
            is GenEvent_771_.Delete -> delete(event.id)
            is GenEvent_771_.Refresh -> loadAll()
            is GenEvent_771_.Search -> search(event.query)
            is GenEvent_771_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_771_.Loading; _state.value = GenState_771_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_771_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_771_.Success(searchUseCase(query)) } }
}
