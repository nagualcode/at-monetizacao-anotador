package br.edu.infnet.photobook_infnet.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos"
)
class Foto (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val titulo: String = "",
    val data: String = "",
    val foto: String = "",
    val localizacao: String = "",
    val texto: String = ""
)


