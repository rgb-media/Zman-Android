package com.rgbmedia.zman.adapters

import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rgbmedia.zman.DOMAIN_NAME
import com.rgbmedia.zman.R
import com.rgbmedia.zman.ZmanApplication
import com.rgbmedia.zman.models.MenuItem
import com.rgbmedia.zman.viewmodels.MainViewModel


class MenuItemsAdapter(private val dataSet: List<MenuItem>, private val mainViewModel: MainViewModel,
                       private val mainElementIndex: Int) : RecyclerView.Adapter<MenuItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView
        val sectionOn: ImageView

        init {
            textView = view.findViewById(R.id.textView)
            imageView = view.findViewById(R.id.imageView)
            sectionOn = view.findViewById(R.id.sectionOnImageView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.menu_row_subitem, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val menuItem = dataSet[position]

        viewHolder.textView.text = menuItem.title

        if (mainViewModel.getWebviewUrlString().value == menuItem.link) {
            viewHolder.sectionOn.visibility = View.VISIBLE
        } else {
            viewHolder.sectionOn.visibility = View.INVISIBLE
        }

        viewHolder.imageView.layoutParams.width = ZmanApplication.instance.resources.getDimension(R.dimen.menu_subitem_image_witdh).toInt()
        viewHolder.imageView.visibility = View.GONE

        viewHolder.textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        viewHolder.textView.alpha = 1f

        if (menuItem.type == "image") {
            viewHolder.textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
            viewHolder.textView.alpha = 0.7f

            viewHolder.imageView.layoutParams.width = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            viewHolder.imageView.visibility = View.VISIBLE

            val identifier = ZmanApplication.instance.resources.getIdentifier(menuItem.icon.lowercase(), "drawable", ZmanApplication.instance.packageName)
            viewHolder.imageView.setImageResource(identifier)
        } else if (menuItem.icon != null) {
            viewHolder.imageView.visibility = View.VISIBLE

            val identifier = ZmanApplication.instance.resources.getIdentifier(menuItem.icon.lowercase(), "drawable", ZmanApplication.instance.packageName)
            viewHolder.imageView.setImageResource(identifier)
        }

        viewHolder.itemView.setOnClickListener {
            if (menuItem.link != null) {
                if (menuItem.link.contains(DOMAIN_NAME) && !menuItem.link.contains("facebook.com")) {
                    mainViewModel.setWebviewUrlString(menuItem.link)

                    mainViewModel.setShowMenu(false)

                    mainViewModel.setSelectedMenuItem(Pair(mainElementIndex, position))
                } else {
                    val browserIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(menuItem.link)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    ContextCompat.startActivity(ZmanApplication.instance, browserIntent, null)
                }

//                if link.contains(Constants.DOMAIN_NAME) && !link.contains("facebook.com") {
//                    webViewModel.urlString = menuItem.type == "viewProfile" ? "\(link)\(LoginState.getId())/" : link
//                } else if let url = URL(string: link) {
//                    UIApplication.shared.open(url, options: [:], completionHandler: nil)
//                }
            }
        }
    }

    override fun getItemCount() = dataSet.size
}