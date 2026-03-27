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

data class GenModel_501_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_501_ {
    data class Load(val id: Long) : GenEvent_501_()
    data class Update(val model: GenModel_501_) : GenEvent_501_()
    data class Delete(val id: Long) : GenEvent_501_()
    data object Refresh : GenEvent_501_()
    data class Search(val query: String) : GenEvent_501_()
    data class Filter(val predicate: String) : GenEvent_501_()
}

sealed class GenState_501_ {
    data object Idle : GenState_501_()
    data object Loading : GenState_501_()
    data class Success(val items: List<GenModel_501_>) : GenState_501_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_501_()
    data class Partial(val items: List<GenModel_501_>, val hasMore: Boolean) : GenState_501_()
}

interface GenRepository_501_ {
    suspend fun getAll(): List<GenModel_501_>
    suspend fun getById(id: Long): GenModel_501_?
    suspend fun save(model: GenModel_501_): GenModel_501_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_501_>
}

@Singleton
class GenRepositoryImpl_501_ @Inject constructor() : GenRepository_501_ {
    private val store = mutableMapOf<Long, GenModel_501_>()
    override suspend fun getAll(): List<GenModel_501_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_501_? = store[id]
    override suspend fun save(model: GenModel_501_): GenModel_501_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_501_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_501_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_501_ @Inject constructor(
    private val repository: GenRepositoryImpl_501_
) : GenUseCase_501_<Unit, List<GenModel_501_>> {
    override suspend fun invoke(params: Unit): List<GenModel_501_> = repository.getAll()
}

class GenSaveUseCase_501_ @Inject constructor(
    private val repository: GenRepositoryImpl_501_
) : GenUseCase_501_<GenModel_501_, GenModel_501_> {
    override suspend fun invoke(params: GenModel_501_): GenModel_501_ = repository.save(params)
}

class GenDeleteUseCase_501_ @Inject constructor(
    private val repository: GenRepositoryImpl_501_
) : GenUseCase_501_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_501_ @Inject constructor(
    private val repository: GenRepositoryImpl_501_
) : GenUseCase_501_<String, List<GenModel_501_>> {
    override suspend fun invoke(params: String): List<GenModel_501_> = repository.search(params)
}

abstract class GenMapper_501_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_501_ : GenMapper_501_<GenModel_501_, String>() {
    override fun map(input: GenModel_501_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_501_ : GenMapper_501_<String, GenModel_501_>() {
    override fun map(input: String): GenModel_501_ {
        val parts = input.split(":")
        return GenModel_501_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_501_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_501_,
    private val saveUseCase: GenSaveUseCase_501_,
    private val deleteUseCase: GenDeleteUseCase_501_,
    private val searchUseCase: GenSearchUseCase_501_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_501_>(GenState_501_.Idle)
    val state: StateFlow<GenState_501_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_501_) {
        when (event) {
            is GenEvent_501_.Load -> loadAll()
            is GenEvent_501_.Update -> save(event.model)
            is GenEvent_501_.Delete -> delete(event.id)
            is GenEvent_501_.Refresh -> loadAll()
            is GenEvent_501_.Search -> search(event.query)
            is GenEvent_501_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_501_.Loading; _state.value = GenState_501_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_501_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_501_.Success(searchUseCase(query)) } }
}
