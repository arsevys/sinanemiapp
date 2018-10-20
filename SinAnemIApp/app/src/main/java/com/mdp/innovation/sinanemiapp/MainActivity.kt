package com.mdp.innovation.sinanemiapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.os.Environment.getExternalStorageDirectory
import android.content.Intent
import android.net.Uri
import android.os.Environment
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.widget.Toast
import android.provider.MediaStore.Images.Media.getBitmap
import android.graphics.Bitmap
import android.app.Activity
import android.provider.MediaStore.Images.Media.getBitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException
import java.io.InputStream
import android.provider.MediaStore.Images.Media.getBitmap
import android.content.DialogInterface
import android.Manifest.permission
import android.Manifest.permission.WRITE_CONTACTS
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import com.theartofdev.edmodo.cropper.CropImage
import android.R.attr.data
import android.R.attr.data








class MainActivity : AppCompatActivity() {
    val CAMERA_PHOTO = 111
    val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124
    private var imageToUploadUri: Uri? = null
    var cantPhoto = 0

    var photo1: Bitmap? = null //FOTO 1 EN BITMAP
    var photo2: Bitmap? = null //FOTO 2 EN BITMAP
    var photo3: Bitmap? = null //FOTO 3 EN BITMAP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onClickListener()
        insertDummyContactWrapper()
    }

    fun captureCameraImage() {
        val chooserIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val f = File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg")
        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
        imageToUploadUri = Uri.fromFile(f)
        startActivityForResult(chooserIntent, CAMERA_PHOTO)
    }


    fun onClickListener(){
        btnCam.setOnClickListener {
            captureCameraImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CAMERA_PHOTO && resultCode === Activity.RESULT_OK) {
            if (imageToUploadUri != null) {
                var selectedImage = imageToUploadUri
                contentResolver.notifyChange(selectedImage, null)
                val reducedSizeBitmap = getBitmap(imageToUploadUri!!.path)
                startCropImageActivity(imageToUploadUri!!)
                /*                if (reducedSizeBitmap != null) {
                    cantPhoto++
                    when(cantPhoto){
                        1 -> {
                            iviPhoto1.rotation = 360f
                            iviPhoto1.setImageBitmap(reducedSizeBitmap)
                        }
                        2 -> {
                            iviPhoto2.rotation = 360f
                            iviPhoto2.setImageBitmap(reducedSizeBitmap)
                        }
                        3 -> {
                            iviPhoto3.rotation = 360f
                            iviPhoto3.setImageBitmap(reducedSizeBitmap)
                            btnCam.isEnabled = false
                        }
                    }
                } else {
                    Toast.makeText(this, "Error while capturing Image_", Toast.LENGTH_LONG).show()
                }*/
            } else {
                Toast.makeText(this, "Error while capturing Image", Toast.LENGTH_LONG).show()
            }
        }else if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === Activity.RESULT_OK) {
                val resultUri = result.uri
                cantPhoto++
                when(cantPhoto){
                    1 -> {
                        photo1 = getBitmap(resultUri.path)
                        iviPhoto1.rotation = 360f
                        iviPhoto1.setImageBitmap(photo1)
                    }
                    2 -> {
                        photo2 = getBitmap(resultUri.path)
                        iviPhoto2.rotation = 360f
                        iviPhoto2.setImageBitmap(photo2)
                    }
                    3 -> {
                        photo3 = getBitmap(resultUri.path)
                        iviPhoto3.rotation = 360f
                        iviPhoto3.setImageBitmap(photo3)
                        btnCam.isEnabled = false
                    }
                }
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun getBitmap(path: String): Bitmap? {
        val uri = Uri.fromFile(File(path))
        var `in`: InputStream? = null
        try {
            val IMAGE_MAX_SIZE = 1200000 // 1.2MP
            `in` = contentResolver.openInputStream(uri)

            // Decode image size
            var o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`in`, null, o)
            `in`!!.close()


            var scale = 1
            while (o.outWidth * o.outHeight * (1 / Math.pow(scale.toDouble(), 2.0)) > IMAGE_MAX_SIZE) {
                scale++
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight)

            var b: Bitmap? = null
            `in` = contentResolver.openInputStream(uri)
            if (scale > 1) {
                scale--
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = BitmapFactory.Options()
                o.inSampleSize = scale
                b = BitmapFactory.decodeStream(`in`, null, o)

                // resize to desired dimensions
                val height = b!!.height
                val width = b.width
                Log.d("", "1th scale operation dimenions - width: $width, height: $height")

                val y = Math.sqrt(IMAGE_MAX_SIZE / (width.toDouble() / height))
                val x = y / height * width

                val scaledBitmap = Bitmap.createScaledBitmap(
                    b, x.toInt(),
                    y.toInt(), true
                )
                b.recycle()
                b = scaledBitmap

                System.gc()
            } else {
                b = BitmapFactory.decodeStream(`in`)
            }
            `in`!!.close()

            Log.d(
                "", "bitmap size - width: " + b!!.width + ", height: " +
                        b.height
            )
            return b
        } catch (e: IOException) {
            Log.e("", e.message, e)
            return null
        }

    }

    /**
     * PERMISOS EN EJECUCION
     * */
    private fun insertDummyContactWrapper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsNeeded = ArrayList<String>()
            val permissionsList = ArrayList<String>()
            if (!addPermission(permissionsList, Manifest.permission.CAMERA))
                permissionsNeeded.add("CAMERA")
            if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
                permissionsNeeded.add("Read Store")

            if (permissionsList.size > 0) {
                if (permissionsNeeded.size > 0) {
                    // Need Rationale
                    var message = "You need to grant access to " + permissionsNeeded[0]
                    for (i in 1 until permissionsNeeded.size)
                        message = message + ", " + permissionsNeeded[i]
                    showMessageOKCancel(message,
                        DialogInterface.OnClickListener { dialog, which ->
                            requestPermissions(
                                permissionsList.toTypedArray(),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
                            )
                        })
                    return
                }
                requestPermissions(
                    permissionsList.toTypedArray(),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
                )
                return
            }
        }else{

        }
        //insertDummyContact()
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    fun addPermission(permissionsList: MutableList<String>, permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission)
                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false
            }
        }else{

        }
        return true
    }


    /**
     * MD
     * */
    private fun startCropImageActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
            .start(this)
    }

}
