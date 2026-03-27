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

data class GenModel_2489_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2489_ {
    data class Load(val id: Long) : GenEvent_2489_()
    data class Update(val model: GenModel_2489_) : GenEvent_2489_()
    data class Delete(val id: Long) : GenEvent_2489_()
    data object Refresh : GenEvent_2489_()
    data class Search(val query: String) : GenEvent_2489_()
    data class Filter(val predicate: String) : GenEvent_2489_()
}

sealed class GenState_2489_ {
    data object Idle : GenState_2489_()
    data object Loading : GenState_2489_()
    data class Success(val items: List<GenModel_2489_>) : GenState_2489_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2489_()
    data class Partial(val items: List<GenModel_2489_>, val hasMore: Boolean) : GenState_2489_()
}

interface GenRepository_2489_ {
    suspend fun getAll(): List<GenModel_2489_>
    suspend fun getById(id: Long): GenModel_2489_?
    suspend fun save(model: GenModel_2489_): GenModel_2489_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2489_>
}

@Singleton
class GenRepositoryImpl_2489_ @Inject constructor() : GenRepository_2489_ {
    private val store = mutableMapOf<Long, GenModel_2489_>()
    override suspend fun getAll(): List<GenModel_2489_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2489_? = store[id]
    override suspend fun save(model: GenModel_2489_): GenModel_2489_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2489_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2489_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2489_ @Inject constructor(
    private val repository: GenRepositoryImpl_2489_
) : GenUseCase_2489_<Unit, List<GenModel_2489_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2489_> = repository.getAll()
}

class GenSaveUseCase_2489_ @Inject constructor(
    private val repository: GenRepositoryImpl_2489_
) : GenUseCase_2489_<GenModel_2489_, GenModel_2489_> {
    override suspend fun invoke(params: GenModel_2489_): GenModel_2489_ = repository.save(params)
}

class GenDeleteUseCase_2489_ @Inject constructor(
    private val repository: GenRepositoryImpl_2489_
) : GenUseCase_2489_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2489_ @Inject constructor(
    private val repository: GenRepositoryImpl_2489_
) : GenUseCase_2489_<String, List<GenModel_2489_>> {
    override suspend fun invoke(params: String): List<GenModel_2489_> = repository.search(params)
}

abstract class GenMapper_2489_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2489_ : GenMapper_2489_<GenModel_2489_, String>() {
    override fun map(input: GenModel_2489_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2489_ : GenMapper_2489_<String, GenModel_2489_>() {
    override fun map(input: String): GenModel_2489_ {
        val parts = input.split(":")
        return GenModel_2489_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2489_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2489_,
    private val saveUseCase: GenSaveUseCase_2489_,
    private val deleteUseCase: GenDeleteUseCase_2489_,
    private val searchUseCase: GenSearchUseCase_2489_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2489_>(GenState_2489_.Idle)
    val state: StateFlow<GenState_2489_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2489_) {
        when (event) {
            is GenEvent_2489_.Load -> loadAll()
            is GenEvent_2489_.Update -> save(event.model)
            is GenEvent_2489_.Delete -> delete(event.id)
            is GenEvent_2489_.Refresh -> loadAll()
            is GenEvent_2489_.Search -> search(event.query)
            is GenEvent_2489_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2489_.Loading; _state.value = GenState_2489_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2489_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2489_.Success(searchUseCase(query)) } }
}
