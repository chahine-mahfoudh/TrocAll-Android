package tn.esprit.Troc.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tn.esprit.Troc.R
import tn.esprit.Troc.models.Post
import tn.esprit.Troc.models.User
import tn.esprit.Troc.service.ApiService
import tn.esprit.Troc.service.LikeService
import tn.esprit.Troc.service.PostService
import tn.esprit.Troc.ui.activities.ChatActivity
import tn.esprit.Troc.ui.activities.MainActivity
import tn.esprit.Troc.ui.modals.CommentsModal
import tn.esprit.Troc.utils.Constants

class FullPostAdapter(var items: MutableList<Post>) :

    RecyclerView.Adapter<FullPostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bindView(items[position])
    }

    override fun getItemCount(): Int = items.size

    class PostViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val videoView: VideoView = itemView.findViewById(R.id.videoView)
        private val postTitleTV: TextView = itemView.findViewById(R.id.postTitleTV)
        private val postDescriptionTV: TextView = itemView.findViewById(R.id.postDescriptionTV)
        private val reportButton: TextView = itemView.findViewById(R.id.reportButton)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val likeCountTV: TextView = itemView.findViewById(R.id.likeCountTV)
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private val commentsButton: ImageButton = itemView.findViewById(R.id.commentsButton)

        fun bindView(post: Post) {

            videoView.setVideoURI(Uri.parse(Constants.BASE_URL_VIDEOS + post.videoFilename))
            videoView.setOnPreparedListener { mp ->
                progressBar.visibility = View.GONE
                mp.start()
                val videoRatio = mp.videoWidth / mp.videoHeight.toFloat()
                val screenRatio: Float = videoView.width / videoView.height.toFloat()
                val scale = videoRatio / screenRatio
                if (scale >= 1f) {
                    videoView.scaleX = scale
                } else {
                    videoView.scaleY = 1f / scale
                }
            }
            videoView.setOnCompletionListener { mp -> mp.start() }

            postTitleTV.text = post.title
            postDescriptionTV.text = post.description





            var userHasLike = false
            var currentLikeCount = post.likes!!.size

            val sharedPreferences =
                itemView.context.getSharedPreferences(Constants.SHARED_PREF_SESSION, Context.MODE_PRIVATE)
            val userData = sharedPreferences.getString("USER_DATA", null)

            val user: User? = if (userData != null) {
                Gson().fromJson(userData, User::class.java)
            } else {
                null
            }

            for (like in post.likes) {
                if (like.idUser == user!!._id) {
                    likeButton.setImageResource(R.drawable.ic_favorite)
                    userHasLike = true
                }
            }

            likeCountTV.text = currentLikeCount.toString()



            likeButton.setOnClickListener {

                ApiService.likeService.addOrRemoveLike(
                    LikeService.LikeBody(post._id, user!!._id)
                ).enqueue(object : Callback<LikeService.LikeResponse> {
                    override fun onResponse(
                        call: Call<LikeService.LikeResponse>,
                        response: Response<LikeService.LikeResponse>
                    ) {
                        if (response.code() == 200) {
                            if (userHasLike) {
                                currentLikeCount -= 1
                                likeCountTV.text = currentLikeCount.toString()
                                likeButton.setImageResource(R.drawable.ic_favorite_border)
                            } else {
                                currentLikeCount += 1
                                likeCountTV.text = currentLikeCount.toString()
                                likeButton.setImageResource(R.drawable.ic_favorite)
                            }
                            userHasLike = !userHasLike
                        }
                    }

                    override fun onFailure(
                        call: Call<LikeService.LikeResponse>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                    }
                })
            }

            commentsButton.setOnClickListener {
                val commentsModal = CommentsModal().newInstance(post)
                commentsModal.show((itemView.context as? MainActivity)!!.supportFragmentManager, CommentsModal.TAG)
            }
        }
    }
}