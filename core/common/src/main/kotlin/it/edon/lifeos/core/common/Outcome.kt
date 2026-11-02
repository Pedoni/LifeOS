package it.edon.lifeos.core.common

/**
 * Result of an operation that can fail in an expected way. The error type [E] is part of the
 * signature, so callers must decide what to do with each kind of failure instead of catching
 * exceptions.
 */
sealed interface Outcome<out T, out E> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>

    data class Failure<out E>(val error: E) : Outcome<Nothing, E>
}

inline fun <T, E, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

fun <T, E> Outcome<T, E>.getOrNull(): T? = (this as? Outcome.Success)?.value

fun <T, E> Outcome<T, E>.errorOrNull(): E? = (this as? Outcome.Failure)?.error
