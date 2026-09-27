package com.habitrpg.android.habitica.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.models.BaseMainObject

open class DiffCallback<T : BaseMainObject>(
    protected val oldList: List<BaseMainObject>,
    protected val newList: List<BaseMainObject>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldList[oldItemPosition].primaryIdentifier == newList[newItemPosition].primaryIdentifier
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}

abstract class BaseRecyclerViewAdapter<T : BaseMainObject, VH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<VH>() {
    open fun getDiffCallback(
        oldList: List<T>,
        newList: List<T>
    ): DiffCallback<T>? {
        return null
    }

    private var items: List<T> = emptyList()

    var data: List<T>
        get() = items
        set(value) {
            val diffCallback = getDiffCallback(items, value)
            items = value
            if (diffCallback != null) {
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                diffResult.dispatchUpdatesTo(this)
            } else {
                notifyDataSetChanged()
            }
        }

    /** Updates backing data without notifying — used while ItemTouchHelper is dragging. */
    protected fun replaceDataSilently(value: List<T>) {
        items = value
    }

    override fun getItemCount(): Int {
        return items.size
    }

    open fun getItem(position: Int): T? {
        return if (position >= 0 && items.size > position) {
            items[position]
        } else {
            null
        }
    }
}
