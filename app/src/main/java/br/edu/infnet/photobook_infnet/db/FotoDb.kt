package br.edu.infnet.photobook_infnet.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.edu.infnet.photobook_infnet.model.Foto

@Database (
    entities = [Foto::class],
    version = 1
)
abstract class FotoDb: RoomDatabase() {

    abstract fun fotoDao(): FotoDao

    companion object {
        @Volatile
        private var instance: FotoDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            FotoDb::class.java,
            "FotoDB")
            .build()
    }

}