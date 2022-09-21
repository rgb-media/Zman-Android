package com.rgbmedia.zman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rgbmedia.zman.R
import com.rgbmedia.zman.ZmanApplication
import com.rgbmedia.zman.models.MenuItem

class MenuItemsAdapter(private val dataSet: List<MenuItem>) : RecyclerView.Adapter<MenuItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView

        init {
            textView = view.findViewById(R.id.textView)
            imageView = view.findViewById(R.id.imageView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.menu_row_subitem, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val menuItem = dataSet.get(position)

        viewHolder.textView.text = menuItem.title

        viewHolder.imageView.visibility = View.GONE
        if (menuItem.type == "image") {
        } else if (menuItem.icon != null) {
            viewHolder.imageView.visibility = View.VISIBLE

            val identifier = ZmanApplication.instance.resources.getIdentifier(menuItem.icon.lowercase(), "drawable", ZmanApplication.instance.packageName)
            viewHolder.imageView.setImageResource(identifier)
        }
    }

    override fun getItemCount() = dataSet.size
}