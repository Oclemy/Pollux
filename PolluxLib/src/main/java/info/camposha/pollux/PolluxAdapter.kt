package info.camposha.pollux

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class PolluxAdapter<M> private constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val filteredResults: ArrayList<M> = ArrayList()
    private val originalList: ArrayList<M> = ArrayList()

    private val VIEW_ITEM = 1
    private val VIEW_PROGRESS = 0

    private var isLoading = false
    private var isLoadMoreEnabled = false
    private var loadMoreResource = -1

    internal var onItemViewClick: ((View, M, Int) -> Unit?)? = null
    internal var clickableIds: IntArray? = null

    //=======FILTER RELATED=======
    private var isFilterApplied = false
    private var query: String? = null

    val data: ArrayList<M>
        get() = if (isFilterApplied) {
            filteredResults
        } else originalList

    override fun getItemViewType(position: Int): Int {
        return if (isLoadMoreEnabled && isLoading) {
            if (position == itemCount - 1) {
                VIEW_PROGRESS
            } else {
                try {
                    getViewType(data[position])
                } catch (e: Exception) {
                    getViewType(data[position - 1])
                }
            }
        } else {
            try {
                getViewType(data[position])
            } catch (e: Exception) {
                getViewType(data[position - 1])
            }
        }
    }


    fun setClickableViews(
        onItemViewClick: (view: View, model: M, adapterPosition: Int) -> Unit,
        vararg ids: Int
    ): PolluxAdapter<M> {
        this.onItemViewClick = onItemViewClick
        this.clickableIds = ids
        return this
    }

    private fun getProgressView(context: Context): View {
        val view = FrameLayout(context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyle)
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.CENTER_HORIZONTAL
        PolluxUtils.setMargins(progressBar, 0, 0, 0, 20)
        progressBar.layoutParams = lp

        (view as ViewGroup).addView(progressBar)
        return view
    }

    fun refreshData() {
        this.filteredResults.clear()
        this.filteredResults.addAll(originalList)
    }

    private fun getViewType(model: M): Int {
        for (triple in viewTypeList.reversed()) {
            if (triple.third(model)) {
                return triple.first
                break
            }
        }
        return 0
    }

    private fun getBinder(viewType: Int): ((Int, M, ViewDataBinding) -> Unit)? {
        for (triple in viewTypeList.reversed()) {
            if (triple.first == viewType) {
                return triple.second
                break
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == VIEW_PROGRESS) {
            val view: View = if (loadMoreResource == -1) {
                getProgressView(parent.context)
            } else {
                LayoutInflater.from(parent.context).inflate(loadMoreResource, parent, false)
            }
            return ProgressViewHolder(view)
        } else {
            val viewHolder = SimpleViewHolder(parent, viewType, getBinder(viewType))

            if (viewHolder.binder != null) {
                if (clickableIds != null && onItemViewClick != null) {
                    val clickListener = View.OnClickListener { view ->
                        try {
                            val model: M

                            if (isFilterApplied)
                                model = filteredResults[viewHolder.adapterPosition]
                            else
                                model = originalList[viewHolder.adapterPosition]

                            onItemViewClick?.invoke(view, model, viewHolder.adapterPosition)
                        } catch (e: Exception) {
                        }
                    }
                    for (clickableId in clickableIds!!) {
                        viewHolder.itemView.findViewById<View>(clickableId)
                            ?.setOnClickListener(clickListener)
                    }
                }
            }

            return viewHolder
        }
    }

    /*override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>?) {
        var isHandled = false
        if (holder !is ProgressViewHolder) {
            if (holder is SimpleAdapter<M>.SimpleViewHolder) {
                (holder as SimpleAdapter<M>.SimpleViewHolder).binder?.invoke(holder.adapterPosition, if (isFilterApplied) filteredResults!![position] else originalList!![position], holder.binding)
            }
        }
        if (!isHandled) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }*/

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is ProgressViewHolder) {
            if (holder is PolluxAdapter<*>.SimpleViewHolder) {
                (holder as PolluxAdapter<M>.SimpleViewHolder).binder?.invoke(
                    holder.adapterPosition,
                    if (isFilterApplied) filteredResults!![position] else originalList!![position],
                    holder.binding!!
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isFilterApplied) {
            if (isLoadMoreEnabled && isLoading) filteredResults.size + 1 else filteredResults.size
        } else {
            if (isLoadMoreEnabled && isLoading) originalList.size + 1 else originalList.size
        }
    }

    //=======LOAD MORE RELATED=======
    fun markLoadMoreAsComplete() {
        if (isLoading) {
            isLoading = false
            android.os.Handler().post { notifyItemRemoved(itemCount - 1) }
        }
    }

    fun setupLoadMorePagination(recyclerView: RecyclerView, loadMoreResource: Int = -1, onLoadMoreListener: () -> Boolean): PolluxAdapter<M> {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return try {
                        if (getItemViewType(position) == VIEW_PROGRESS) layoutManager.spanCount else 1
                    } catch (e: Exception) {
                        1
                    }
                }
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (layoutManager != null) {
                    val totalItemCount = layoutManager.itemCount - 1
                    var lastVisibleItem = 0

                    when (layoutManager) {
                        is StaggeredGridLayoutManager -> {
                            val lastVisibleItemPositions =
                                layoutManager.findLastVisibleItemPositions(null)
                            // get maximum element within the list
                            lastVisibleItem = getLastVisibleItem(lastVisibleItemPositions)
                            //                        firstVisibleItem = getFirstVisibleItem(((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null));
                        }
                        is GridLayoutManager -> {
                            lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                            //                        firstVisibleItem = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
                        }
                        is LinearLayoutManager -> {
                            lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                        }
                    }

                    if (!isLoading && totalItemCount <= lastVisibleItem) {
                        android.os.Handler().post {
                            val previous = isLoading
                            isLoading = onLoadMoreListener()
                            if (isLoading != previous) {
                                if (!previous && isLoading) {
                                    notifyItemInserted(itemCount - 1)
                                } else if (previous && !isLoading) {
                                    notifyItemRemoved(itemCount - 1)
                                }
                            }
                        }
                    }
                }
            }
        })

        android.os.Handler().postDelayed({
            //                    isLoading = onLoadMoreListener.onLoadMore();
            //                    notifyDataSetChanged();
        }, 2000)
        isLoadMoreEnabled = true
        this.loadMoreResource = loadMoreResource
        return this
    }

    /**
     * Used for staggered
     *
     * @param lastVisibleItemPositions
     * @return
     */
    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0

        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    private fun clearFilter() {
        filteredResults.clear()
        filteredResults.addAll(originalList)
        isFilterApplied = false
    }

    interface FilterLogic<M> {
        fun filter(text: String, model: M): Boolean
    }

    private var filterLogic: ((String, M) -> Boolean)? = null
    fun performFilter(text: String?, filterLogic: ((text: String, model: M) -> Boolean)?) {
        this.query = text
        this.filterLogic = filterLogic
        if (text!!.isEmpty()) {
            clearFilter()
        } else {
            filteredResults.clear()
            for (model in originalList) {
                if (filterLogic?.invoke(text, model) == true) {
                    filteredResults.add(model)
                }
            }
            isFilterApplied = true
        }
        notifyDataSetChanged()
    }

    private open inner class SimpleViewHolder(
        parent: ViewGroup,
        res: Int,
        var binder: ((Int, M, ViewDataBinding) -> Unit)?
    ) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(res, parent, false)) {
        var binding: ViewDataBinding? = DataBindingUtil.bind(itemView)
    }

    //--------NEW VIEW TYPE IMPLEMENTATION--------


    private val viewTypeList: ArrayList<Triple<Int, (adapterPosition: Int, model: M, binding: ViewDataBinding) -> Unit, (model: M) -> Boolean>> =
        ArrayList()

    fun <B : ViewDataBinding> addViewType(
        res: Int,
        binder: (adapterPosition: Int, model: M, binding: B) -> Unit,
        viewTypeLogic: (model: M) -> Boolean
    ) {
        viewTypeList.add(Triple(res, binder as (Int, M, ViewDataBinding) -> Unit, viewTypeLogic))
    }
    //--------NEW VIEW TYPE IMPLEMENTATION--------

    private class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v)

    private fun ifFilterAppliedThenFilterList() {
        if (isFilterApplied) {
            performFilter(query, filterLogic)
        }
    }

    //ArrayListFunctions
    fun add(e: M): Boolean {
        val r = originalList.add(e)
        ifFilterAppliedThenFilterList()
        return r
    }

    fun add(index: Int, element: M) {
        originalList.add(index, element)
        ifFilterAppliedThenFilterList()
    }

    operator fun get(index: Int): M {
        return originalList[index]
    }

    fun addAll(c: List<M>): Boolean {
        val r = originalList.addAll(c)
        ifFilterAppliedThenFilterList()
        return r
    }

    fun addAll(c: Collection<M>): Boolean {
        val r = originalList.addAll(c)
        ifFilterAppliedThenFilterList()
        return r
    }

    fun addAll(index: Int, c: Collection<M>): Boolean {
        val r = originalList.addAll(index, c)
        if (isFilterApplied) {
            performFilter(query, filterLogic)
        }
        return r
    }

    operator fun set(index: Int, e: M): M {
        val r = originalList.set(index, e)
        ifFilterAppliedThenFilterList()
        return r
    }

    fun remove(index: Int): M {
        val r = originalList.removeAt(index)
        ifFilterAppliedThenFilterList()
        return r
    }

    fun remove(o: M): Boolean {
        val r = originalList.remove(o)
        ifFilterAppliedThenFilterList()
        return r
    }

    fun clear() {
        originalList.clear()
        ifFilterAppliedThenFilterList()
    }

    fun removeAll(c: Collection<M>): Boolean {
        val r = originalList!!.removeAll(c)
        ifFilterAppliedThenFilterList()
        return r
    }


    /*lateinit var testB: (adapaterPosition: Int, model: M, binding: ViewDataBinding) -> Unit
    fun <B : ViewDataBinding> addViewType(res: Int, binder: (adapaterPosition: Int, model: M, binding: B) -> Unit) {
        testB = binder as (Int, M, ViewDataBinding) -> Unit
    }*/

    companion object {
        fun <M, B : ViewDataBinding> with(
            res: Int,
            binder: (adapaterPosition: Int, model: M, binding: B) -> Unit
        ): PolluxAdapter<M> {
            val simpleAdapter = PolluxAdapter<M>()
            simpleAdapter.addViewType(res, binder, viewTypeLogic = { true })
            return simpleAdapter
        }
    }
}