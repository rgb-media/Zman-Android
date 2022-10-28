package com.rgbmedia.zman.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.rgbmedia.zman.R
import com.rgbmedia.zman.ZmanApplication
import com.rgbmedia.zman.utils.LoginState
import com.rgbmedia.zman.utils.PicassoCircleTransformation
import com.rgbmedia.zman.viewmodels.MainMenuElement
import com.rgbmedia.zman.viewmodels.MainViewModel
import com.squareup.picasso.Picasso
import java.net.URL


class MenuAdapter(private val dataSet: List<MainMenuElement>, private val mainViewModel: MainViewModel) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

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
        val data = dataSet[position]
        val menuElement = data.item

        viewHolder.textView.text = menuElement.title

        viewHolder.imageView.layoutParams.width = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        viewHolder.imageView.layoutParams.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT

        if (menuElement.items?.isEmpty() != null && menuElement.type != "login") {
            viewHolder.imageView.setImageResource(R.drawable.arrow_down)

            if (data.expanded) {
                viewHolder.imageView.setImageResource(R.drawable.arrow_up)
            }
        } else {
            if (menuElement.type == "login" && LoginState.isLoggedIn()) {
                viewHolder.imageView.layoutParams.width = ZmanApplication.instance.resources.getDimension(R.dimen.menu_item_image_size).toInt()
                viewHolder.imageView.layoutParams.height = ZmanApplication.instance.resources.getDimension(R.dimen.menu_item_image_size).toInt()

                val imageUrl = LoginState.getImage()

                Picasso.get().load(imageUrl).transform(PicassoCircleTransformation()).into(viewHolder.imageView)
            } else {
                val identifier = ZmanApplication.instance.resources.getIdentifier(menuElement.icon.lowercase(), "drawable", ZmanApplication.instance.packageName)
                viewHolder.imageView.setImageResource(identifier)
            }
        }

        if (data.expanded || (menuElement.type == "login" && LoginState.isLoggedIn())) {
            viewHolder.itemsRecyclerView.adapter = MenuItemsAdapter(menuElement.items.toList(), mainViewModel, position)
        } else {
            viewHolder.itemsRecyclerView.adapter = null

            if (position == mainViewModel.getSearchPosition().first) {
                mainViewModel.setSearchResultsVisible(false)
            }
        }

        viewHolder.itemView.setOnClickListener {
            if (menuElement.type == "login") {
                if (!LoginState.isLoggedIn()) {
                    mainViewModel.setShowLogin(true)
                }
            } else {
                data.expanded = !data.expanded

                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount() = dataSet.size

}