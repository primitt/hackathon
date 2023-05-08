package dev.primitt.graphql

import com.apollographql.apollo3.api.ApolloResponse
import dev.primitt.apolloClient

suspend fun queryLaunchList(): ApolloResponse<LaunchListQuery.Data> {
    return apolloClient.query(LaunchListQuery()).execute()
}