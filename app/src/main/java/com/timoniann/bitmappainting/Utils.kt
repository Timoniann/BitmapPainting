package com.timoniann.bitmappainting

import java.util.*

fun<R, K, T : Iterable<K>> T.take(action: ((K) -> R)): List<R>{
    val result = LinkedList<R>()
    forEach { result.add(action(it)) }
    return result
}