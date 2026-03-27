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

data class GenModel_759_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_759_ {
    data class Load(val id: Long) : GenEvent_759_()
    data class Update(val model: GenModel_759_) : GenEvent_759_()
    data class Delete(val id: Long) : GenEvent_759_()
    data object Refresh : GenEvent_759_()
    data class Search(val query: String) : GenEvent_759_()
    data class Filter(val predicate: String) : GenEvent_759_()
}

sealed class GenState_759_ {
    data object Idle : GenState_759_()
    data object Loading : GenState_759_()
    data class Success(val items: List<GenModel_759_>) : GenState_759_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_759_()
    data class Partial(val items: List<GenModel_759_>, val hasMore: Boolean) : GenState_759_()
}

interface GenRepository_759_ {
    suspend fun getAll(): List<GenModel_759_>
    suspend fun getById(id: Long): GenModel_759_?
    suspend fun save(model: GenModel_759_): GenModel_759_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_759_>
}

@Singleton
class GenRepositoryImpl_759_ @Inject constructor() : GenRepository_759_ {
    private val store = mutableMapOf<Long, GenModel_759_>()
    override suspend fun getAll(): List<GenModel_759_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_759_? = store[id]
    override suspend fun save(model: GenModel_759_): GenModel_759_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_759_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_759_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_759_ @Inject constructor(
    private val repository: GenRepositoryImpl_759_
) : GenUseCase_759_<Unit, List<GenModel_759_>> {
    override suspend fun invoke(params: Unit): List<GenModel_759_> = repository.getAll()
}

class GenSaveUseCase_759_ @Inject constructor(
    private val repository: GenRepositoryImpl_759_
) : GenUseCase_759_<GenModel_759_, GenModel_759_> {
    override suspend fun invoke(params: GenModel_759_): GenModel_759_ = repository.save(params)
}

class GenDeleteUseCase_759_ @Inject constructor(
    private val repository: GenRepositoryImpl_759_
) : GenUseCase_759_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_759_ @Inject constructor(
    private val repository: GenRepositoryImpl_759_
) : GenUseCase_759_<String, List<GenModel_759_>> {
    override suspend fun invoke(params: String): List<GenModel_759_> = repository.search(params)
}

abstract class GenMapper_759_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_759_ : GenMapper_759_<GenModel_759_, String>() {
    override fun map(input: GenModel_759_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_759_ : GenMapper_759_<String, GenModel_759_>() {
    override fun map(input: String): GenModel_759_ {
        val parts = input.split(":")
        return GenModel_759_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_759_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_759_,
    private val saveUseCase: GenSaveUseCase_759_,
    private val deleteUseCase: GenDeleteUseCase_759_,
    private val searchUseCase: GenSearchUseCase_759_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_759_>(GenState_759_.Idle)
    val state: StateFlow<GenState_759_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_759_) {
        when (event) {
            is GenEvent_759_.Load -> loadAll()
            is GenEvent_759_.Update -> save(event.model)
            is GenEvent_759_.Delete -> delete(event.id)
            is GenEvent_759_.Refresh -> loadAll()
            is GenEvent_759_.Search -> search(event.query)
            is GenEvent_759_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_759_.Loading; _state.value = GenState_759_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_759_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_759_.Success(searchUseCase(query)) } }
}
