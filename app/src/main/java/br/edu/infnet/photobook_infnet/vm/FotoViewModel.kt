package br.edu.infnet.photobook_infnet.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import br.edu.infnet.photobook_infnet.db.FotosRepo
import br.edu.infnet.photobook_infnet.model.Foto

class FotoViewModel(application: Application) : AndroidViewModel(application) {

    private var mRepository: FotosRepo = FotosRepo(application)

    private var mAllNotes: LiveData<List<Foto>> = mRepository.getAllNotes()

    fun getAllNotes(): LiveData<List<Foto>> {
        return mAllNotes
    }

    fun insert(foto: Foto){
        mRepository.insert(foto)
    }

    fun deleteNote(foto: Foto){
        mRepository.deleteNote(foto)
    }

    fun updateNote(foto: Foto){
        mRepository.updateNote(foto)
    }

    fun deleteNoteByTitle(title: String) {
        mRepository.deleteNoteByTitle(title)
    }
}