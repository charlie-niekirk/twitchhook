package com.cniekirk.twitchhook.data

import com.cniekirk.twitchhook.StreamEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf

@FlowPreview
class DataStreamMuxer(
    private val providers: List<DataStreamProvider>
) {

    /**
     * Combine each data stream and return a [Flow] that will emit all items from
     * each respective data stream in order
     */
    fun combinedEventStream(): Flow<StreamEvent> =
        flowOf(*providers.map { it.eventStream }.toTypedArray()).flattenMerge()

}