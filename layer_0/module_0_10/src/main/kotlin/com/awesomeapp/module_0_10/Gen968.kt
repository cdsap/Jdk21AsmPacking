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

data class GenModel_968_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_968_ {
    data class Load(val id: Long) : GenEvent_968_()
    data class Update(val model: GenModel_968_) : GenEvent_968_()
    data class Delete(val id: Long) : GenEvent_968_()
    data object Refresh : GenEvent_968_()
    data class Search(val query: String) : GenEvent_968_()
    data class Filter(val predicate: String) : GenEvent_968_()
}

sealed class GenState_968_ {
    data object Idle : GenState_968_()
    data object Loading : GenState_968_()
    data class Success(val items: List<GenModel_968_>) : GenState_968_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_968_()
    data class Partial(val items: List<GenModel_968_>, val hasMore: Boolean) : GenState_968_()
}

interface GenRepository_968_ {
    suspend fun getAll(): List<GenModel_968_>
    suspend fun getById(id: Long): GenModel_968_?
    suspend fun save(model: GenModel_968_): GenModel_968_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_968_>
}

@Singleton
class GenRepositoryImpl_968_ @Inject constructor() : GenRepository_968_ {
    private val store = mutableMapOf<Long, GenModel_968_>()
    override suspend fun getAll(): List<GenModel_968_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_968_? = store[id]
    override suspend fun save(model: GenModel_968_): GenModel_968_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_968_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_968_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_968_ @Inject constructor(
    private val repository: GenRepositoryImpl_968_
) : GenUseCase_968_<Unit, List<GenModel_968_>> {
    override suspend fun invoke(params: Unit): List<GenModel_968_> = repository.getAll()
}

class GenSaveUseCase_968_ @Inject constructor(
    private val repository: GenRepositoryImpl_968_
) : GenUseCase_968_<GenModel_968_, GenModel_968_> {
    override suspend fun invoke(params: GenModel_968_): GenModel_968_ = repository.save(params)
}

class GenDeleteUseCase_968_ @Inject constructor(
    private val repository: GenRepositoryImpl_968_
) : GenUseCase_968_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_968_ @Inject constructor(
    private val repository: GenRepositoryImpl_968_
) : GenUseCase_968_<String, List<GenModel_968_>> {
    override suspend fun invoke(params: String): List<GenModel_968_> = repository.search(params)
}

abstract class GenMapper_968_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_968_ : GenMapper_968_<GenModel_968_, String>() {
    override fun map(input: GenModel_968_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_968_ : GenMapper_968_<String, GenModel_968_>() {
    override fun map(input: String): GenModel_968_ {
        val parts = input.split(":")
        return GenModel_968_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_968_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_968_,
    private val saveUseCase: GenSaveUseCase_968_,
    private val deleteUseCase: GenDeleteUseCase_968_,
    private val searchUseCase: GenSearchUseCase_968_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_968_>(GenState_968_.Idle)
    val state: StateFlow<GenState_968_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_968_) {
        when (event) {
            is GenEvent_968_.Load -> loadAll()
            is GenEvent_968_.Update -> save(event.model)
            is GenEvent_968_.Delete -> delete(event.id)
            is GenEvent_968_.Refresh -> loadAll()
            is GenEvent_968_.Search -> search(event.query)
            is GenEvent_968_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_968_.Loading; _state.value = GenState_968_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_968_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_968_.Success(searchUseCase(query)) } }
}
