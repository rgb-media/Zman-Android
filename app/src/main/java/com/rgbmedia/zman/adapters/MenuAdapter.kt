package com.rgbmedia.zman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rgbmedia.zman.R
import com.rgbmedia.zman.ZmanApplication
import com.rgbmedia.zman.utils.LoginState
import com.rgbmedia.zman.viewmodels.MainMenuElement

class MenuAdapter(private val dataSet: List<MainMenuElement>) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView
        val itemsRecyclerView: RecyclerView

        init {
            textView = view.findViewById(R.id.textView)
            imageView = view.findViewById(R.id.imageView)
            itemsRecyclerView = view.findViewById(R.id.itemsRecyclerView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.menu_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet.get(position)
        val menuElement = data.item

        viewHolder.textView.text = menuElement.title

        if (menuElement.items?.isEmpty() != null && menuElement.type != "login") {
            viewHolder.imageView.setImageResource(R.drawable.arrow_down)

            if (data.expanded) {
                viewHolder.imageView.setImageResource(R.drawable.arrow_up)
            }

            viewHolder.itemView.setOnClickListener {
                data.expanded = !data.expanded

                notifyDataSetChanged()
            }
        } else {
            if (menuElement.type == "login" && LoginState.isLoggedIn()) {
            } else {
                val identifier = ZmanApplication.instance.resources.getIdentifier(menuElement.icon.lowercase(), "drawable", ZmanApplication.instance.packageName)
                viewHolder.imageView.setImageResource(identifier)
            }
        }

        if (data.expanded) {
            viewHolder.itemsRecyclerView.adapter = MenuItemsAdapter(menuElement.items.toList())
        } else {
            viewHolder.itemsRecyclerView.adapter = null
        }
    }

    override fun getItemCount() = dataSet.size
}