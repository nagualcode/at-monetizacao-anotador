package br.edu.infnet.photobook_infnet.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.DateFormat
import java.util.*

class UsuarioViewModel : ViewModel() {
    val nome = MutableLiveData<String>().apply { value = "" }
    val email = MutableLiveData<String>().apply { value = "" }
    val dataAtual = MutableLiveData<String>().apply { value = getDate() }


    private fun getDate(): String {
        val date = Calendar.getInstance().time
        return DateFormat.getDateInstance(DateFormat.LONG).format(date)
    }

}