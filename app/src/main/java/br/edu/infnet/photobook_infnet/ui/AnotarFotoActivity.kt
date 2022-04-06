package br.edu.infnet.photobook_infnet.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import br.edu.infnet.photobook_infnet.R
import br.edu.infnet.photobook_infnet.Util.EXTRA_DATA
import br.edu.infnet.photobook_infnet.Util.EXTRA_ID
import br.edu.infnet.photobook_infnet.Util.EXTRA_IMAGEM
import br.edu.infnet.photobook_infnet.Util.EXTRA_LOCAL
import br.edu.infnet.photobook_infnet.Util.EXTRA_TEXTO
import br.edu.infnet.photobook_infnet.Util.EXTRA_TITULO
import br.edu.infnet.photobook_infnet.model.Foto
import br.edu.infnet.photobook_infnet.vm.FotoViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_foto.*
import java.io.*
import java.text.DateFormat
import java.util.*


class AnotarFotoActivity : AppCompatActivity() {

    private lateinit var fotoViewModel: FotoViewModel
    private val REQUEST_PERMISSION_CODE = 1009
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val GRANTED = PackageManager.PERMISSION_GRANTED
    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val CAMERA  = Manifest.permission.CAMERA
    private var LATITUDE = ""
    private var LONGITUDE = ""
    private var LOCATION = ""
    private var imageBitmap: Bitmap? = null
    private var encodedImageString = ""
    private var encFileText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_foto)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.let {
                act -> fotoViewModel = ViewModelProviders.of(act)
            .get(FotoViewModel::class.java)
        }

        fillFieldsData()
        setupListeners()
        getLocation()
        changeImage()
    }

    private fun setupListeners() {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        btn_save_note.setOnClickListener {
            val retornoIntent = Intent()

            val titulo = add_titulo_input.text.toString()
            val data = add_data_input.text.toString()
            val foto = encodedImageString
            val localizacao = LOCATION
            val texto = add_text_input.text.toString()

            writeInAInternalFile(titulo, id, texto)

            if (encodedImageString.isNotEmpty() && titulo.isNotEmpty() &&
                    data.isNotEmpty() && texto.isNotEmpty()) {
                retornoIntent.putExtra(EXTRA_ID, id)
                retornoIntent.putExtra(EXTRA_TITULO, titulo)
                retornoIntent.putExtra(EXTRA_DATA, data)
                retornoIntent.putExtra(EXTRA_IMAGEM, foto)
                retornoIntent.putExtra(EXTRA_LOCAL, localizacao)
                retornoIntent.putExtra(EXTRA_TEXTO, encFileText)

                setResult(Activity.RESULT_OK, retornoIntent)
                finish()
            } else {
                showSnackbar("Preencha todos campos!")
            }
        }
    }

    private fun fillFieldsData(){
        val titulo = intent.getStringExtra(EXTRA_TITULO)

        if (titulo != null) {
            val data = intent.getStringExtra(EXTRA_DATA)
            encodedImageString = intent.getStringExtra(EXTRA_IMAGEM)!!
            LOCATION = intent.getStringExtra(EXTRA_LOCAL)!!
            encFileText = intent.getStringExtra(EXTRA_TEXTO)!!
            val texto = readInAInternalFile(encFileText)

            btn_save_note.text = getString(R.string.update)
            add_titulo_input.setText(titulo.toString())
            add_data_input.setText(data.toString())
            add_text_input.setText(texto)
            tv_add_location.text = LOCATION

            val bytarray: ByteArray = Base64.decode(encodedImageString, Base64.DEFAULT)
            val bmimage = BitmapFactory.decodeByteArray(
                bytarray, 0,
                bytarray.size
            )

            img_add.setImageBitmap(bmimage)
        }
    }

    private fun changeImage() {
        btn_alterar_imagem.setOnClickListener {
            when {
                checkSelfPermission(CAMERA) == GRANTED -> captureImage()
                shouldShowRequestPermissionRationale(CAMERA) -> showDialogPermission(
                    "É preciso liberar o acesso à câmera!",
                    arrayOf(CAMERA)
                )
                else -> requestPermissions(
                    arrayOf(CAMERA),
                    REQUEST_IMAGE_CAPTURE
                )
            }
        }
    }

    private fun captureImage(){
        val capturaImagemIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(capturaImagemIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: Exception) {
            showSnackbar("${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                try {
                    imageBitmap = data!!.extras!!["data"] as Bitmap?
                    img_add.setImageBitmap(imageBitmap)

                    val baos = ByteArrayOutputStream()
                    imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val b: ByteArray = baos.toByteArray()
                    encodedImageString = Base64.encodeToString(b, Base64.DEFAULT)

                } catch (e: Exception) {
                    showSnackbar("${e.message}")
                }
            }
        }
    }

    private fun getEncFile(nome: String): EncryptedFile{
        val masterKeyAlias: String =
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val file = File(applicationContext.filesDir, nome)

        return EncryptedFile.Builder(
            file,
            applicationContext,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
            .build()
    }


    private fun writeInAInternalFile(title: String, id: Int, text: String){
        val date = getDate()

        val nomeArquivoTxt = "${title}_$date.txt"
        encFileText = nomeArquivoTxt

        deleteInAInternalFile(nomeArquivoTxt, id, title)

        val encryptedOut: FileOutputStream =
            getEncFile(nomeArquivoTxt).openFileOutput()
        val pw = PrintWriter(encryptedOut)
        pw.println(text)
        pw.flush()
        encryptedOut.close()
    }

    private fun readInAInternalFile(file_name: String): String {
        var texto = ""
        val encryptedIn: FileInputStream =
            getEncFile(file_name).openFileInput()
        val br = BufferedReader(InputStreamReader(encryptedIn))
        br.lines().forEach {
                t ->  texto = t.toString()
        }
        encryptedIn.close()
        return texto
    }

    private fun deleteInAInternalFile(nome: String, id: Int, title: String) {
        val file = File(applicationContext.filesDir, nome)

        if (file.exists()) {
            file.delete()
            if (id == 0) {
                fotoViewModel.deleteNoteByTitle(title)
            }
        }
    }

    private fun getCurrentLocation() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
        if (!isGPSEnabled && !isNetworkEnabled) {
            Log.d("Permissao", "Ative os serviços necessários")
        } else {
            when {
                isGPSEnabled -> {
                    try {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2000L, 0f, locationListener)
                    } catch(ex: SecurityException) {
                        Log.d("Permissao", "Security Exception")
                    }
                }
                isNetworkEnabled -> {
                    try {
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            2000L, 0f, locationListener)
                    } catch(ex: SecurityException) {
                        Log.d("Permissao", "Security Exception")
                    }
                }
            }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            LATITUDE = location.latitude.toString()
            LONGITUDE = location.longitude.toString()
            LOCATION = "LAT: $LATITUDE LOG: $LONGITUDE"
            tv_add_location.text = LOCATION
        }

        override fun onProviderDisabled(provider: String) {
            showSnackbar("$provider off")
        }

        override fun onProviderEnabled(provider: String) {
            showSnackbar("$provider on")
        }
    }

    fun getLocation() {
        val permissionACL = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val permissionAFL = checkSelfPermission(FINE_LOCATION)

        if (permissionACL == GRANTED || permissionAFL == GRANTED)
            getCurrentLocation()
        else {
            if (shouldShowRequestPermissionRationale(FINE_LOCATION))
                showDialogPermission(
                    "É necessário conceder as permissões " +
                            "para todos os recurso disponíveis.",
                    arrayOf(FINE_LOCATION)
                )
            else
                requestPermissions(arrayOf(FINE_LOCATION),
                    REQUEST_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                permissions.forEachIndexed { index, permission ->
                    if (grantResults[index] == GRANTED)
                        when (permission) {
                            FINE_LOCATION -> getCurrentLocation()
                        }
                    }
                }
            }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showDialogPermission(
        message: String, permissions: Array<String>
    ) {
        val alertDialog = AlertDialog
            .Builder(this)
            .setTitle("Permissões")
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                requestPermissions(
                    permissions,
                    REQUEST_PERMISSION_CODE)
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        alertDialog.show()
    }

    private fun getDate(): String {
        val date = Calendar.getInstance().time
        return DateFormat.getDateInstance(DateFormat.LONG).format(date)
    }

    private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar
            .make(
                root_layout,
                message,
                duration
            ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_add_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_delete -> {
                val titulo = intent.getStringExtra(EXTRA_TITULO)
                val id = intent.getIntExtra(EXTRA_ID, 0)
                val retornoIntent = Intent()
                if (titulo != null) {
                    deleteInAInternalFile(encFileText, id, titulo)
                    actionDeleteNote()
                    setResult(RESULT_CANCELED, retornoIntent)
                    finish()
                }
            }
        }
        return false
    }

    private fun actionDeleteNote() {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val titulo = intent.getStringExtra(EXTRA_TITULO)
        val data = intent.getStringExtra(EXTRA_DATA)
        val imagem = intent.getStringExtra(EXTRA_IMAGEM)
        val localizacao = intent.getStringExtra(EXTRA_LOCAL)
        val texto = intent.getStringExtra(EXTRA_TEXTO)

        val note = Foto(
            id,
            titulo!!,
            data!!,
            imagem!!,
            localizacao!!,
            texto!!
        )
        fotoViewModel.deleteNote(note)
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
        return true
    }
}