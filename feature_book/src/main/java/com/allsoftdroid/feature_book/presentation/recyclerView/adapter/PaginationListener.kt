package com.allsoftdroid.feature_book.presentation.recyclerView.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationListener(private val layoutManager: LinearLayoutManager,
                                  private val pageSize:Int) : RecyclerView.OnScrollListener() {


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition()

        if(!isLoading()){
            if ((visibleItemCount + firstVisibleItemPos) >= totalItemCount
                && firstVisibleItemPos >= 0
                && totalItemCount >= pageSize) {
                loadNext()
            }
        }
    }

    protected abstract fun loadNext()

    abstract fun isLoading(): Boolean
}