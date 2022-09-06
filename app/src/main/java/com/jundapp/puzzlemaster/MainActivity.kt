package com.jundapp.puzzlemaster

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var mInterstitialAd: InterstitialAd
    private val REQUEST_CODE = 100
    private val BLANK = 0
    private val SIZE = 4


    private var data = arrayOf(-999, BLANK, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
    private var chunk = arrayOf(
        R.drawable.blank,
        R.drawable.img1,
        R.drawable.img2,
        R.drawable.img3,
        R.drawable.img4,
        R.drawable.img5,
        R.drawable.img6,
        R.drawable.img7,
        R.drawable.img8,
        R.drawable.img9,
        R.drawable.img10,
        R.drawable.img11,
        R.drawable.img12,
        R.drawable.img13,
        R.drawable.img14,
        R.drawable.img15,
        R.drawable.img16
    )
    private var currentBlank = 1
    private var default = true


    private var chunkedImages: ArrayList<Bitmap>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAds()

        chunkedImages = ArrayList()
        initGameEvent()

        updateDisplay()
    }

    private fun initAds() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-2749371619132674/9941973709"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
    }

    private fun initGameEvent() {
        for (i in 1 until 17)
            getImageView(i)!!.setOnClickListener{if (valid(i)) doUpdate(i)}

        btnChooseImage.setOnClickListener { openGallery() }
        btnShuffle.setOnClickListener { shuffle() }
    }

    private fun doUpdate(i: Int) {
        if (default) {
            getImageView(currentBlank)!!.setImageResource(chunk.get(data[i]))
            getImageView(i)!!.setImageResource(chunk.get(data[currentBlank]))
        } else {
            getImageView(currentBlank)!!.setImageBitmap(chunkedImages!!.get(data[i]))
            getImageView(i)!!.setImageBitmap(chunkedImages!!.get(data[currentBlank]))
        }
        data[currentBlank] = data[i]
        currentBlank = i
        data[i] = BLANK

        if(win()) {
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.")
            }
        }
    }

    private fun win(): Boolean {
        var res = true
        for (i in (2..16)){
            res = res && data[i]==i
        }
        return res
    }

    private fun valid(i: Int): Boolean {
        if (i > currentBlank) {
            return (i - currentBlank == 1 || i - currentBlank == 4)
        } else {
            return (currentBlank - i == 1 || currentBlank - i == 4)
        }
    }

    private fun shuffle() {

        fun doUp() {
            if (currentBlank <= SIZE * (SIZE - 1)) {
                data[currentBlank] = data[currentBlank + SIZE]
                currentBlank += SIZE
                data[currentBlank] = BLANK
            }
        }
        fun doRight() {
            if (currentBlank % SIZE != 1) {
                data[currentBlank] = data[currentBlank - 1]
                currentBlank -= 1
                data[currentBlank] = BLANK
            }
        }
        fun doBottom() {
            if (currentBlank > SIZE) {
                data[currentBlank] = data[currentBlank - SIZE]
                currentBlank -= SIZE
                data[currentBlank] = BLANK
            }
        }
        fun doLeft() {
            if (currentBlank % SIZE != 0) {
                data[currentBlank] = data[currentBlank + 1]
                currentBlank += 1
                data[currentBlank] = BLANK
            }
        }

        for (i in 0..100) {
            val random = Random.nextInt(4)
            when (random) {
                0 -> doBottom()
                1 -> doRight()
                2 -> doUp()
                3 -> doLeft()
            }
        }
        for (i in 0..4) {
            doBottom()
            doRight()
        }
        if (default) updateDisplay() else mergeImage()
    }
    private fun updateDisplay() {
        for (i in 1 until 17){
            getImageView(i)!!.setImageResource(chunk.get(data[i]))
        }
    }
    private fun mergeImage() {
        for (i in 1 until 17){
            getImageView(i)!!.setImageBitmap(chunkedImages!!.get(data[i]))
        }
    }
    private fun getImageView(i : Int) : ImageView? {
        when(i){
            1 -> return imageView1
            2 -> return imageView2
            3 -> return imageView3
            4 -> return imageView4
            5 -> return imageView5
            6 -> return imageView6
            7 -> return imageView7
            8 -> return imageView8
            9 -> return imageView9
            10 -> return imageView10
            11 -> return imageView11
            12 -> return imageView12
            13 -> return imageView13
            14 -> return imageView14
            15 -> return imageView15
            16 -> return imageView16
        }
        return null
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            CropImage.activity(data?.data)
                .setAspectRatio(1,1)
                .start(this);
        }
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === Activity.RESULT_OK) {
                rawImage.setImageURI(result.uri)
                splitImage(rawImage)
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun splitImage(image: ImageView) {

        val rows: Int
        val cols: Int

        val chunkHeight: Int
        val chunkWidth: Int

        chunkedImages!!.clear()
        chunkedImages!!.add(ContextCompat.getDrawable(this, R.drawable.blank)!!.toBitmap())

        val drawable = image.getDrawable() as BitmapDrawable
        val bitmap = drawable.bitmap
        val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        cols = 4
        rows = 4
        chunkHeight = bitmap.height / rows
        chunkWidth = bitmap.width / cols

        var yCoord = 0
        for (x in 0 until rows) {
            var xCoord = 0
            for (y in 0 until cols) {
                val bitmap = Bitmap.createBitmap(
                                    scaledBitmap,
                                    xCoord,
                                    yCoord,
                                    chunkWidth,
                                    chunkHeight
                                )
                chunkedImages!!.add(bitmap)
                xCoord += chunkWidth
            }
            yCoord += chunkHeight
        }
        default = false
        data = arrayOf(-999, BLANK, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        currentBlank = 1
        mergeImage()
    }

}
