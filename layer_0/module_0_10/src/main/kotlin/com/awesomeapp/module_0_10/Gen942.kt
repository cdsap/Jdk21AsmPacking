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

data class GenModel_942_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_942_ {
    data class Load(val id: Long) : GenEvent_942_()
    data class Update(val model: GenModel_942_) : GenEvent_942_()
    data class Delete(val id: Long) : GenEvent_942_()
    data object Refresh : GenEvent_942_()
    data class Search(val query: String) : GenEvent_942_()
    data class Filter(val predicate: String) : GenEvent_942_()
}

sealed class GenState_942_ {
    data object Idle : GenState_942_()
    data object Loading : GenState_942_()
    data class Success(val items: List<GenModel_942_>) : GenState_942_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_942_()
    data class Partial(val items: List<GenModel_942_>, val hasMore: Boolean) : GenState_942_()
}

interface GenRepository_942_ {
    suspend fun getAll(): List<GenModel_942_>
    suspend fun getById(id: Long): GenModel_942_?
    suspend fun save(model: GenModel_942_): GenModel_942_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_942_>
}

@Singleton
class GenRepositoryImpl_942_ @Inject constructor() : GenRepository_942_ {
    private val store = mutableMapOf<Long, GenModel_942_>()
    override suspend fun getAll(): List<GenModel_942_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_942_? = store[id]
    override suspend fun save(model: GenModel_942_): GenModel_942_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_942_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_942_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_942_ @Inject constructor(
    private val repository: GenRepositoryImpl_942_
) : GenUseCase_942_<Unit, List<GenModel_942_>> {
    override suspend fun invoke(params: Unit): List<GenModel_942_> = repository.getAll()
}

class GenSaveUseCase_942_ @Inject constructor(
    private val repository: GenRepositoryImpl_942_
) : GenUseCase_942_<GenModel_942_, GenModel_942_> {
    override suspend fun invoke(params: GenModel_942_): GenModel_942_ = repository.save(params)
}

class GenDeleteUseCase_942_ @Inject constructor(
    private val repository: GenRepositoryImpl_942_
) : GenUseCase_942_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_942_ @Inject constructor(
    private val repository: GenRepositoryImpl_942_
) : GenUseCase_942_<String, List<GenModel_942_>> {
    override suspend fun invoke(params: String): List<GenModel_942_> = repository.search(params)
}

abstract class GenMapper_942_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_942_ : GenMapper_942_<GenModel_942_, String>() {
    override fun map(input: GenModel_942_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_942_ : GenMapper_942_<String, GenModel_942_>() {
    override fun map(input: String): GenModel_942_ {
        val parts = input.split(":")
        return GenModel_942_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_942_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_942_,
    private val saveUseCase: GenSaveUseCase_942_,
    private val deleteUseCase: GenDeleteUseCase_942_,
    private val searchUseCase: GenSearchUseCase_942_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_942_>(GenState_942_.Idle)
    val state: StateFlow<GenState_942_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_942_) {
        when (event) {
            is GenEvent_942_.Load -> loadAll()
            is GenEvent_942_.Update -> save(event.model)
            is GenEvent_942_.Delete -> delete(event.id)
            is GenEvent_942_.Refresh -> loadAll()
            is GenEvent_942_.Search -> search(event.query)
            is GenEvent_942_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_942_.Loading; _state.value = GenState_942_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_942_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_942_.Success(searchUseCase(query)) } }
}
