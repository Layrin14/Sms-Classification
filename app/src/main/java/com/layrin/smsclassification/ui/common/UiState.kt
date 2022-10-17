package com.layrin.smsclassification.ui.common

interface UiState {
    val id: Int
    fun areItemsTheSame(other: UiState): Boolean = this.id == other.id
    fun areContentsTheSame(other: UiState): Boolean = this == other
}