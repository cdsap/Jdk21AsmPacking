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

data class GenModel_2551_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2551_ {
    data class Load(val id: Long) : GenEvent_2551_()
    data class Update(val model: GenModel_2551_) : GenEvent_2551_()
    data class Delete(val id: Long) : GenEvent_2551_()
    data object Refresh : GenEvent_2551_()
    data class Search(val query: String) : GenEvent_2551_()
    data class Filter(val predicate: String) : GenEvent_2551_()
}

sealed class GenState_2551_ {
    data object Idle : GenState_2551_()
    data object Loading : GenState_2551_()
    data class Success(val items: List<GenModel_2551_>) : GenState_2551_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2551_()
    data class Partial(val items: List<GenModel_2551_>, val hasMore: Boolean) : GenState_2551_()
}

interface GenRepository_2551_ {
    suspend fun getAll(): List<GenModel_2551_>
    suspend fun getById(id: Long): GenModel_2551_?
    suspend fun save(model: GenModel_2551_): GenModel_2551_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2551_>
}

@Singleton
class GenRepositoryImpl_2551_ @Inject constructor() : GenRepository_2551_ {
    private val store = mutableMapOf<Long, GenModel_2551_>()
    override suspend fun getAll(): List<GenModel_2551_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2551_? = store[id]
    override suspend fun save(model: GenModel_2551_): GenModel_2551_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2551_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2551_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2551_ @Inject constructor(
    private val repository: GenRepositoryImpl_2551_
) : GenUseCase_2551_<Unit, List<GenModel_2551_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2551_> = repository.getAll()
}

class GenSaveUseCase_2551_ @Inject constructor(
    private val repository: GenRepositoryImpl_2551_
) : GenUseCase_2551_<GenModel_2551_, GenModel_2551_> {
    override suspend fun invoke(params: GenModel_2551_): GenModel_2551_ = repository.save(params)
}

class GenDeleteUseCase_2551_ @Inject constructor(
    private val repository: GenRepositoryImpl_2551_
) : GenUseCase_2551_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2551_ @Inject constructor(
    private val repository: GenRepositoryImpl_2551_
) : GenUseCase_2551_<String, List<GenModel_2551_>> {
    override suspend fun invoke(params: String): List<GenModel_2551_> = repository.search(params)
}

abstract class GenMapper_2551_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2551_ : GenMapper_2551_<GenModel_2551_, String>() {
    override fun map(input: GenModel_2551_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2551_ : GenMapper_2551_<String, GenModel_2551_>() {
    override fun map(input: String): GenModel_2551_ {
        val parts = input.split(":")
        return GenModel_2551_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2551_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2551_,
    private val saveUseCase: GenSaveUseCase_2551_,
    private val deleteUseCase: GenDeleteUseCase_2551_,
    private val searchUseCase: GenSearchUseCase_2551_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2551_>(GenState_2551_.Idle)
    val state: StateFlow<GenState_2551_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2551_) {
        when (event) {
            is GenEvent_2551_.Load -> loadAll()
            is GenEvent_2551_.Update -> save(event.model)
            is GenEvent_2551_.Delete -> delete(event.id)
            is GenEvent_2551_.Refresh -> loadAll()
            is GenEvent_2551_.Search -> search(event.query)
            is GenEvent_2551_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2551_.Loading; _state.value = GenState_2551_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2551_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2551_.Success(searchUseCase(query)) } }
}
