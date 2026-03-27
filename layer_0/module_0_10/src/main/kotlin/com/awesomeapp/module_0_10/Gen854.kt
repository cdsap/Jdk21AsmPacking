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

data class GenModel_854_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_854_ {
    data class Load(val id: Long) : GenEvent_854_()
    data class Update(val model: GenModel_854_) : GenEvent_854_()
    data class Delete(val id: Long) : GenEvent_854_()
    data object Refresh : GenEvent_854_()
    data class Search(val query: String) : GenEvent_854_()
    data class Filter(val predicate: String) : GenEvent_854_()
}

sealed class GenState_854_ {
    data object Idle : GenState_854_()
    data object Loading : GenState_854_()
    data class Success(val items: List<GenModel_854_>) : GenState_854_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_854_()
    data class Partial(val items: List<GenModel_854_>, val hasMore: Boolean) : GenState_854_()
}

interface GenRepository_854_ {
    suspend fun getAll(): List<GenModel_854_>
    suspend fun getById(id: Long): GenModel_854_?
    suspend fun save(model: GenModel_854_): GenModel_854_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_854_>
}

@Singleton
class GenRepositoryImpl_854_ @Inject constructor() : GenRepository_854_ {
    private val store = mutableMapOf<Long, GenModel_854_>()
    override suspend fun getAll(): List<GenModel_854_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_854_? = store[id]
    override suspend fun save(model: GenModel_854_): GenModel_854_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_854_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_854_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_854_ @Inject constructor(
    private val repository: GenRepositoryImpl_854_
) : GenUseCase_854_<Unit, List<GenModel_854_>> {
    override suspend fun invoke(params: Unit): List<GenModel_854_> = repository.getAll()
}

class GenSaveUseCase_854_ @Inject constructor(
    private val repository: GenRepositoryImpl_854_
) : GenUseCase_854_<GenModel_854_, GenModel_854_> {
    override suspend fun invoke(params: GenModel_854_): GenModel_854_ = repository.save(params)
}

class GenDeleteUseCase_854_ @Inject constructor(
    private val repository: GenRepositoryImpl_854_
) : GenUseCase_854_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_854_ @Inject constructor(
    private val repository: GenRepositoryImpl_854_
) : GenUseCase_854_<String, List<GenModel_854_>> {
    override suspend fun invoke(params: String): List<GenModel_854_> = repository.search(params)
}

abstract class GenMapper_854_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_854_ : GenMapper_854_<GenModel_854_, String>() {
    override fun map(input: GenModel_854_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_854_ : GenMapper_854_<String, GenModel_854_>() {
    override fun map(input: String): GenModel_854_ {
        val parts = input.split(":")
        return GenModel_854_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_854_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_854_,
    private val saveUseCase: GenSaveUseCase_854_,
    private val deleteUseCase: GenDeleteUseCase_854_,
    private val searchUseCase: GenSearchUseCase_854_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_854_>(GenState_854_.Idle)
    val state: StateFlow<GenState_854_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_854_) {
        when (event) {
            is GenEvent_854_.Load -> loadAll()
            is GenEvent_854_.Update -> save(event.model)
            is GenEvent_854_.Delete -> delete(event.id)
            is GenEvent_854_.Refresh -> loadAll()
            is GenEvent_854_.Search -> search(event.query)
            is GenEvent_854_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_854_.Loading; _state.value = GenState_854_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_854_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_854_.Success(searchUseCase(query)) } }
}
