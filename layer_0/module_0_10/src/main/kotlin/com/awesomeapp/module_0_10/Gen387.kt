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

data class GenModel_387_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_387_ {
    data class Load(val id: Long) : GenEvent_387_()
    data class Update(val model: GenModel_387_) : GenEvent_387_()
    data class Delete(val id: Long) : GenEvent_387_()
    data object Refresh : GenEvent_387_()
    data class Search(val query: String) : GenEvent_387_()
    data class Filter(val predicate: String) : GenEvent_387_()
}

sealed class GenState_387_ {
    data object Idle : GenState_387_()
    data object Loading : GenState_387_()
    data class Success(val items: List<GenModel_387_>) : GenState_387_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_387_()
    data class Partial(val items: List<GenModel_387_>, val hasMore: Boolean) : GenState_387_()
}

interface GenRepository_387_ {
    suspend fun getAll(): List<GenModel_387_>
    suspend fun getById(id: Long): GenModel_387_?
    suspend fun save(model: GenModel_387_): GenModel_387_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_387_>
}

@Singleton
class GenRepositoryImpl_387_ @Inject constructor() : GenRepository_387_ {
    private val store = mutableMapOf<Long, GenModel_387_>()
    override suspend fun getAll(): List<GenModel_387_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_387_? = store[id]
    override suspend fun save(model: GenModel_387_): GenModel_387_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_387_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_387_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_387_ @Inject constructor(
    private val repository: GenRepositoryImpl_387_
) : GenUseCase_387_<Unit, List<GenModel_387_>> {
    override suspend fun invoke(params: Unit): List<GenModel_387_> = repository.getAll()
}

class GenSaveUseCase_387_ @Inject constructor(
    private val repository: GenRepositoryImpl_387_
) : GenUseCase_387_<GenModel_387_, GenModel_387_> {
    override suspend fun invoke(params: GenModel_387_): GenModel_387_ = repository.save(params)
}

class GenDeleteUseCase_387_ @Inject constructor(
    private val repository: GenRepositoryImpl_387_
) : GenUseCase_387_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_387_ @Inject constructor(
    private val repository: GenRepositoryImpl_387_
) : GenUseCase_387_<String, List<GenModel_387_>> {
    override suspend fun invoke(params: String): List<GenModel_387_> = repository.search(params)
}

abstract class GenMapper_387_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_387_ : GenMapper_387_<GenModel_387_, String>() {
    override fun map(input: GenModel_387_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_387_ : GenMapper_387_<String, GenModel_387_>() {
    override fun map(input: String): GenModel_387_ {
        val parts = input.split(":")
        return GenModel_387_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_387_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_387_,
    private val saveUseCase: GenSaveUseCase_387_,
    private val deleteUseCase: GenDeleteUseCase_387_,
    private val searchUseCase: GenSearchUseCase_387_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_387_>(GenState_387_.Idle)
    val state: StateFlow<GenState_387_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_387_) {
        when (event) {
            is GenEvent_387_.Load -> loadAll()
            is GenEvent_387_.Update -> save(event.model)
            is GenEvent_387_.Delete -> delete(event.id)
            is GenEvent_387_.Refresh -> loadAll()
            is GenEvent_387_.Search -> search(event.query)
            is GenEvent_387_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_387_.Loading; _state.value = GenState_387_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_387_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_387_.Success(searchUseCase(query)) } }
}
