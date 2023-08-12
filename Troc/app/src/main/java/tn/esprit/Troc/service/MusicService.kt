package tn.esprit.Troc.service

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import tn.esprit.Troc.models.Music

interface MusicService {

    data class MusicResponse(
        @SerializedName("music")
        val music: Music
    )

    data class MusicsResponse(
        @SerializedName("musics")
        val musics: List<Music>
    )

    @GET("/music")
    fun getAll(): Call<MusicsResponse>
}