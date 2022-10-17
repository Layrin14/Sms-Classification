package com.layrin.smsclassification.ui.common


class SelectionManager {

    private val _selectedItems = mutableListOf<Int>()

    val selectedItems get() = _selectedItems.toList()

    val isMultiSelectionModeActive get() = _selectedItems.size > 0

    fun isItemSelected(position: Int): Boolean = _selectedItems.contains(position)

    fun toggleSelectedItem(position: Int, listener: (Int) -> Unit) {
        if (_selectedItems.contains(position)) _selectedItems.remove(position)
        else _selectedItems.add(position)
        listener(_selectedItems.size)
    }

    fun clearSelectedItems(): List<Int> {
        if (!isMultiSelectionModeActive) return emptyList()
        val copy = _selectedItems.toList()
        _selectedItems.clear()
        return copy
    }
}