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

data class GenModel_316_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_316_ {
    data class Load(val id: Long) : GenEvent_316_()
    data class Update(val model: GenModel_316_) : GenEvent_316_()
    data class Delete(val id: Long) : GenEvent_316_()
    data object Refresh : GenEvent_316_()
    data class Search(val query: String) : GenEvent_316_()
    data class Filter(val predicate: String) : GenEvent_316_()
}

sealed class GenState_316_ {
    data object Idle : GenState_316_()
    data object Loading : GenState_316_()
    data class Success(val items: List<GenModel_316_>) : GenState_316_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_316_()
    data class Partial(val items: List<GenModel_316_>, val hasMore: Boolean) : GenState_316_()
}

interface GenRepository_316_ {
    suspend fun getAll(): List<GenModel_316_>
    suspend fun getById(id: Long): GenModel_316_?
    suspend fun save(model: GenModel_316_): GenModel_316_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_316_>
}

@Singleton
class GenRepositoryImpl_316_ @Inject constructor() : GenRepository_316_ {
    private val store = mutableMapOf<Long, GenModel_316_>()
    override suspend fun getAll(): List<GenModel_316_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_316_? = store[id]
    override suspend fun save(model: GenModel_316_): GenModel_316_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_316_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_316_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_316_ @Inject constructor(
    private val repository: GenRepositoryImpl_316_
) : GenUseCase_316_<Unit, List<GenModel_316_>> {
    override suspend fun invoke(params: Unit): List<GenModel_316_> = repository.getAll()
}

class GenSaveUseCase_316_ @Inject constructor(
    private val repository: GenRepositoryImpl_316_
) : GenUseCase_316_<GenModel_316_, GenModel_316_> {
    override suspend fun invoke(params: GenModel_316_): GenModel_316_ = repository.save(params)
}

class GenDeleteUseCase_316_ @Inject constructor(
    private val repository: GenRepositoryImpl_316_
) : GenUseCase_316_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_316_ @Inject constructor(
    private val repository: GenRepositoryImpl_316_
) : GenUseCase_316_<String, List<GenModel_316_>> {
    override suspend fun invoke(params: String): List<GenModel_316_> = repository.search(params)
}

abstract class GenMapper_316_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_316_ : GenMapper_316_<GenModel_316_, String>() {
    override fun map(input: GenModel_316_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_316_ : GenMapper_316_<String, GenModel_316_>() {
    override fun map(input: String): GenModel_316_ {
        val parts = input.split(":")
        return GenModel_316_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_316_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_316_,
    private val saveUseCase: GenSaveUseCase_316_,
    private val deleteUseCase: GenDeleteUseCase_316_,
    private val searchUseCase: GenSearchUseCase_316_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_316_>(GenState_316_.Idle)
    val state: StateFlow<GenState_316_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_316_) {
        when (event) {
            is GenEvent_316_.Load -> loadAll()
            is GenEvent_316_.Update -> save(event.model)
            is GenEvent_316_.Delete -> delete(event.id)
            is GenEvent_316_.Refresh -> loadAll()
            is GenEvent_316_.Search -> search(event.query)
            is GenEvent_316_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_316_.Loading; _state.value = GenState_316_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_316_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_316_.Success(searchUseCase(query)) } }
}
