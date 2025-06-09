package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GaleriaFotosActivity : AppCompatActivity() {

    private lateinit var fotosRecyclerView: RecyclerView
    private lateinit var fotoAdapter: FotoAdapter
    private val fotosList = mutableListOf<String>()
    private var currentPhotoPath: String? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                // O caminho do arquivo já está salvo em currentPhotoPath
                fotosList.add(path)
                fotoAdapter.notifyItemInserted(fotosList.size - 1)
                Toast.makeText(this, "Foto adicionada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                val file = createImageFile() // Cria um novo arquivo no armazenamento interno
                val fos = FileOutputStream(file)
                inputStream?.copyTo(fos)
                inputStream?.close()
                fos.close()
                fotosList.add(file.absolutePath)
                fotoAdapter.notifyItemInserted(fotosList.size - 1)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Erro ao copiar a imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeria_fotos)

        fotosRecyclerView = findViewById(R.id.fotosRecyclerView)
        val backButton: ImageButton = findViewById(R.id.backButtonGaleria)
        val grayCircleButton: ImageButton = findViewById(R.id.grayCircleButton) // Câmera
        val pickFromGalleryButton: ImageButton = findViewById(R.id.pickFromGalleryButton) // Galeria

        // Pega a lista de fotos existente da tela anterior
        intent.getStringArrayListExtra("photos")?.let {
            fotosList.addAll(it)
        }

        fotoAdapter = FotoAdapter(this, fotosList,
            onPhotoClick = { path ->
                val file = File(path)
                val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(viewIntent)
            },
            onPhotoLongClick = { position ->
                val pathToRemove = fotosList[position]
                fotosList.removeAt(position)
                fotoAdapter.notifyItemRemoved(position)
                File(pathToRemove).delete() // Apaga o arquivo físico
                Toast.makeText(this, "Foto removida.", Toast.LENGTH_SHORT).show()
            }
        )
        fotosRecyclerView.adapter = fotoAdapter
        fotosRecyclerView.layoutManager = GridLayoutManager(this, 3)

        backButton.setOnClickListener {
            devolverResultados()
        }

        grayCircleButton.setOnClickListener {
            // Lógica para permissão e captura de câmera
            requestCameraAndLaunch()
        }

        pickFromGalleryButton.setOnClickListener {
            // Lógica para permissão e seleção da galeria
            requestStorageAndLaunch()
        }
    }

    private fun devolverResultados() {
        val resultIntent = Intent().apply {
            putStringArrayListExtra("photos", ArrayList(fotosList))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        devolverResultados()
        super.onBackPressed()
    }

    private fun requestCameraAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun requestStorageAndLaunch() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }
            else -> {
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) launchCamera() else Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show()
    }

    private val storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) pickImageLauncher.launch("image/*") else Toast.makeText(this, "Permissão de armazenamento negada.", Toast.LENGTH_SHORT).show()
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                photoFile
            )
            currentPhotoPath = photoFile.absolutePath
            takePictureLauncher.launch(photoURI)
        } catch (ex: IOException) {
            Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}