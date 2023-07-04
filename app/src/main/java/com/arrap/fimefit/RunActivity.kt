package com.arrap.fimefit

import android.content.ContentValues
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.arrap.fimefit.Utility.setHeightLinearLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.io.File

class RunActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var user: String? = null
    private var idRun: String? = null

    private var centerLat: Double? = null
    private var centerLong: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)
        createMapFragment()
        loadDatas()
    }
    private fun createMapFragment(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(centerLat!!, centerLong!!),
                16f), 1000, null)

        loadLocations()

    }

    private fun loadLocations(){

        var collection = "locations/$user/$idRun"


        var point: LatLng
        var listPoints: Iterable<LatLng>
        listPoints = arrayListOf()
        listPoints.clear()

        val dbLocations: FirebaseFirestore = FirebaseFirestore.getInstance()
        dbLocations.collection(collection)
            .orderBy("time")
            .get()
            .addOnSuccessListener { documents ->

                for (docLocation in documents) {

                    var position = docLocation.toObject(Location::class.java)
                    //listPosition.add(position!!)
                    point = LatLng(position?.latitude!!, position?.longitude!!)
                    listPoints.add(point)
                }
                paintRun(listPoints)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting locations: ", exception)
            }
    }
    private fun paintRun(listPosition:  Iterable<LatLng>){
        val polylineOptions = PolylineOptions()
            .width(25f)
            .color(ContextCompat.getColor(this, R.color.salmon_dark))
            .addAll(listPosition)

        val polyline = map.addPolyline(polylineOptions)
        polyline.startCap = RoundCap()
    }


    fun changeTypeMap(v: View){
        var ivTypeMap = findViewById<ImageView>(R.id.ivTypeMap)
        if (map.mapType == GoogleMap.MAP_TYPE_HYBRID){
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            ivTypeMap.setImageResource(R.drawable.map_type_hybrid)
        }
        else{
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            ivTypeMap.setImageResource(R.drawable.map_type_normal)
        }
    }



    private fun loadDatas(){
        //RECEPCION DE PARAMETRO
        val bundle = intent.extras

        user = bundle?.getString("user")
        idRun = bundle?.getString("idRun")
        centerLat = bundle?.getDouble("centerLatitude")
        centerLong = bundle?.getDouble("centerLongitude")

        if (bundle?.getDouble("distanceTarget") == 0.0){
            var lyCurrentLevel = findViewById<LinearLayout>(R.id.lyCurrentLevel)
            setHeightLinearLayout(lyCurrentLevel, 0)
        }
        else{

            var levelText = "${getString(R.string.level)} ${bundle?.getString("image_level")!!.subSequence(6,7).toString()}"
            var tvNumberLevel = findViewById<TextView>(R.id.tvNumberLevel)
            tvNumberLevel.text = levelText

            var ivCurrentLevel = findViewById<ImageView>(R.id.ivCurrentLevel)
            when (bundle.getString("image_level")){
                "level_1" -> ivCurrentLevel.setImageResource(R.drawable.level_1)
                "level_2" -> ivCurrentLevel.setImageResource(R.drawable.level_2)
                "level_3" -> ivCurrentLevel.setImageResource(R.drawable.level_3)
                "level_4" -> ivCurrentLevel.setImageResource(R.drawable.level_4)
                "level_5" -> ivCurrentLevel.setImageResource(R.drawable.level_5)
                "level_6" -> ivCurrentLevel.setImageResource(R.drawable.level_6)
                "level_7" -> ivCurrentLevel.setImageResource(R.drawable.level_7)
            }


            var csbDistanceLevel = findViewById<CircularSeekBar>(R.id.csbDistanceLevel)
            csbDistanceLevel.max = bundle?.getDouble("distanceTarget")!!.toFloat()
            csbDistanceLevel.progress = bundle?.getDouble("distanceTotal")!!.toFloat()

            var td = bundle?.getDouble("distanceTotal")!!
            var td_k: String = td.toString()
            if (td > 1000) td_k = (td/1000).toInt().toString() + "K"
            var ld = bundle?.getDouble("distanceTotal")!!.toDouble()
            var ld_k: String = ld.toInt().toString()
            if (ld > 1000) ld_k = (ld/1000).toInt().toString() + "K"

            var tvTotalDistance = findViewById<TextView>(R.id.tvTotalDistance)
            tvTotalDistance.text = "${td_k}/${ld_k} kms"
            //tvTotalDistance.text = "${totalsSelectedSport.totalDistance!!}/${levelSelectedSport.DistanceTarget!!} kms"

            var porcent = (bundle.getDouble("distanceTotal") *100 / bundle.getDouble("distanceTarget")).toInt()
            var tvTotalDistanceLevel = findViewById<TextView>(R.id.tvTotalDistanceLevel)
            tvTotalDistanceLevel.text = "$porcent%"

            var csbRunsLevel = findViewById<CircularSeekBar>(R.id.csbRunsLevel)
            csbRunsLevel.max = bundle.getDouble("runsTarget").toFloat()
            csbRunsLevel.max = bundle.getDouble("runsTotal").toFloat()

            var tvTotalRunsLevel = findViewById<TextView>(R.id.tvTotalRunsLevel)
            tvTotalRunsLevel.text = "${bundle.getInt("runsTotal")}/${bundle.getInt("runsTarget")}"


        }

        if (bundle?.getInt("countPhotos")!! > 0){
            var ivPicture = findViewById<ImageView>(R.id.ivPicture)
            var path = bundle?.getString("lastImage")

            val storageRef = FirebaseStorage.getInstance().reference.child(path!!) //.jpg")
            var localfile = File.createTempFile("tempImage", "jpg")
            storageRef.getFile(localfile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                ivPicture.setImageBitmap(bitmap)


                val metaRef = FirebaseStorage.getInstance().reference.child(path!!)

                metaRef.metadata.addOnSuccessListener { metadata ->
                    if (metadata.getCustomMetadata("orientation") == "horizontal"){
                        ivPicture.updateLayoutParams {
                            height = bitmap.height
                            ivPicture.translationX = 20f
                            ivPicture.translationY = -200f
                        }
                    }
                    else{
                        ivPicture.rotation = 90f
                        ivPicture.translationY = -500f
                        ivPicture.translationX = -80f
                        ivPicture.updateLayoutParams {
                            height = bitmap.width
                        }
                    }
                }.addOnFailureListener {
                    // Uh-oh, an error occurred!
                }

            }.addOnFailureListener{
                Toast.makeText(this, "fallo al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

        var ivSportSelected = findViewById<ImageView>(R.id.ivSportSelected)
        when (bundle?.getString("sport")){
            "Bike" ->  ivSportSelected.setImageResource(R.mipmap.bike)
            "RollerSkate" -> ivSportSelected.setImageResource(R.mipmap.rollerskate)
            "Running" -> ivSportSelected.setImageResource(R.mipmap.running)

        }

        var activatedGPS = bundle?.getBoolean("activatedGPS")
        if (activatedGPS == false){ //quitamos el mapa y los datos de mediciones
            var lyRun = findViewById<LinearLayout>(R.id.lyRun)
            setHeightLinearLayout(lyRun, 0)
            var lyDatas = findViewById<LinearLayout>(R.id.lyDatas)
            setHeightLinearLayout(lyDatas, 0)
        }
        else{

            var medalDistance = bundle?.getString("medalDistance")
            var medalAvgSpeed = bundle?.getString("medalAvgSpeed")
            var medalMaxSpeed = bundle?.getString("medalMaxSpeed")

            if (medalDistance == "none"
                && medalAvgSpeed == "none"
                && medalMaxSpeed == "none"){

                var lyMedalsRun = findViewById<LinearLayout>(R.id.lyMedalsRun)
                setHeightLinearLayout(lyMedalsRun, 0)
            }
            else{
                var ivMedalDistance = findViewById<ImageView>(R.id.ivMedalDistance)
                var tvMedalDistanceTitle = findViewById<TextView>(R.id.tvMedalDistanceTitle)

                when (medalDistance){
                    "gold" -> {
                        ivMedalDistance.setImageResource(R.drawable.medalgold)
                        tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        ivMedalDistance.setImageResource(R.drawable.medalsilver)
                        tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        ivMedalDistance.setImageResource(R.drawable.medalbronze)
                        tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                }

                var ivMedalAvgSpeed = findViewById<ImageView>(R.id.ivMedalAvgSpeed)
                var tvMedalAvgSpeedTitle = findViewById<TextView>(R.id.tvMedalAvgSpeedTitle)

                when (medalAvgSpeed){
                    "gold" -> {
                        ivMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                        tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        ivMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                        tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        ivMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                        tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                }

                var ivMedalMaxSpeed = findViewById<ImageView>(R.id.ivMedalMaxSpeed)
                var tvMedalMaxSpeedTitle = findViewById<TextView>(R.id.tvMedalMaxSpeedTitle)

                when (medalMaxSpeed){
                    "gold" -> {
                        ivMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                        tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        ivMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                        tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        ivMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                        tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                }
            }


            var tvDurationRun = findViewById<TextView>(R.id.tvDurationRun)

            tvDurationRun.text = bundle?.getString("duration")
            if (bundle?.getInt("challengeDuration") == 0){
                var lyChallengeDurationRun = findViewById<LinearLayout>(R.id.lyChallengeDurationRun)
                setHeightLinearLayout(lyChallengeDurationRun, 0)
            }
            else{
                var tvChallengeDurationRun = findViewById<TextView>(R.id.tvChallengeDurationRun)
                tvChallengeDurationRun.text = bundle?.getString("challengeDuration")
            }
            if (bundle?.getBoolean("intervalMode") == false){
                var lyIntervalRun = findViewById<LinearLayout>(R.id.lyIntervalRun)
                setHeightLinearLayout(lyIntervalRun, 0)
            }
            else{
                var details: String = "${bundle?.getInt("intervalDuration")}mins. ("
                details += "${bundle?.getString("runningTime")} / ${bundle?.getString("walkingTime")})"

                var tvIntervalRun = findViewById<TextView>(R.id.tvIntervalRun)
                tvIntervalRun.setText(details)
            }


            var tvDistanceRun = findViewById<TextView>(R.id.tvDistanceRun)
            tvDistanceRun.text = bundle?.getDouble("distance").toString()
            if (bundle?.getDouble("challengeDistance") == 0.0){
                var lyChallengeDistancePopUp = findViewById<LinearLayout>(R.id.lyChallengeDistancePopUp)
                setHeightLinearLayout(lyChallengeDistancePopUp, 0)
            }
            else{
                var tvChallengeDistanceRun = findViewById<TextView>(R.id.tvChallengeDistanceRun)
                tvChallengeDistanceRun.text = bundle?.getDouble("challengeDistance").toString()
            }

            if (bundle?.getDouble("minAltitude") == 0.0){
                var lyUnevennessRun = findViewById<LinearLayout>(R.id.lyUnevennessRun)
                setHeightLinearLayout(lyUnevennessRun, 0)
            }
            else{

                var tvMaxUnevennessRun = findViewById<TextView>(R.id.tvMaxUnevennessRun)
                var tvMinUnevennessRun = findViewById<TextView>(R.id.tvMinUnevennessRun)
                tvMaxUnevennessRun.text = bundle?.getDouble("maxAltitude")!!.toInt().toString()
                tvMinUnevennessRun.text = bundle?.getDouble("minAltitude")!!.toInt().toString()
            }
            var tvAvgSpeedRun = findViewById<TextView>(R.id.tvAvgSpeedRun)
            var tvMaxSpeedRun = findViewById<TextView>(R.id.tvMaxSpeedRun)

            tvAvgSpeedRun.text = bundle?.getDouble("avgSpeed").toString()
            tvMaxSpeedRun.text = bundle?.getDouble("maxSpeed").toString()
        }


    }

}