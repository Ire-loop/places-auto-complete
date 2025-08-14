package com.example.places.autocomplete.ui.model

/**
 * - Generic UI state class for handling different states of UI data.
 * - Used to represent loading, success, and error states in a type-safe way.
 */
sealed class UiState<out T> {
    /**
     * Loading state when data is being fetched
     */
    object Loading : UiState<Nothing>()

    /**
     * Success state with the fetched data
     *
     * @param data The successfully loaded data
     */
    data class Success<out T>(val data: T) : UiState<T>()

    /**
     * Error state when data fetching fails
     *
     * @param message Error message explaining what went wrong
     */
    data class Error(val message: String) : UiState<Nothing>()

    /**
     * Transforms the success data using the provided transformation function
     */
    fun <R> map(transform: (T) -> R):UiState<R> {
        return when (this) {
            is Loading -> Loading
            is Error -> Error(message)
            is Success -> Success(transform(data))
        }
    }
}