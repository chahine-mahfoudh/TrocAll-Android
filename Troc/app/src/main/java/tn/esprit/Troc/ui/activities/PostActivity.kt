package tn.esprit.Troc.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tn.esprit.Troc.R
import tn.esprit.Troc.models.Post
import tn.esprit.Troc.models.User

import tn.esprit.Troc.service.ApiService
import tn.esprit.Troc.service.ChatService
import tn.esprit.Troc.service.PostService
import tn.esprit.Troc.utils.Constants

class PostActivity : AppCompatActivity() {

    private var videoView: VideoView? = null
    private var postTitleTV: TextView? = null
    private var postDescriptionTV: TextView? = null
    private var reportButton: TextView? = null
    private var progressBar: ProgressBar? = null
    private var currentUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        initialize()
    }

    private fun bindViews() {
        videoView = findViewById(R.id.videoView)
        postTitleTV = findViewById(R.id.postTitleTV)
        postDescriptionTV = findViewById(R.id.postDescriptionTV)
        reportButton = findViewById(R.id.reportButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initialize() {
        bindViews()

        val post = intent.getSerializableExtra("post") as Post?


        videoView!!.setVideoURI(Uri.parse(Constants.BASE_URL_VIDEOS + post!!.videoFilename))
        videoView!!.setOnPreparedListener { mp ->
            progressBar!!.visibility = View.GONE
            mp.start()
            val videoRatio = mp.videoWidth / mp.videoHeight.toFloat()
            val screenRatio: Float = videoView!!.width / videoView!!.height.toFloat()
            val scale = videoRatio / screenRatio
            if (scale >= 1f) {
                videoView!!.scaleX = scale
            } else {
                videoView!!.scaleY = 1f / scale
            }
        }
        videoView!!.setOnCompletionListener { mp -> mp.start() }

        postTitleTV!!.text = post.title
        postDescriptionTV!!.text = post.description

        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_SESSION, MODE_PRIVATE)
        val userData = sharedPreferences.getString("USER_DATA", null)

        val sessionUser: User? = Gson().fromJson(userData, User::class.java)
        currentUser = intent.getSerializableExtra("user") as User?

        if (currentUser == null) {
            currentUser = sessionUser
        }

        reportButton!!.setOnClickListener {
            ApiService.chatService.creerNouvelleConversation(
                ChatService.ConversationBody(
                    sessionUser!!._id,
                    currentUser!!._id
                )
            ).enqueue(
                object : Callback<ChatService.MessageResponse> {
                    override fun onResponse(
                        call: Call<ChatService.MessageResponse>,
                        response: Response<ChatService.MessageResponse>
                    ) {
                        if (response.code() == 200) {
                            finish()
                        } else {
                            println("status code is " + response.code())
                        }
                    }

                    override fun onFailure(
                        call: Call<ChatService.MessageResponse>,
                        t: Throwable
                    ) {
                        println("HTTP ERROR : ")
                    }
                }
            )
        }
    }


}