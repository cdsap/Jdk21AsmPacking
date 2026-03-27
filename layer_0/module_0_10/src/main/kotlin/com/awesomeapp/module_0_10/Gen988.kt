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

data class GenModel_988_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_988_ {
    data class Load(val id: Long) : GenEvent_988_()
    data class Update(val model: GenModel_988_) : GenEvent_988_()
    data class Delete(val id: Long) : GenEvent_988_()
    data object Refresh : GenEvent_988_()
    data class Search(val query: String) : GenEvent_988_()
    data class Filter(val predicate: String) : GenEvent_988_()
}

sealed class GenState_988_ {
    data object Idle : GenState_988_()
    data object Loading : GenState_988_()
    data class Success(val items: List<GenModel_988_>) : GenState_988_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_988_()
    data class Partial(val items: List<GenModel_988_>, val hasMore: Boolean) : GenState_988_()
}

interface GenRepository_988_ {
    suspend fun getAll(): List<GenModel_988_>
    suspend fun getById(id: Long): GenModel_988_?
    suspend fun save(model: GenModel_988_): GenModel_988_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_988_>
}

@Singleton
class GenRepositoryImpl_988_ @Inject constructor() : GenRepository_988_ {
    private val store = mutableMapOf<Long, GenModel_988_>()
    override suspend fun getAll(): List<GenModel_988_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_988_? = store[id]
    override suspend fun save(model: GenModel_988_): GenModel_988_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_988_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_988_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_988_ @Inject constructor(
    private val repository: GenRepositoryImpl_988_
) : GenUseCase_988_<Unit, List<GenModel_988_>> {
    override suspend fun invoke(params: Unit): List<GenModel_988_> = repository.getAll()
}

class GenSaveUseCase_988_ @Inject constructor(
    private val repository: GenRepositoryImpl_988_
) : GenUseCase_988_<GenModel_988_, GenModel_988_> {
    override suspend fun invoke(params: GenModel_988_): GenModel_988_ = repository.save(params)
}

class GenDeleteUseCase_988_ @Inject constructor(
    private val repository: GenRepositoryImpl_988_
) : GenUseCase_988_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_988_ @Inject constructor(
    private val repository: GenRepositoryImpl_988_
) : GenUseCase_988_<String, List<GenModel_988_>> {
    override suspend fun invoke(params: String): List<GenModel_988_> = repository.search(params)
}

abstract class GenMapper_988_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_988_ : GenMapper_988_<GenModel_988_, String>() {
    override fun map(input: GenModel_988_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_988_ : GenMapper_988_<String, GenModel_988_>() {
    override fun map(input: String): GenModel_988_ {
        val parts = input.split(":")
        return GenModel_988_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_988_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_988_,
    private val saveUseCase: GenSaveUseCase_988_,
    private val deleteUseCase: GenDeleteUseCase_988_,
    private val searchUseCase: GenSearchUseCase_988_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_988_>(GenState_988_.Idle)
    val state: StateFlow<GenState_988_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_988_) {
        when (event) {
            is GenEvent_988_.Load -> loadAll()
            is GenEvent_988_.Update -> save(event.model)
            is GenEvent_988_.Delete -> delete(event.id)
            is GenEvent_988_.Refresh -> loadAll()
            is GenEvent_988_.Search -> search(event.query)
            is GenEvent_988_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_988_.Loading; _state.value = GenState_988_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_988_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_988_.Success(searchUseCase(query)) } }
}
