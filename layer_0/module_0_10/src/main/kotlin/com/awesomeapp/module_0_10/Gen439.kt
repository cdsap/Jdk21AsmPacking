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

data class GenModel_439_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_439_ {
    data class Load(val id: Long) : GenEvent_439_()
    data class Update(val model: GenModel_439_) : GenEvent_439_()
    data class Delete(val id: Long) : GenEvent_439_()
    data object Refresh : GenEvent_439_()
    data class Search(val query: String) : GenEvent_439_()
    data class Filter(val predicate: String) : GenEvent_439_()
}

sealed class GenState_439_ {
    data object Idle : GenState_439_()
    data object Loading : GenState_439_()
    data class Success(val items: List<GenModel_439_>) : GenState_439_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_439_()
    data class Partial(val items: List<GenModel_439_>, val hasMore: Boolean) : GenState_439_()
}

interface GenRepository_439_ {
    suspend fun getAll(): List<GenModel_439_>
    suspend fun getById(id: Long): GenModel_439_?
    suspend fun save(model: GenModel_439_): GenModel_439_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_439_>
}

@Singleton
class GenRepositoryImpl_439_ @Inject constructor() : GenRepository_439_ {
    private val store = mutableMapOf<Long, GenModel_439_>()
    override suspend fun getAll(): List<GenModel_439_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_439_? = store[id]
    override suspend fun save(model: GenModel_439_): GenModel_439_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_439_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_439_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_439_ @Inject constructor(
    private val repository: GenRepositoryImpl_439_
) : GenUseCase_439_<Unit, List<GenModel_439_>> {
    override suspend fun invoke(params: Unit): List<GenModel_439_> = repository.getAll()
}

class GenSaveUseCase_439_ @Inject constructor(
    private val repository: GenRepositoryImpl_439_
) : GenUseCase_439_<GenModel_439_, GenModel_439_> {
    override suspend fun invoke(params: GenModel_439_): GenModel_439_ = repository.save(params)
}

class GenDeleteUseCase_439_ @Inject constructor(
    private val repository: GenRepositoryImpl_439_
) : GenUseCase_439_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_439_ @Inject constructor(
    private val repository: GenRepositoryImpl_439_
) : GenUseCase_439_<String, List<GenModel_439_>> {
    override suspend fun invoke(params: String): List<GenModel_439_> = repository.search(params)
}

abstract class GenMapper_439_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_439_ : GenMapper_439_<GenModel_439_, String>() {
    override fun map(input: GenModel_439_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_439_ : GenMapper_439_<String, GenModel_439_>() {
    override fun map(input: String): GenModel_439_ {
        val parts = input.split(":")
        return GenModel_439_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_439_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_439_,
    private val saveUseCase: GenSaveUseCase_439_,
    private val deleteUseCase: GenDeleteUseCase_439_,
    private val searchUseCase: GenSearchUseCase_439_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_439_>(GenState_439_.Idle)
    val state: StateFlow<GenState_439_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_439_) {
        when (event) {
            is GenEvent_439_.Load -> loadAll()
            is GenEvent_439_.Update -> save(event.model)
            is GenEvent_439_.Delete -> delete(event.id)
            is GenEvent_439_.Refresh -> loadAll()
            is GenEvent_439_.Search -> search(event.query)
            is GenEvent_439_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_439_.Loading; _state.value = GenState_439_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_439_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_439_.Success(searchUseCase(query)) } }
}
