package com.cniekirk.twitchhook.data

import com.cniekirk.twitchhook.StreamEvent
import kotlinx.coroutines.flow.SharedFlow

interface DataStreamProvider {
    val eventStream: SharedFlow<StreamEvent>
    fun startDataStream(dataStreamConfig: DataStreamConfig)
    fun stopDataStream()
    fun clean()
}