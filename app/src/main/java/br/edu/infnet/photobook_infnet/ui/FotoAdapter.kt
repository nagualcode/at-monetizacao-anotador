package br.edu.infnet.photobook_infnet.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.infnet.photobook_infnet.R
import br.edu.infnet.photobook_infnet.model.Foto

class FotoAdapter(
    val onClickListener: (Foto) -> Unit
)
    : RecyclerView.Adapter<FotoAdapter.NoteViewHolder>() {

    var notesList: List<Foto>? = null

    class NoteViewHolder(itemView: View):
        RecyclerView.ViewHolder(itemView) {

        var titulo: TextView = itemView.findViewById(R.id.tv_foto_titulo)

        var update: TextView = itemView.findViewById(R.id.tv_foto_titulo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.fotos_card,
                parent,
                false
            )
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList!![position]

        holder.titulo.text = note.titulo


        holder.update.setOnClickListener {
            onClickListener(note)
        }
    }

    override fun getItemCount(): Int {
        return if (notesList != null){
            notesList!!.size
        } else {
            0
        }
    }

    fun setNotes(fotos: List<Foto>){
        notesList = fotos
        notifyDataSetChanged()
    }
}