package com.topstreams.firetv

enum class StreamProvider(val displayName: String) {
    TOPSTREAMS("TopStreams"),
    GIVEMEREDDITSTREAMS("GiveMeRedditStreams"),
    METHSTREAMS("MethStreams");
    
    companion object {
        val DEFAULT = TOPSTREAMS
        
        fun fromString(value: String?): StreamProvider {
            return values().find { it.name == value } ?: DEFAULT
        }
    }
}