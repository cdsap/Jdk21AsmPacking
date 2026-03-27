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

data class GenModel_551_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_551_ {
    data class Load(val id: Long) : GenEvent_551_()
    data class Update(val model: GenModel_551_) : GenEvent_551_()
    data class Delete(val id: Long) : GenEvent_551_()
    data object Refresh : GenEvent_551_()
    data class Search(val query: String) : GenEvent_551_()
    data class Filter(val predicate: String) : GenEvent_551_()
}

sealed class GenState_551_ {
    data object Idle : GenState_551_()
    data object Loading : GenState_551_()
    data class Success(val items: List<GenModel_551_>) : GenState_551_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_551_()
    data class Partial(val items: List<GenModel_551_>, val hasMore: Boolean) : GenState_551_()
}

interface GenRepository_551_ {
    suspend fun getAll(): List<GenModel_551_>
    suspend fun getById(id: Long): GenModel_551_?
    suspend fun save(model: GenModel_551_): GenModel_551_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_551_>
}

@Singleton
class GenRepositoryImpl_551_ @Inject constructor() : GenRepository_551_ {
    private val store = mutableMapOf<Long, GenModel_551_>()
    override suspend fun getAll(): List<GenModel_551_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_551_? = store[id]
    override suspend fun save(model: GenModel_551_): GenModel_551_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_551_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_551_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_551_ @Inject constructor(
    private val repository: GenRepositoryImpl_551_
) : GenUseCase_551_<Unit, List<GenModel_551_>> {
    override suspend fun invoke(params: Unit): List<GenModel_551_> = repository.getAll()
}

class GenSaveUseCase_551_ @Inject constructor(
    private val repository: GenRepositoryImpl_551_
) : GenUseCase_551_<GenModel_551_, GenModel_551_> {
    override suspend fun invoke(params: GenModel_551_): GenModel_551_ = repository.save(params)
}

class GenDeleteUseCase_551_ @Inject constructor(
    private val repository: GenRepositoryImpl_551_
) : GenUseCase_551_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_551_ @Inject constructor(
    private val repository: GenRepositoryImpl_551_
) : GenUseCase_551_<String, List<GenModel_551_>> {
    override suspend fun invoke(params: String): List<GenModel_551_> = repository.search(params)
}

abstract class GenMapper_551_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_551_ : GenMapper_551_<GenModel_551_, String>() {
    override fun map(input: GenModel_551_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_551_ : GenMapper_551_<String, GenModel_551_>() {
    override fun map(input: String): GenModel_551_ {
        val parts = input.split(":")
        return GenModel_551_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_551_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_551_,
    private val saveUseCase: GenSaveUseCase_551_,
    private val deleteUseCase: GenDeleteUseCase_551_,
    private val searchUseCase: GenSearchUseCase_551_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_551_>(GenState_551_.Idle)
    val state: StateFlow<GenState_551_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_551_) {
        when (event) {
            is GenEvent_551_.Load -> loadAll()
            is GenEvent_551_.Update -> save(event.model)
            is GenEvent_551_.Delete -> delete(event.id)
            is GenEvent_551_.Refresh -> loadAll()
            is GenEvent_551_.Search -> search(event.query)
            is GenEvent_551_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_551_.Loading; _state.value = GenState_551_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_551_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_551_.Success(searchUseCase(query)) } }
}
