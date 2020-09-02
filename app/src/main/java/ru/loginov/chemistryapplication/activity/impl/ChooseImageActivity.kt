package ru.loginov.chemistryapplication.activity.impl

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_choose_image_activity.*
import org.apache.commons.io.IOUtils
import ru.loginov.chemistryapplication.ChemistryApplication.TAG
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.util.FileSystemUtils.createTmpFile
import ru.loginov.chemistryapplication.util.FileSystemUtils.getNextFileCode
import ru.loginov.chemistryapplication.util.PermissionUtils
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class ChooseImageActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_FOR_IMAGE_FROM_CAMERA = 1
        const val REQUEST_CODE_FOR_IMAGE_FROM_GALLERY = 2
        const val REQUEST_CODE_FOR_GET_COLORS = 3
        const val REQUEST_CODE_FOR_GET_PERMISSIONS = 4
        val REQUEST_CODE_TMP_FILE = getNextFileCode()

        private var prevImageUri: Uri? = null
    }

    private var imageUri: Uri? = null
    private val grantedPermission = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_choose_image_activity)

        if (PermissionUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_FOR_GET_PERMISSIONS)) {
            grantedPermission.set(true)
        }
    }

    override fun onStart() {
        super.onStart()

        if (prevImageUri != null) {
            last_image_button.visibility = View.VISIBLE
        }
    }

    fun startCamera(view: View) {

        if (!grantedPermission.get()) {
            Toast.makeText(applicationContext, "Can not get permission for read storage", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            imageUri = createTmpFile(this, REQUEST_CODE_TMP_FILE).first
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, REQUEST_CODE_FOR_IMAGE_FROM_CAMERA)
            } else {
                Toast.makeText(applicationContext, "Can not use camera", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(applicationContext, "Can not use camera", Toast.LENGTH_SHORT).show()
        }
    }

    fun startGallery(view: View) {
        if (!grantedPermission.get()) {
            Toast.makeText(applicationContext, "Can not get permission for read storage", Toast.LENGTH_SHORT).show()
            return
        }

        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, REQUEST_CODE_FOR_IMAGE_FROM_GALLERY)
    }

    fun startLastPhoto(view: View) {
        if (!grantedPermission.get()) {
            Toast.makeText(applicationContext, "Can not get permission for read storage", Toast.LENGTH_SHORT).show()
            return
        }

        if (prevImageUri == null) {
            Toast.makeText(this, "You did not chose image", Toast.LENGTH_SHORT).show()
            return
        }

        startChoseColor(prevImageUri)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_FOR_IMAGE_FROM_CAMERA -> {
                    Log.d(TAG, "onActivityResult: Get image from camera")
                    if (imageUri == null) {
                        Toast.makeText(this, "Can not get image from camera", Toast.LENGTH_SHORT).show()
                        return
                    }
                    prevImageUri = imageUri
                    startChoseColor(imageUri)
                }
                REQUEST_CODE_FOR_IMAGE_FROM_GALLERY -> {
                    Log.d(TAG, "onActivityResult: Get image from gallery")
                    if (data == null || data.data == null) {
                        Toast.makeText(this, "Can not get image from gallery", Toast.LENGTH_SHORT).show()
                        return
                    }

                    createTmpFile(this, REQUEST_CODE_TMP_FILE).also { pair ->
                        contentResolver.openInputStream(data.data!!).use { input ->
                            FileOutputStream(pair.second).use { output ->
                                IOUtils.copy(input, output)
                            }
                        }

                        prevImageUri = pair.first
                        startChoseColor(pair.first)
                    }
                }
                REQUEST_CODE_FOR_GET_COLORS -> {
                    Log.d(TAG, "onActivityResult: Get colors from image")
                    if (data == null) {
                        Toast.makeText(this, "Can not get colors from image", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_CANCELED, data)
                    }
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
                else -> throw IllegalArgumentException("Unknown request code")
            }
        } else {
            Log.d(TAG, "onActivityResult: Can not get image by request code = $requestCode")

            if (requestCode == REQUEST_CODE_FOR_GET_COLORS) {
                setResult(Activity.RESULT_CANCELED, null)
                finish()
            }
        }
    }

    private fun startChoseColor(file: Uri?) {
        requireNotNull(file) { "Can not choose color from null image" }
        val intent = Intent(applicationContext, ChooseColorsActivity::class.java)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        intent.putExtra("image", file)

        startActivityForResult(intent, REQUEST_CODE_FOR_GET_COLORS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_FOR_GET_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                grantedPermission.set(true)
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


}