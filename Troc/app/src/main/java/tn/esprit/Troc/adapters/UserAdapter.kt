package tn.esprit.Troc.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tn.esprit.Troc.R
import tn.esprit.Troc.models.User
import tn.esprit.Troc.ui.activities.ProfileActivity
import tn.esprit.Troc.utils.Constants
import tn.esprit.Troc.utils.ImageLoader

class UserAdapter(private var items: MutableList<User>) :
    RecyclerView.Adapter<UserAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.single_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bindView(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SearchViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val imageIV: ImageView = itemView.findViewById(R.id.imageIV)
        private val descriptionTV: TextView = itemView.findViewById(R.id.titleTV)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)


        fun bindView(user: User) {

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ProfileActivity::class.java)
                intent.putExtra("user", user)
                itemView.context.startActivity(intent)
            }

            ImageLoader.setImageFromUrl(
                imageIV,
                progressBar,
                Constants.BASE_URL_IMAGES + user.imageFilename
            )

            descriptionTV.text = user.username
        }
    }
}