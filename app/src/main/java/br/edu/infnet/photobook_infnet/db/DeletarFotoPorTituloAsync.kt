package br.edu.infnet.photobook_infnet.db

import android.os.AsyncTask

class DeletarFotoPorTituloAsync (
    private val mAsyncTaskDao: FotoDao,
) : AsyncTask<String, Unit, Unit >()
{
    override fun doInBackground(vararg params: String?) {
        mAsyncTaskDao.deleteNoteByTitle(params[0]!!)

    }
}