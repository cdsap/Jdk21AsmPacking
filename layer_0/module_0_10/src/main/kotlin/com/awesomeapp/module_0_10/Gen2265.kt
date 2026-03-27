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

data class GenModel_2265_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2265_ {
    data class Load(val id: Long) : GenEvent_2265_()
    data class Update(val model: GenModel_2265_) : GenEvent_2265_()
    data class Delete(val id: Long) : GenEvent_2265_()
    data object Refresh : GenEvent_2265_()
    data class Search(val query: String) : GenEvent_2265_()
    data class Filter(val predicate: String) : GenEvent_2265_()
}

sealed class GenState_2265_ {
    data object Idle : GenState_2265_()
    data object Loading : GenState_2265_()
    data class Success(val items: List<GenModel_2265_>) : GenState_2265_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2265_()
    data class Partial(val items: List<GenModel_2265_>, val hasMore: Boolean) : GenState_2265_()
}

interface GenRepository_2265_ {
    suspend fun getAll(): List<GenModel_2265_>
    suspend fun getById(id: Long): GenModel_2265_?
    suspend fun save(model: GenModel_2265_): GenModel_2265_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2265_>
}

@Singleton
class GenRepositoryImpl_2265_ @Inject constructor() : GenRepository_2265_ {
    private val store = mutableMapOf<Long, GenModel_2265_>()
    override suspend fun getAll(): List<GenModel_2265_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2265_? = store[id]
    override suspend fun save(model: GenModel_2265_): GenModel_2265_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2265_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2265_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2265_ @Inject constructor(
    private val repository: GenRepositoryImpl_2265_
) : GenUseCase_2265_<Unit, List<GenModel_2265_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2265_> = repository.getAll()
}

class GenSaveUseCase_2265_ @Inject constructor(
    private val repository: GenRepositoryImpl_2265_
) : GenUseCase_2265_<GenModel_2265_, GenModel_2265_> {
    override suspend fun invoke(params: GenModel_2265_): GenModel_2265_ = repository.save(params)
}

class GenDeleteUseCase_2265_ @Inject constructor(
    private val repository: GenRepositoryImpl_2265_
) : GenUseCase_2265_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2265_ @Inject constructor(
    private val repository: GenRepositoryImpl_2265_
) : GenUseCase_2265_<String, List<GenModel_2265_>> {
    override suspend fun invoke(params: String): List<GenModel_2265_> = repository.search(params)
}

abstract class GenMapper_2265_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2265_ : GenMapper_2265_<GenModel_2265_, String>() {
    override fun map(input: GenModel_2265_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2265_ : GenMapper_2265_<String, GenModel_2265_>() {
    override fun map(input: String): GenModel_2265_ {
        val parts = input.split(":")
        return GenModel_2265_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2265_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2265_,
    private val saveUseCase: GenSaveUseCase_2265_,
    private val deleteUseCase: GenDeleteUseCase_2265_,
    private val searchUseCase: GenSearchUseCase_2265_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2265_>(GenState_2265_.Idle)
    val state: StateFlow<GenState_2265_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2265_) {
        when (event) {
            is GenEvent_2265_.Load -> loadAll()
            is GenEvent_2265_.Update -> save(event.model)
            is GenEvent_2265_.Delete -> delete(event.id)
            is GenEvent_2265_.Refresh -> loadAll()
            is GenEvent_2265_.Search -> search(event.query)
            is GenEvent_2265_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2265_.Loading; _state.value = GenState_2265_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2265_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2265_.Success(searchUseCase(query)) } }
}
