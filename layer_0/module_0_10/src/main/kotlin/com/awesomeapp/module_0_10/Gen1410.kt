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

data class GenModel_1410_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1410_ {
    data class Load(val id: Long) : GenEvent_1410_()
    data class Update(val model: GenModel_1410_) : GenEvent_1410_()
    data class Delete(val id: Long) : GenEvent_1410_()
    data object Refresh : GenEvent_1410_()
    data class Search(val query: String) : GenEvent_1410_()
    data class Filter(val predicate: String) : GenEvent_1410_()
}

sealed class GenState_1410_ {
    data object Idle : GenState_1410_()
    data object Loading : GenState_1410_()
    data class Success(val items: List<GenModel_1410_>) : GenState_1410_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1410_()
    data class Partial(val items: List<GenModel_1410_>, val hasMore: Boolean) : GenState_1410_()
}

interface GenRepository_1410_ {
    suspend fun getAll(): List<GenModel_1410_>
    suspend fun getById(id: Long): GenModel_1410_?
    suspend fun save(model: GenModel_1410_): GenModel_1410_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1410_>
}

@Singleton
class GenRepositoryImpl_1410_ @Inject constructor() : GenRepository_1410_ {
    private val store = mutableMapOf<Long, GenModel_1410_>()
    override suspend fun getAll(): List<GenModel_1410_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1410_? = store[id]
    override suspend fun save(model: GenModel_1410_): GenModel_1410_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1410_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1410_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1410_ @Inject constructor(
    private val repository: GenRepositoryImpl_1410_
) : GenUseCase_1410_<Unit, List<GenModel_1410_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1410_> = repository.getAll()
}

class GenSaveUseCase_1410_ @Inject constructor(
    private val repository: GenRepositoryImpl_1410_
) : GenUseCase_1410_<GenModel_1410_, GenModel_1410_> {
    override suspend fun invoke(params: GenModel_1410_): GenModel_1410_ = repository.save(params)
}

class GenDeleteUseCase_1410_ @Inject constructor(
    private val repository: GenRepositoryImpl_1410_
) : GenUseCase_1410_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1410_ @Inject constructor(
    private val repository: GenRepositoryImpl_1410_
) : GenUseCase_1410_<String, List<GenModel_1410_>> {
    override suspend fun invoke(params: String): List<GenModel_1410_> = repository.search(params)
}

abstract class GenMapper_1410_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1410_ : GenMapper_1410_<GenModel_1410_, String>() {
    override fun map(input: GenModel_1410_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1410_ : GenMapper_1410_<String, GenModel_1410_>() {
    override fun map(input: String): GenModel_1410_ {
        val parts = input.split(":")
        return GenModel_1410_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1410_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1410_,
    private val saveUseCase: GenSaveUseCase_1410_,
    private val deleteUseCase: GenDeleteUseCase_1410_,
    private val searchUseCase: GenSearchUseCase_1410_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1410_>(GenState_1410_.Idle)
    val state: StateFlow<GenState_1410_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1410_) {
        when (event) {
            is GenEvent_1410_.Load -> loadAll()
            is GenEvent_1410_.Update -> save(event.model)
            is GenEvent_1410_.Delete -> delete(event.id)
            is GenEvent_1410_.Refresh -> loadAll()
            is GenEvent_1410_.Search -> search(event.query)
            is GenEvent_1410_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1410_.Loading; _state.value = GenState_1410_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1410_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1410_.Success(searchUseCase(query)) } }
}
