package br.edu.infnet.photobook_infnet.db

import android.app.Application

import androidx.lifecycle.LiveData
import br.edu.infnet.photobook_infnet.Util.DELETE
import br.edu.infnet.photobook_infnet.Util.INSERT
import br.edu.infnet.photobook_infnet.Util.UPDATE
import br.edu.infnet.photobook_infnet.model.Foto

class FotosRepo(application: Application) {

    var fotoDao: FotoDao

    var mAllNotes: LiveData<List<Foto>>

    init {
        val db: FotoDb = FotoDb.invoke(application)
        fotoDao = db.fotoDao()
        mAllNotes = fotoDao.all()
    }

    fun getAllNotes(): LiveData<List<Foto>> {
        return mAllNotes
    }

    fun insert(foto: Foto) {
        InserirFotoPorTituloAsync(fotoDao, INSERT).execute(foto)
    }

    fun deleteNote(foto: Foto) {
        InserirFotoPorTituloAsync(fotoDao, DELETE).execute(foto)
    }

    fun updateNote(foto: Foto) {
        InserirFotoPorTituloAsync(fotoDao, UPDATE).execute(foto)
    }

    fun deleteNoteByTitle(title: String) {
        DeletarFotoPorTituloAsync(fotoDao).execute(title)
    }
}
