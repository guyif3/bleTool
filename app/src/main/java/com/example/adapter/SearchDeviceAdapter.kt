package com.example.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.R
import com.example.viewholder.SearchViewholer
import com.library.ble.data.SearchData
import com.zhy.autolayout.utils.AutoUtils
import java.util.ArrayList

/**
 * Created by gongjianghua on 16/7/6.
 */

class SearchDeviceAdapter(private val listener: View.OnClickListener) : RecyclerView.Adapter<SearchViewholer>() {
    private var list:ArrayList<SearchData> = ArrayList<SearchData>()

    init {
        list = ArrayList<SearchData>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewholer {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        AutoUtils.auto(view)
        return SearchViewholer(view)
    }

    override fun onBindViewHolder(holder: SearchViewholer, position: Int) {
        if (list.size > 0 && position < list.size) {
            holder.showData(list[position])
        }
    }

    override fun getItemCount(): Int {
        if (list.size > 0) return list.size
        return 0
    }

    fun addSearData(d: SearchData) {
        if (list.contains(d)) {
            var index = -1
            for (i in list.indices) {
                val old = list[i]
                if (old == d) {
                    index = i
                    old.update_time = d.update_time
                    old.ressi = d.ressi
                    break
                }
            }
            if (index != -1) notifyItemChanged(index)
        } else {
             list.add(d);
            notifyDataSetChanged()
        }
    }

    fun showSearchState() {

    }
}
