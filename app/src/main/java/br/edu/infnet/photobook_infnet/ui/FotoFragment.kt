package br.edu.infnet.photobook_infnet.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.infnet.photobook_infnet.R
import br.edu.infnet.photobook_infnet.Util.EXTRA_DATA
import br.edu.infnet.photobook_infnet.Util.EXTRA_ID
import br.edu.infnet.photobook_infnet.Util.EXTRA_IMAGEM
import br.edu.infnet.photobook_infnet.Util.EXTRA_LOCAL
import br.edu.infnet.photobook_infnet.Util.EXTRA_TEXTO
import br.edu.infnet.photobook_infnet.Util.EXTRA_TITULO
import br.edu.infnet.photobook_infnet.model.Foto
import br.edu.infnet.photobook_infnet.vm.FotoViewModel
import br.edu.infnet.photobook_infnet.vm.UsuarioViewModel
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_foto.*

class FotoFragment : Fragment() {

    private lateinit var usuarioViewModel: UsuarioViewModel
    private lateinit var fotoViewModel: FotoViewModel
    private lateinit var auth: FirebaseAuth
    private val ADD_REQUEST_CODE = 71
    private var adapter: FotoAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_foto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
                act -> usuarioViewModel = ViewModelProviders.of(act)
            .get(UsuarioViewModel::class.java)
        }

        fotoViewModel = ViewModelProviders.of(this).get(FotoViewModel::class.java)
        adapter = FotoAdapter() {
                partItem: Foto -> partItemClicked(partItem)
        }

        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        fillUserData()
        subscribe()
        setupListeners()
        bannerAd()
    }

    private fun setupRecyclerView() {
        rv_notes.layoutManager = LinearLayoutManager(activity)
        rv_notes.adapter = adapter
    }

    private fun subscribe() {
        fotoViewModel.getAllNotes().observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                tv_lista_vazia.visibility = View.VISIBLE
            } else {
                tv_lista_vazia.visibility = View.GONE
            }
            adapter!!.setNotes(it)
        })
    }

    private fun setupListeners() {
        fab_add_note.setOnClickListener {
            val intent = Intent(context, AnotarFotoActivity::class.java)
            startActivityForResult(intent, ADD_REQUEST_CODE)
        }
        btn_versao_premium.setOnClickListener {
            layout_adView.visibility = View.GONE
            Toast.makeText(context, "Premium!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun partItemClicked(foto: Foto) {
        val intent = Intent(context, AnotarFotoActivity::class.java)
        intent.putExtra(EXTRA_ID, foto.id)
        intent.putExtra(EXTRA_TITULO, foto.titulo)
        intent.putExtra(EXTRA_DATA, foto.data)
        intent.putExtra(EXTRA_IMAGEM, foto.foto)
        intent.putExtra(EXTRA_LOCAL, foto.localizacao)
        intent.putExtra(EXTRA_TEXTO, foto.texto)

        startActivityForResult(intent, ADD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK){
                if (requestCode == ADD_REQUEST_CODE) {
                    data.let {
                        val id = it!!.getIntExtra(EXTRA_ID, 0)
                        val titulo = it.getStringExtra(EXTRA_TITULO)
                        val date = it.getStringExtra(EXTRA_DATA)
                        val imagem = it.getStringExtra(EXTRA_IMAGEM)!!
                        val localizacao = it.getStringExtra(EXTRA_LOCAL)
                        val texto = it.getStringExtra(EXTRA_TEXTO)

                        val note = Foto(
                            id,
                            titulo!!,
                            date!!,
                            imagem,
                            localizacao!!,
                            texto!!
                        )
                        if (id == 0) {
                            fotoViewModel.insert(note)
                        } else {
                            fotoViewModel.updateNote(note)
                        }
                    }
                }
            }
    }

    private fun fillUserData() {
        usuarioViewModel.nome.observe(viewLifecycleOwner, Observer {
            if(it != null){
                tv_nome_user.text = it.toString()
            }
        })
        usuarioViewModel.email.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                tv_email_user.text = it.toString()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater!!.inflate(R.menu.overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_logout -> {
                auth.signOut()
                findNavController().navigate(R.id.action_noteFragment_to_signInFragment, null)
            }
        }
        return true
    }

    private fun bannerAd() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

}