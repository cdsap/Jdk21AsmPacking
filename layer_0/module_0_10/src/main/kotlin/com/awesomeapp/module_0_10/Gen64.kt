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

data class GenModel_64_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_64_ {
    data class Load(val id: Long) : GenEvent_64_()
    data class Update(val model: GenModel_64_) : GenEvent_64_()
    data class Delete(val id: Long) : GenEvent_64_()
    data object Refresh : GenEvent_64_()
    data class Search(val query: String) : GenEvent_64_()
    data class Filter(val predicate: String) : GenEvent_64_()
}

sealed class GenState_64_ {
    data object Idle : GenState_64_()
    data object Loading : GenState_64_()
    data class Success(val items: List<GenModel_64_>) : GenState_64_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_64_()
    data class Partial(val items: List<GenModel_64_>, val hasMore: Boolean) : GenState_64_()
}

interface GenRepository_64_ {
    suspend fun getAll(): List<GenModel_64_>
    suspend fun getById(id: Long): GenModel_64_?
    suspend fun save(model: GenModel_64_): GenModel_64_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_64_>
}

@Singleton
class GenRepositoryImpl_64_ @Inject constructor() : GenRepository_64_ {
    private val store = mutableMapOf<Long, GenModel_64_>()
    override suspend fun getAll(): List<GenModel_64_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_64_? = store[id]
    override suspend fun save(model: GenModel_64_): GenModel_64_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_64_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_64_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_64_ @Inject constructor(
    private val repository: GenRepositoryImpl_64_
) : GenUseCase_64_<Unit, List<GenModel_64_>> {
    override suspend fun invoke(params: Unit): List<GenModel_64_> = repository.getAll()
}

class GenSaveUseCase_64_ @Inject constructor(
    private val repository: GenRepositoryImpl_64_
) : GenUseCase_64_<GenModel_64_, GenModel_64_> {
    override suspend fun invoke(params: GenModel_64_): GenModel_64_ = repository.save(params)
}

class GenDeleteUseCase_64_ @Inject constructor(
    private val repository: GenRepositoryImpl_64_
) : GenUseCase_64_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_64_ @Inject constructor(
    private val repository: GenRepositoryImpl_64_
) : GenUseCase_64_<String, List<GenModel_64_>> {
    override suspend fun invoke(params: String): List<GenModel_64_> = repository.search(params)
}

abstract class GenMapper_64_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_64_ : GenMapper_64_<GenModel_64_, String>() {
    override fun map(input: GenModel_64_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_64_ : GenMapper_64_<String, GenModel_64_>() {
    override fun map(input: String): GenModel_64_ {
        val parts = input.split(":")
        return GenModel_64_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_64_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_64_,
    private val saveUseCase: GenSaveUseCase_64_,
    private val deleteUseCase: GenDeleteUseCase_64_,
    private val searchUseCase: GenSearchUseCase_64_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_64_>(GenState_64_.Idle)
    val state: StateFlow<GenState_64_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_64_) {
        when (event) {
            is GenEvent_64_.Load -> loadAll()
            is GenEvent_64_.Update -> save(event.model)
            is GenEvent_64_.Delete -> delete(event.id)
            is GenEvent_64_.Refresh -> loadAll()
            is GenEvent_64_.Search -> search(event.query)
            is GenEvent_64_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_64_.Loading; _state.value = GenState_64_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_64_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_64_.Success(searchUseCase(query)) } }
}
