package com.capstone.skinpal.ui.history

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.capstone.skinpal.BuildConfig
import com.capstone.skinpal.ui.ViewModelFactory
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID
import com.capstone.skinpal.R
import com.capstone.skinpal.data.Result
import com.capstone.skinpal.data.UserPreference
import com.capstone.skinpal.databinding.ActivityWeeklyCameraBinding
import com.capstone.skinpal.di.Injection
import com.capstone.skinpal.ui.camera.getImageUri
import com.capstone.skinpal.ui.camera.reduceFileImage
import com.capstone.skinpal.ui.camera.uriToFile

class CameraWeeklyActivity : AppCompatActivity() {
    private var _binding: ActivityWeeklyCameraBinding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null
    private val cameraWeeklyViewModel by viewModels<CameraWeeklyViewModel> {
        ViewModelFactory(Injection.provideRepository(this))
    }

    private val cropImageLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val croppedImageUri = UCrop.getOutput(result.data!!)
            if (croppedImageUri != null) {
                currentImageUri = croppedImageUri
                showImage()
            } else {
                showToast("Cropping failed.")
            }
        } else {
            showToast("Cropping was cancelled.")
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (!granted) {
                Toast.makeText(this, getString(R.string.permission_request_denied), Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWeeklyCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val week = intent.getStringExtra("WEEK") ?: "pekan1" // Use a default string value
        title = "Week $week"

        val resultFragment = ResultFragment()

        val bundle = Bundle()
        bundle.putString("week", week)
        resultFragment.arguments = bundle

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        currentImageUri = savedInstanceState?.getParcelable("CURRENT_IMAGE_URI")
        val imageUriString = intent.getStringExtra("IMAGE_URI")

        loadPreviewImage(week)
        /*if (imageUriString.isNullOrEmpty()) {
            loadPreviewImage(week)
        } else {
            displaySavedImage(imageUriString)
            disableButtons()
        }*/

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.showResult.setOnClickListener {
            // Hapus pengecekan currentImageUri
            val userPreference = UserPreference(this)
            val userId = userPreference.getSession().user ?: getString(R.string.default_user)
            val week = intent.getStringExtra("WEEK") ?: "pekan1"

            // Langsung tampilkan result
            showImageInfo()
        }

        binding.analyzeButton.setOnClickListener {
            if (currentImageUri == null) {
                showToast("No image to analyze. Please capture or select an image.")
                return@setOnClickListener
            }

            val userPreference = UserPreference(this)
            val userId = userPreference.getSession().user ?: getString(R.string.default_user)
            val week = intent.getStringExtra("WEEK") ?: "pekan1"
            currentImageUri?.let { uri ->
                //saveImage(uri.toString(), week)
                analyzeImage(uri.toString(), week)
            } ?: showToast("Failed to save image. No image captured.")
        }

    }

    private fun loadPreviewImage(week: String) {
        val userPreference = UserPreference(this)
        val userId = userPreference.getSession().user ?: getString(R.string.default_user)

        Log.d("CameraWeeklyActivity", "Loading preview for userId: $userId, week: $week")

        cameraWeeklyViewModel.getAnalysis(userId, week).observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    showLoading(false)
                    if (result.data != null && result.data.publicUrl.isNotEmpty()) {
                        Log.d("CameraWeeklyActivity", "Found image URL: ${result.data.publicUrl}")

                        Glide.with(this)
                            .load(result.data.publicUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .error(R.drawable.ic_place_holder)
                            .into(binding.previewImageView)

                        // Update UI untuk menampilkan showResult
                        binding.apply {
                            galleryButton.visibility = GONE
                            cameraButton.visibility = GONE
                            showResult.apply {
                                visibility = VISIBLE
                                isEnabled = true
                            }
                            analyzeButton.visibility = GONE
                            binding.uploadButton.apply {
                                visibility = View.VISIBLE
                                setOnClickListener {
                                    resetToInitialState() // Panggil fungsi baru ini
                                }
                            }
                        }
                    } else {
                        binding.previewImageView.setImageResource(R.drawable.ic_place_holder)
                        resetButtons()
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    Log.e("CameraWeeklyActivity", "Error loading preview: ${result.error}")
                    binding.previewImageView.setImageResource(R.drawable.ic_place_holder)
                    resetButtons()
                }
                is Result.Loading -> showLoading(true)
            }
        }
    }

    private fun resetToInitialState() {
        // Reset image view
        binding.previewImageView.setImageResource(R.drawable.ic_place_holder)

        // Reset URI
        currentImageUri = null

        // Reset button states
        binding.apply {
            galleryButton.apply {
                isEnabled = true
                visibility = View.VISIBLE
            }
            cameraButton.apply {
                isEnabled = true
                visibility = View.VISIBLE
            }
            analyzeButton.apply {
                visibility = View.VISIBLE
                isEnabled = true
            }
            showResult.visibility = View.GONE
            uploadButton.visibility = View.GONE
        }

        showToast("Ready to upload a new image")
    }


    // Add this new function to handle button visibility
    private fun toggleAnalysisButtons(hasAnalysis: Boolean) {
        binding.apply {
            if (hasAnalysis) {
                showResult.visibility = View.VISIBLE
                analyzeButton.visibility = View.GONE
            } else {
                showResult.visibility = View.GONE
                analyzeButton.visibility = View.VISIBLE
            }
        }
    }

    // Update analyzeImage function to toggle buttons after successful analysis
    private fun analyzeImage(imageUriString: String, week: String) {
        val userPreference = UserPreference(this)
        val session = userPreference.getSession()

        val user_id = session.user ?: run {
            showToast("User ID not found")
            return
        }

        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()

            if (BuildConfig.DEBUG) {
                Log.d("CameraWeeklyActivity", """
                Uploading image:
                userId: $user_id
                week: $week
                token: ${session.token!!.take(10)}...
                file: ${imageFile.name}
            """.trimIndent())
            }

            cameraWeeklyViewModel.analyzeImage(
                imageFile = imageFile,
                user_id = user_id,
                week = week
            ).observe(this) { result ->
                when (result) {
                    is Result.Loading -> showLoading(true)
                    is Result.Success -> {
                        showLoading(false)
                        showImage()
                        showImageInfo()
                        disableButtons() // Gunakan disableButtons() setelah analisis berhasil
                    }
                    is Result.Error -> {
                        showLoading(false)
                        if (result.error.contains("authorized", ignoreCase = true)) {
                            showToast("Session expired. Please login again")
                        } else {
                            showToast(result.error)
                        }
                        resetButtons() // Gunakan resetButtons() saat error
                    }
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun displaySavedImage(imageUri: String) {
        Glide.with(this)
            .load(imageUri)
            .into(binding.previewImageView)
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            currentImageUri?.let {
                showImage()
                startCrop(it)
            }
        } else {
            currentImageUri = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("CURRENT_IMAGE_URI", currentImageUri)
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(this.cacheDir, "cropped_image_${UUID.randomUUID()}.jpg"))
        val uCropIntent = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(224, 224)
            .getIntent(this)
        cropImageLauncher.launch(uCropIntent)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            startCrop(uri)
        } else {
            showToast("No image selected.")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Glide.with(this).load(it).into(binding.previewImageView)
        }
    }

    private fun showImageInfo() {
        val week = intent.getStringExtra("WEEK") ?: "pekan1"
        val userPreference = UserPreference(this)
        val userId = userPreference.getSession().user ?: getString(R.string.default_user)

        // Tambahkan log untuk debugging
        Log.d("CameraWeeklyActivity", "Showing image info for userId: $userId, week: $week")

        val bottomSheet = ResultFragment().apply {
            arguments = Bundle().apply {
                putString("userId", userId)
                putString("week", week)
            }
        }

        try {
            bottomSheet.show(supportFragmentManager, "ResultFragment")
        } catch (e: Exception) {
            Log.e("CameraWeeklyActivity", "Error showing bottom sheet: ${e.message}")
            showToast("Error showing results")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun disableButtons() {
        binding.apply {
            galleryButton.isEnabled = false
            cameraButton.isEnabled = false
            analyzeButton.visibility = View.GONE
            showResult.visibility = View.VISIBLE
            uploadButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    currentImageUri = null
                    previewImageView.setImageResource(R.drawable.ic_place_holder)
                    resetButtons()
                    showToast("Ready to upload a new image.")
                    showLoading(isLoading = false)
                }
            }
        }
    }

    private fun resetButtons() {
        binding.apply {
            galleryButton.isEnabled = true
            cameraButton.isEnabled = true
            analyzeButton.visibility = View.VISIBLE
            showResult.visibility = View.GONE
            uploadButton.visibility = View.GONE
        }
    }

    private fun enableButtons() {
        binding.galleryButton.isEnabled = true
        binding.cameraButton.isEnabled = true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
