package com.lena.pasletp1.user

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.snackbar.Snackbar
import com.google.modernstorage.mediastore.FileType
import com.google.modernstorage.mediastore.MediaStoreRepository
import com.google.modernstorage.mediastore.SharedPrimary
import com.lena.pasletp1.R
import com.lena.pasletp1.databinding.ActivityUserInfoBinding
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding

    private val viewModel: UserInfoViewModel by viewModels()

    private val mediaStore by lazy { MediaStoreRepository(this) }

    private val permissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { accepted ->
            if (accepted.filterKeys { it in permissions }.all { it.value }) {
                launchCamera()
            }
            else showExplanation()
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { accepted ->
            if (accepted) handleImage()
            else Snackbar.make(binding.root, "Failed!", Snackbar.LENGTH_LONG).show()
        }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { accepted ->
        if (accepted != null) {
            photoUri = accepted
            handleImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.takePictureButton.setOnClickListener {
            launchCameraWithPermission()
        }

        binding.uploadImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.userUpdateButton.setOnClickListener {
            viewModel.update(UserInfo(
                email = binding.userEmail.text.toString(),
                firstName = binding.userFirstName.text.toString(),
                lastName = binding.userLastName.text.toString(),
                avatar = null
            ))
        }

        viewModel.collect { userInfo ->
            binding.imageView.load(userInfo.avatar) {
                transformations(CircleCropTransformation())
                error(R.drawable.ic_launcher_background)
            }
            binding.userLastName.setText(userInfo.lastName)
            binding.userFirstName.setText(userInfo.firstName)
            binding.userEmail.setText(userInfo.email)
        }

        viewModel.refresh()
    }
    private fun launchCameraWithPermission() {
        val camPermission = Manifest.permission.CAMERA
        val permissionStatus = checkSelfPermission(camPermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)

        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        when {
            mediaStore.canWriteSharedEntries() && isAlreadyAccepted -> launchCamera()
            isExplanationNeeded -> showExplanation()
            else -> cameraPermissionLauncher.launch(arrayOf(camPermission, storagePermission))
        }
    }

    private fun showExplanation() {
        // ici on construit une pop-up système (Dialog) pour expliquer la nécessité de la demande de permission
        AlertDialog.Builder(this)
            .setMessage(":pleading_face: On a besoin de la caméra, vraiment! :point_right::point_left:")
            .setPositiveButton("Bon, ok") { _, _ -> launchAppSettings() }
            .setNegativeButton("Nope") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun launchAppSettings() {
        // Cet intent permet d'ouvrir les paramètres de l'app (pour modifier les permissions déjà refusées par ex)
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        // ici pas besoin de vérifier avant car on vise un écran système:
        startActivity(intent)
    }

    private lateinit var photoUri: Uri
    private fun launchCamera() {
        lifecycleScope.launch {
            photoUri = mediaStore.createMediaUri(
                filename = "picture-${UUID.randomUUID()}.jpg",
                type = FileType.IMAGE,
                location = SharedPrimary
            ).getOrThrow()
            cameraLauncher.launch(photoUri)
        }
    }

    private fun handleImage() {
        binding.imageView.load(photoUri) {
            transformations(CircleCropTransformation())
            error(R.drawable.ic_launcher_background)
        }
        viewModel.updateAvatar(convert(photoUri))
    }

    private fun convert(uri: Uri): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = contentResolver.openInputStream(uri)!!.readBytes().toRequestBody()
        )
    }
}