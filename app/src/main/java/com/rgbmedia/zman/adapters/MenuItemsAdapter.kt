package com.rgbmedia.zman.adapters

import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.rgbmedia.zman.*
import com.rgbmedia.zman.models.MenuItem
import com.rgbmedia.zman.utils.Utils
import com.rgbmedia.zman.viewmodels.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MenuItemsAdapter(private val dataSet: List<MenuItem>, private val mainViewModel: MainViewModel,
                       private val mainElementIndex: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_TYPE_NORMAL        = 1
    val ITEM_TYPE_NEWSLETTER    = 2
    val ITEM_TYPE_SEARCH        = 3

    class StandardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView
        val sectionOn: ImageView

        init {
            textView = view.findViewById(R.id.textView)
            imageView = view.findViewById(R.id.imageView)
            sectionOn = view.findViewById(R.id.sectionOnImageView)
        }
    }

    class NewsletterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: RelativeLayout
        val button: ImageView
        val editText: EditText
        val finalText: TextView

        init {
            container = view.findViewById(R.id.newsletterContainer)
            button = view.findViewById(R.id.subscribeButton)
            editText = view.findViewById(R.id.emailEditText)
            finalText = view.findViewById(R.id.newsletterTextView)
        }
    }

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchButton: ImageView
        val hintTV: TextView
        val editText: EditText

        init {
            searchButton = view.findViewById(R.id.searchIV)
            hintTV = view.findViewById(R.id.hintTV)
            editText = view.findViewById(R.id.searchEditText)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_NEWSLETTER) {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.menu_row_newsletter, viewGroup, false)

            return NewsletterViewHolder(view)
        }

        if (viewType == ITEM_TYPE_SEARCH) {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.menu_row_search, viewGroup, false)

            return SearchViewHolder(view)
        }

        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.menu_row_subitem, viewGroup, false)

        return StandardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val menuItem = dataSet[position]

        if (menuItem.type == "search") {
            mainViewModel.setSearchPosition(Pair(mainElementIndex, position))

            val viewHolder = holder as SearchViewHolder

            var searchJob: Job? = null

            viewHolder.editText.doAfterTextChanged {
                if (it.toString().isEmpty()) {
                    viewHolder.searchButton.setImageResource(R.drawable.search)
                    viewHolder.hintTV.visibility = View.VISIBLE
                } else {
                    viewHolder.searchButton.setImageResource(R.drawable.close_search)
                    viewHolder.hintTV.visibility = View.GONE
                }

                searchJob?.cancel()
                searchJob = mainViewModel.viewModelScope.launch {
                    delay(1_000)

                    mainViewModel.search(it.toString())
                }
            }

            viewHolder.searchButton.setOnClickListener {
                if (viewHolder.editText.text.isNotEmpty()) {
                    viewHolder.editText.setText("")
                }
            }
        } else if (menuItem.type == "newsletter") {
            val viewHolder = holder as NewsletterViewHolder

            if (mainViewModel.getNewsletterResponse().value?.lowercase() == "success") {
                viewHolder.button.setOnClickListener(null)
                viewHolder.editText.isEnabled = false

                viewHolder.finalText.scaleX = 0f
                viewHolder.finalText.animate().scaleX(1f).setDuration(MENU_ANIMATION_DURATION).start()
            } else {
                viewHolder.finalText.scaleX = 0f
                viewHolder.editText.isEnabled = true

                viewHolder.button.setOnClickListener {
                    mainViewModel.setNewsletterPosition(Pair(mainElementIndex, position))

                    val email = viewHolder.editText.text.toString()

                    if (Utils.isValidEmail(email)) {
                        viewHolder.container.setBackgroundResource(R.drawable.newsletter_border_white)
                        viewHolder.button.setBackgroundResource(R.drawable.subscribe_white)

                        mainViewModel.sendEmail(email)
                    } else {
                        viewHolder.container.setBackgroundResource(R.drawable.newsletter_border_red)
                        viewHolder.button.setBackgroundResource(R.drawable.subscribe_red)
                    }
                }
            }
        } else {
            val viewHolder = holder as StandardViewHolder

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

                viewHolder.imageView.layoutParams.width =
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                viewHolder.imageView.visibility = View.VISIBLE

                val identifier = ZmanApplication.instance.resources.getIdentifier(
                    menuItem.icon.lowercase(),
                    "drawable",
                    ZmanApplication.instance.packageName
                )
                viewHolder.imageView.setImageResource(identifier)
            } else if (menuItem.icon != null) {
                viewHolder.imageView.visibility = View.VISIBLE

                val identifier = ZmanApplication.instance.resources.getIdentifier(
                    menuItem.icon.lowercase(),
                    "drawable",
                    ZmanApplication.instance.packageName
                )
                viewHolder.imageView.setImageResource(identifier)
            }

            viewHolder.itemView.setOnClickListener {
                if (menuItem.link != null) {
                    if (menuItem.link.contains(DOMAIN_NAME) && !menuItem.link.contains("facebook.com")) {
                        mainViewModel.setWebviewUrlString(menuItem.link)

                        mainViewModel.setShowMenu(false)

                        mainViewModel.setSelectedMenuItem(Pair(mainElementIndex, position))
                    } else {
                        val browserIntent: Intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(menuItem.link)).apply {
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
    }

    override fun getItemViewType(position: Int): Int {
        val menuItem = dataSet[position]

        if (menuItem.type == "newsletter") {
            return ITEM_TYPE_NEWSLETTER
        }

        if (menuItem.type == "search") {
            return ITEM_TYPE_SEARCH
        }

        return ITEM_TYPE_NORMAL
    }

    override fun getItemCount() = dataSet.size
}