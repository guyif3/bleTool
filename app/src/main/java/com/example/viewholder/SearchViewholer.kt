package com.example.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.example.R
import com.library.ble.data.SearchData
import com.zhy.autolayout.utils.AutoUtils


/**
 * Created by gongjianghua on 16/7/6.
 */

class SearchViewholer(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var data: SearchData? = null
    internal var name: TextView? = null
    internal var mac: TextView? = null
    internal var ressi: TextView? = null

    init {
        AutoUtils.auto(itemView)
        name=itemView.findViewById(R.id.name) as TextView
        mac=itemView.findViewById(R.id.mac) as TextView
        ressi=itemView.findViewById(R.id.ressi) as TextView
    }

    fun showData(searchData: SearchData) {
        this.data = searchData
        name!!.text = data!!.device.name
        mac!!.text = data!!.mac
        ressi!!.text = "信号" + data!!.ressi
    }
}
