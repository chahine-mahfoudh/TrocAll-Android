package tn.esprit.Troc.models

import java.io.Serializable
import java.util.*

data class Like(
    var date: Date,
    var idUser: String?,
    var idPost: String?
) : Serializable
