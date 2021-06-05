package com.cniekirk.twitchhook.data

import com.cniekirk.twitchhook.StreamEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf

@FlowPreview
class DataStreamMuxer(
    private val providers: Map<String, DataStreamProvider>
) {

    /**
     * Start the data streams in [providers]
     */
    fun start(dataStreamConfig: Map<String, DataStreamConfig>) {
        providers.forEach { provider ->
            dataStreamConfig[provider.key]?.let { provider.value.startDataStream(it) }
        }
    }

    /**
     * Combine each data stream from [providers] and return a [Flow] that will emit all
     * items from each respective data stream in order
     */
    fun combinedEventStream(): Flow<StreamEvent> =
        flowOf(*providers.map { it.value.eventStream }.toTypedArray()).flattenMerge()

    /**
     * Stops data streams in [providers] from emitting any more data as they're hot streams
     * which need to be manually stopped
     */
    fun stop() = providers.forEach { it.value.stopDataStream() }

}