package br.edu.infnet.photobook_infnet.db

import android.os.AsyncTask
import br.edu.infnet.photobook_infnet.Util.DELETE
import br.edu.infnet.photobook_infnet.Util.INSERT
import br.edu.infnet.photobook_infnet.Util.UPDATE
import br.edu.infnet.photobook_infnet.model.Foto

class InserirFotoPorTituloAsync (
    private val mAsyncTaskDao: FotoDao,
    private val DBOperation: Int
) : AsyncTask<Foto, Void, Void>()
{
    override fun doInBackground(vararg params: Foto): Void? {
        when (DBOperation) {
            INSERT -> mAsyncTaskDao.insert(params[0])
            UPDATE -> mAsyncTaskDao.updateNote(params[0])
            DELETE -> mAsyncTaskDao.deleteNote(params[0])
        }
        return null
    }
}