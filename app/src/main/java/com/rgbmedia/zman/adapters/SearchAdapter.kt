package com.rgbmedia.zman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rgbmedia.zman.DOMAIN_NAME
import com.rgbmedia.zman.R
import com.rgbmedia.zman.models.SearchResult
import com.rgbmedia.zman.viewmodels.MainViewModel

class SearchAdapter(private val dataSet: Array<SearchResult>, private val mainViewModel: MainViewModel) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taxonomyTV: TextView
        val nameTV: TextView

        init {
            taxonomyTV = view.findViewById(R.id.taxonomyTV)
            nameTV = view.findViewById(R.id.nameTV)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SearchAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.search_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.taxonomyTV.text = data.taxonomy
        viewHolder.nameTV.text = data.name

        viewHolder.itemView.setOnClickListener {
            if (data.link != null) {
                mainViewModel.setWebviewUrlString(data.link)

                mainViewModel.setShowMenu(false)
            }
        }
    }

    override fun getItemCount() = dataSet.size
}