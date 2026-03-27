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

data class GenModel_8_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_8_ {
    data class Load(val id: Long) : GenEvent_8_()
    data class Update(val model: GenModel_8_) : GenEvent_8_()
    data class Delete(val id: Long) : GenEvent_8_()
    data object Refresh : GenEvent_8_()
    data class Search(val query: String) : GenEvent_8_()
    data class Filter(val predicate: String) : GenEvent_8_()
}

sealed class GenState_8_ {
    data object Idle : GenState_8_()
    data object Loading : GenState_8_()
    data class Success(val items: List<GenModel_8_>) : GenState_8_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_8_()
    data class Partial(val items: List<GenModel_8_>, val hasMore: Boolean) : GenState_8_()
}

interface GenRepository_8_ {
    suspend fun getAll(): List<GenModel_8_>
    suspend fun getById(id: Long): GenModel_8_?
    suspend fun save(model: GenModel_8_): GenModel_8_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_8_>
}

@Singleton
class GenRepositoryImpl_8_ @Inject constructor() : GenRepository_8_ {
    private val store = mutableMapOf<Long, GenModel_8_>()
    override suspend fun getAll(): List<GenModel_8_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_8_? = store[id]
    override suspend fun save(model: GenModel_8_): GenModel_8_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_8_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_8_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_8_ @Inject constructor(
    private val repository: GenRepositoryImpl_8_
) : GenUseCase_8_<Unit, List<GenModel_8_>> {
    override suspend fun invoke(params: Unit): List<GenModel_8_> = repository.getAll()
}

class GenSaveUseCase_8_ @Inject constructor(
    private val repository: GenRepositoryImpl_8_
) : GenUseCase_8_<GenModel_8_, GenModel_8_> {
    override suspend fun invoke(params: GenModel_8_): GenModel_8_ = repository.save(params)
}

class GenDeleteUseCase_8_ @Inject constructor(
    private val repository: GenRepositoryImpl_8_
) : GenUseCase_8_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_8_ @Inject constructor(
    private val repository: GenRepositoryImpl_8_
) : GenUseCase_8_<String, List<GenModel_8_>> {
    override suspend fun invoke(params: String): List<GenModel_8_> = repository.search(params)
}

abstract class GenMapper_8_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_8_ : GenMapper_8_<GenModel_8_, String>() {
    override fun map(input: GenModel_8_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_8_ : GenMapper_8_<String, GenModel_8_>() {
    override fun map(input: String): GenModel_8_ {
        val parts = input.split(":")
        return GenModel_8_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_8_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_8_,
    private val saveUseCase: GenSaveUseCase_8_,
    private val deleteUseCase: GenDeleteUseCase_8_,
    private val searchUseCase: GenSearchUseCase_8_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_8_>(GenState_8_.Idle)
    val state: StateFlow<GenState_8_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_8_) {
        when (event) {
            is GenEvent_8_.Load -> loadAll()
            is GenEvent_8_.Update -> save(event.model)
            is GenEvent_8_.Delete -> delete(event.id)
            is GenEvent_8_.Refresh -> loadAll()
            is GenEvent_8_.Search -> search(event.query)
            is GenEvent_8_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_8_.Loading; _state.value = GenState_8_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_8_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_8_.Success(searchUseCase(query)) } }
}
