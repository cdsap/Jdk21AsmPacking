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

data class GenModel_810_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_810_ {
    data class Load(val id: Long) : GenEvent_810_()
    data class Update(val model: GenModel_810_) : GenEvent_810_()
    data class Delete(val id: Long) : GenEvent_810_()
    data object Refresh : GenEvent_810_()
    data class Search(val query: String) : GenEvent_810_()
    data class Filter(val predicate: String) : GenEvent_810_()
}

sealed class GenState_810_ {
    data object Idle : GenState_810_()
    data object Loading : GenState_810_()
    data class Success(val items: List<GenModel_810_>) : GenState_810_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_810_()
    data class Partial(val items: List<GenModel_810_>, val hasMore: Boolean) : GenState_810_()
}

interface GenRepository_810_ {
    suspend fun getAll(): List<GenModel_810_>
    suspend fun getById(id: Long): GenModel_810_?
    suspend fun save(model: GenModel_810_): GenModel_810_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_810_>
}

@Singleton
class GenRepositoryImpl_810_ @Inject constructor() : GenRepository_810_ {
    private val store = mutableMapOf<Long, GenModel_810_>()
    override suspend fun getAll(): List<GenModel_810_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_810_? = store[id]
    override suspend fun save(model: GenModel_810_): GenModel_810_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_810_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_810_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_810_ @Inject constructor(
    private val repository: GenRepositoryImpl_810_
) : GenUseCase_810_<Unit, List<GenModel_810_>> {
    override suspend fun invoke(params: Unit): List<GenModel_810_> = repository.getAll()
}

class GenSaveUseCase_810_ @Inject constructor(
    private val repository: GenRepositoryImpl_810_
) : GenUseCase_810_<GenModel_810_, GenModel_810_> {
    override suspend fun invoke(params: GenModel_810_): GenModel_810_ = repository.save(params)
}

class GenDeleteUseCase_810_ @Inject constructor(
    private val repository: GenRepositoryImpl_810_
) : GenUseCase_810_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_810_ @Inject constructor(
    private val repository: GenRepositoryImpl_810_
) : GenUseCase_810_<String, List<GenModel_810_>> {
    override suspend fun invoke(params: String): List<GenModel_810_> = repository.search(params)
}

abstract class GenMapper_810_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_810_ : GenMapper_810_<GenModel_810_, String>() {
    override fun map(input: GenModel_810_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_810_ : GenMapper_810_<String, GenModel_810_>() {
    override fun map(input: String): GenModel_810_ {
        val parts = input.split(":")
        return GenModel_810_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_810_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_810_,
    private val saveUseCase: GenSaveUseCase_810_,
    private val deleteUseCase: GenDeleteUseCase_810_,
    private val searchUseCase: GenSearchUseCase_810_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_810_>(GenState_810_.Idle)
    val state: StateFlow<GenState_810_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_810_) {
        when (event) {
            is GenEvent_810_.Load -> loadAll()
            is GenEvent_810_.Update -> save(event.model)
            is GenEvent_810_.Delete -> delete(event.id)
            is GenEvent_810_.Refresh -> loadAll()
            is GenEvent_810_.Search -> search(event.query)
            is GenEvent_810_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_810_.Loading; _state.value = GenState_810_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_810_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_810_.Success(searchUseCase(query)) } }
}
