package io.github.vivitoto.vanga.homefilters

import kotlinx.coroutines.flow.Flow

interface HomeScreenFilterRepository {
    fun getFilters(): Flow<List<HomeScreenFilter>>
    suspend fun putFilters(filters: List<HomeScreenFilter>)
}