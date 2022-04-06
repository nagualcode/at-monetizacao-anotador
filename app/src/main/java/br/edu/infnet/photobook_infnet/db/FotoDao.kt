package br.edu.infnet.photobook_infnet.db

import androidx.lifecycle.LiveData
import androidx.room.*
import br.edu.infnet.photobook_infnet.model.Foto

@Dao
interface FotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notes: Foto)

    @Update
    fun updateNote(notes: Foto)

    @Delete
    fun deleteNote(notes: Foto)

    @Query("SELECT * FROM fotos")
    fun all() : LiveData<List<Foto>>

    @Query("DELETE FROM fotos WHERE titulo = :titulo")
    fun deleteNoteByTitle(titulo: String)
}