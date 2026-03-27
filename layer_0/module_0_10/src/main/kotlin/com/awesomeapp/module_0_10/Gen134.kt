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

data class GenModel_134_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_134_ {
    data class Load(val id: Long) : GenEvent_134_()
    data class Update(val model: GenModel_134_) : GenEvent_134_()
    data class Delete(val id: Long) : GenEvent_134_()
    data object Refresh : GenEvent_134_()
    data class Search(val query: String) : GenEvent_134_()
    data class Filter(val predicate: String) : GenEvent_134_()
}

sealed class GenState_134_ {
    data object Idle : GenState_134_()
    data object Loading : GenState_134_()
    data class Success(val items: List<GenModel_134_>) : GenState_134_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_134_()
    data class Partial(val items: List<GenModel_134_>, val hasMore: Boolean) : GenState_134_()
}

interface GenRepository_134_ {
    suspend fun getAll(): List<GenModel_134_>
    suspend fun getById(id: Long): GenModel_134_?
    suspend fun save(model: GenModel_134_): GenModel_134_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_134_>
}

@Singleton
class GenRepositoryImpl_134_ @Inject constructor() : GenRepository_134_ {
    private val store = mutableMapOf<Long, GenModel_134_>()
    override suspend fun getAll(): List<GenModel_134_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_134_? = store[id]
    override suspend fun save(model: GenModel_134_): GenModel_134_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_134_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_134_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_134_ @Inject constructor(
    private val repository: GenRepositoryImpl_134_
) : GenUseCase_134_<Unit, List<GenModel_134_>> {
    override suspend fun invoke(params: Unit): List<GenModel_134_> = repository.getAll()
}

class GenSaveUseCase_134_ @Inject constructor(
    private val repository: GenRepositoryImpl_134_
) : GenUseCase_134_<GenModel_134_, GenModel_134_> {
    override suspend fun invoke(params: GenModel_134_): GenModel_134_ = repository.save(params)
}

class GenDeleteUseCase_134_ @Inject constructor(
    private val repository: GenRepositoryImpl_134_
) : GenUseCase_134_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_134_ @Inject constructor(
    private val repository: GenRepositoryImpl_134_
) : GenUseCase_134_<String, List<GenModel_134_>> {
    override suspend fun invoke(params: String): List<GenModel_134_> = repository.search(params)
}

abstract class GenMapper_134_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_134_ : GenMapper_134_<GenModel_134_, String>() {
    override fun map(input: GenModel_134_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_134_ : GenMapper_134_<String, GenModel_134_>() {
    override fun map(input: String): GenModel_134_ {
        val parts = input.split(":")
        return GenModel_134_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_134_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_134_,
    private val saveUseCase: GenSaveUseCase_134_,
    private val deleteUseCase: GenDeleteUseCase_134_,
    private val searchUseCase: GenSearchUseCase_134_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_134_>(GenState_134_.Idle)
    val state: StateFlow<GenState_134_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_134_) {
        when (event) {
            is GenEvent_134_.Load -> loadAll()
            is GenEvent_134_.Update -> save(event.model)
            is GenEvent_134_.Delete -> delete(event.id)
            is GenEvent_134_.Refresh -> loadAll()
            is GenEvent_134_.Search -> search(event.query)
            is GenEvent_134_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_134_.Loading; _state.value = GenState_134_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_134_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_134_.Success(searchUseCase(query)) } }
}
