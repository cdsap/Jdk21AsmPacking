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

data class GenModel_629_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_629_ {
    data class Load(val id: Long) : GenEvent_629_()
    data class Update(val model: GenModel_629_) : GenEvent_629_()
    data class Delete(val id: Long) : GenEvent_629_()
    data object Refresh : GenEvent_629_()
    data class Search(val query: String) : GenEvent_629_()
    data class Filter(val predicate: String) : GenEvent_629_()
}

sealed class GenState_629_ {
    data object Idle : GenState_629_()
    data object Loading : GenState_629_()
    data class Success(val items: List<GenModel_629_>) : GenState_629_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_629_()
    data class Partial(val items: List<GenModel_629_>, val hasMore: Boolean) : GenState_629_()
}

interface GenRepository_629_ {
    suspend fun getAll(): List<GenModel_629_>
    suspend fun getById(id: Long): GenModel_629_?
    suspend fun save(model: GenModel_629_): GenModel_629_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_629_>
}

@Singleton
class GenRepositoryImpl_629_ @Inject constructor() : GenRepository_629_ {
    private val store = mutableMapOf<Long, GenModel_629_>()
    override suspend fun getAll(): List<GenModel_629_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_629_? = store[id]
    override suspend fun save(model: GenModel_629_): GenModel_629_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_629_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_629_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_629_ @Inject constructor(
    private val repository: GenRepositoryImpl_629_
) : GenUseCase_629_<Unit, List<GenModel_629_>> {
    override suspend fun invoke(params: Unit): List<GenModel_629_> = repository.getAll()
}

class GenSaveUseCase_629_ @Inject constructor(
    private val repository: GenRepositoryImpl_629_
) : GenUseCase_629_<GenModel_629_, GenModel_629_> {
    override suspend fun invoke(params: GenModel_629_): GenModel_629_ = repository.save(params)
}

class GenDeleteUseCase_629_ @Inject constructor(
    private val repository: GenRepositoryImpl_629_
) : GenUseCase_629_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_629_ @Inject constructor(
    private val repository: GenRepositoryImpl_629_
) : GenUseCase_629_<String, List<GenModel_629_>> {
    override suspend fun invoke(params: String): List<GenModel_629_> = repository.search(params)
}

abstract class GenMapper_629_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_629_ : GenMapper_629_<GenModel_629_, String>() {
    override fun map(input: GenModel_629_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_629_ : GenMapper_629_<String, GenModel_629_>() {
    override fun map(input: String): GenModel_629_ {
        val parts = input.split(":")
        return GenModel_629_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_629_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_629_,
    private val saveUseCase: GenSaveUseCase_629_,
    private val deleteUseCase: GenDeleteUseCase_629_,
    private val searchUseCase: GenSearchUseCase_629_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_629_>(GenState_629_.Idle)
    val state: StateFlow<GenState_629_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_629_) {
        when (event) {
            is GenEvent_629_.Load -> loadAll()
            is GenEvent_629_.Update -> save(event.model)
            is GenEvent_629_.Delete -> delete(event.id)
            is GenEvent_629_.Refresh -> loadAll()
            is GenEvent_629_.Search -> search(event.query)
            is GenEvent_629_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_629_.Loading; _state.value = GenState_629_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_629_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_629_.Success(searchUseCase(query)) } }
}
