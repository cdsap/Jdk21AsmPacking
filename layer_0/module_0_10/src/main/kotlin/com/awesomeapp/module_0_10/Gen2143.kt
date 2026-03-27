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

data class GenModel_2143_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2143_ {
    data class Load(val id: Long) : GenEvent_2143_()
    data class Update(val model: GenModel_2143_) : GenEvent_2143_()
    data class Delete(val id: Long) : GenEvent_2143_()
    data object Refresh : GenEvent_2143_()
    data class Search(val query: String) : GenEvent_2143_()
    data class Filter(val predicate: String) : GenEvent_2143_()
}

sealed class GenState_2143_ {
    data object Idle : GenState_2143_()
    data object Loading : GenState_2143_()
    data class Success(val items: List<GenModel_2143_>) : GenState_2143_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2143_()
    data class Partial(val items: List<GenModel_2143_>, val hasMore: Boolean) : GenState_2143_()
}

interface GenRepository_2143_ {
    suspend fun getAll(): List<GenModel_2143_>
    suspend fun getById(id: Long): GenModel_2143_?
    suspend fun save(model: GenModel_2143_): GenModel_2143_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2143_>
}

@Singleton
class GenRepositoryImpl_2143_ @Inject constructor() : GenRepository_2143_ {
    private val store = mutableMapOf<Long, GenModel_2143_>()
    override suspend fun getAll(): List<GenModel_2143_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2143_? = store[id]
    override suspend fun save(model: GenModel_2143_): GenModel_2143_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2143_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2143_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2143_ @Inject constructor(
    private val repository: GenRepositoryImpl_2143_
) : GenUseCase_2143_<Unit, List<GenModel_2143_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2143_> = repository.getAll()
}

class GenSaveUseCase_2143_ @Inject constructor(
    private val repository: GenRepositoryImpl_2143_
) : GenUseCase_2143_<GenModel_2143_, GenModel_2143_> {
    override suspend fun invoke(params: GenModel_2143_): GenModel_2143_ = repository.save(params)
}

class GenDeleteUseCase_2143_ @Inject constructor(
    private val repository: GenRepositoryImpl_2143_
) : GenUseCase_2143_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2143_ @Inject constructor(
    private val repository: GenRepositoryImpl_2143_
) : GenUseCase_2143_<String, List<GenModel_2143_>> {
    override suspend fun invoke(params: String): List<GenModel_2143_> = repository.search(params)
}

abstract class GenMapper_2143_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2143_ : GenMapper_2143_<GenModel_2143_, String>() {
    override fun map(input: GenModel_2143_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2143_ : GenMapper_2143_<String, GenModel_2143_>() {
    override fun map(input: String): GenModel_2143_ {
        val parts = input.split(":")
        return GenModel_2143_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2143_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2143_,
    private val saveUseCase: GenSaveUseCase_2143_,
    private val deleteUseCase: GenDeleteUseCase_2143_,
    private val searchUseCase: GenSearchUseCase_2143_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2143_>(GenState_2143_.Idle)
    val state: StateFlow<GenState_2143_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2143_) {
        when (event) {
            is GenEvent_2143_.Load -> loadAll()
            is GenEvent_2143_.Update -> save(event.model)
            is GenEvent_2143_.Delete -> delete(event.id)
            is GenEvent_2143_.Refresh -> loadAll()
            is GenEvent_2143_.Search -> search(event.query)
            is GenEvent_2143_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2143_.Loading; _state.value = GenState_2143_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2143_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2143_.Success(searchUseCase(query)) } }
}
