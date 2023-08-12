package tn.esprit.Troc.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tn.esprit.Troc.R
import tn.esprit.Troc.adapters.ConversationAdapter
import tn.esprit.Troc.models.Conversation
import tn.esprit.Troc.models.User
import tn.esprit.Troc.service.ApiService
import tn.esprit.Troc.service.ChatService
import tn.esprit.Troc.utils.Constants

class ChatFragment : Fragment() {

    private var conversationsRV: RecyclerView? = null
    private var shimmerFrameLayout: ShimmerFrameLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // VIEW BINDING
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout)
        conversationsRV = view.findViewById(R.id.conversationsRV)

        shimmerFrameLayout!!.startShimmer();
        conversationsRV!!.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val sharedPreferences =
            requireContext().getSharedPreferences(Constants.SHARED_PREF_SESSION, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString("USER_DATA", null)

        val user: User? = if (userData != null) {
            Gson().fromJson(userData, User::class.java)
        } else {
            null
        }

        ApiService.chatService.getMyConversations(
            ChatService.MyConversationsBody(
                user!!._id
            )
        ).enqueue(
                object : Callback<ChatService.ConversationsResponse> {
                    override fun onResponse(
                        call: Call<ChatService.ConversationsResponse>,
                        response: Response<ChatService.ConversationsResponse>
                    ) {
                        if (response.code() == 200) {
                            conversationsRV!!.adapter =
                                ConversationAdapter(response.body()?.conversations as MutableList<Conversation>)

                            shimmerFrameLayout!!.stopShimmer()
                            shimmerFrameLayout!!.visibility = View.GONE
                        } else {
                            println("status code is " + response.code())
                        }
                    }

                    override fun onFailure(
                        call: Call<ChatService.ConversationsResponse>,
                        t: Throwable
                    ) {
                        println("HTTP ERROR")
                        t.printStackTrace()
                    }
                }
            )

        return view
    }
}