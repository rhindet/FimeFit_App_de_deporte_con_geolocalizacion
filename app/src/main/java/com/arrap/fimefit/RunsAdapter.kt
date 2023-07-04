package com.arrap.fimefit

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.arrap.fimefit.LoginActivity.Companion.useremail
import com.arrap.fimefit.Utility.animateViewofFloat
import com.arrap.fimefit.Utility.deleteRunAndLinkedData
import com.arrap.fimefit.Utility.setHeightLinearLayout
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File


class RunsAdapter(private val runsList:ArrayList<Runs>) : RecyclerView.Adapter<RunsAdapter.MyViewHolder>() {

    private lateinit var context:Context
    private var minimized = true

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):RunsAdapter.MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.card_run,parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder:RunsAdapter.MyViewHolder,position: Int) {
        val run :Runs = runsList[position]

        setHeightLinearLayout(holder.lyDataRunBody,0)
        holder.lyDataRunBodyContainer.translationY = -200f
        holder.ivHeaderOpenClose.setOnClickListener{
            if(minimized) {
                setHeightLinearLayout(holder.lyDataRunBody,600)
                animateViewofFloat(holder.lyDataRunBodyContainer,"translationY",0f,300L)
                holder.ivHeaderOpenClose.setRotation(180f)
                minimized = false
            }else {
                setHeightLinearLayout(holder.lyDataRunBody,0)
               holder.lyDataRunBodyContainer.translationY = -200f
                holder.ivHeaderOpenClose.setRotation(0f)
                minimized = true
            }
        }

        var day = run.date?.subSequence(8,10)
        var n_month = run.date?.subSequence(5,7)
        var month :String ?=null
        var year = run.date?.subSequence(0,4)

        when(n_month) {
            "01" -> month ="ENE"
            "02" -> month ="FEB"
            "03" -> month ="MAR"
            "04" -> month ="ABR"
            "05" -> month ="MAY"
            "06" -> month ="JUN"
            "07" -> month ="JUL"
            "08" -> month ="AGO"
            "09" -> month ="SEP"
            "10" -> month ="OCT"
            "11" -> month ="NOV"
            "12" -> month ="DIC"

        }
        var date :String = "$day-$month-$year"
        holder.tvDate.text = date
        holder.tvHeaderDate.text = date

        holder.tvStartTime.text = run.startTime?.subSequence(0,5)
        holder.tvDurationRun.text = run.duration
        holder.tvHeaderDuration.text = run.duration!!.subSequence(0,5).toString() + "HH"

        if(!run.challengeDuration.isNullOrEmpty()) {
            holder.tvChallengeDurationRun.text = run.challengeDuration
        }else {
            setHeightLinearLayout(holder.lyChallengeDurationRun,0)

        }

        if(run.challengeDistance != null) {
            holder.tvChallengeDistanceRun.text = run.challengeDistance.toString()
        }else {
            setHeightLinearLayout(holder.lyChallengeDistance,0)

        }

        if(run.intervalMode != null){
            var details:String = "${run.intervalDuration}mins. ("
            details += "${run.runningTime}/${run.walkingTime})"
            holder.tvIntervalRun.text = details
        }
        else{
            setHeightLinearLayout(holder.lyIntervalRun,0)
        }
        holder.tvDistanceRun.setText(run.distance.toString())
        holder.tvHeaderDistance.setText(run.distance.toString() + "KM")


        holder.tvMaxUnevennessRun.setText(run.maxAltitude.toString())
        holder.tvMinUnevennessRun.setText(run.minAltitude.toString())

        holder.tvAvgSpeedRun.setText(run.avgSpeed.toString())
        holder.tvHeaderAvgSpeed.setText(run.avgSpeed.toString()+"KM/H")
        holder.tvMaxSpeedRun.setText(run.maxSpeed.toString())

        when(run.medalDistance){
            "gold"->{
                holder.ivMedalDistance.setImageResource(R.drawable.medalgold)
                holder.ivHeaderMedalDistance.setImageResource(R.drawable.medalgold)
                holder.tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
            }
            "silver"->{
                holder.ivMedalDistance.setImageResource(R.drawable.medalsilver)
                holder.ivHeaderMedalDistance.setImageResource(R.drawable.medalsilver)
                holder.tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
            }
            "bronze"->{
                holder.ivMedalDistance.setImageResource(R.drawable.medalbronze)
                holder.ivHeaderMedalDistance.setImageResource(R.drawable.medalbronze)
                holder.tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
            }
        }

        when(run.medalAvgSpeed){
            "gold"->{
                holder.ivMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                holder.ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                holder.tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
            }
            "silver"->{
                holder.ivMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                holder.ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                holder.tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
            }
            "bronze"->{
                holder.ivMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                holder.ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                holder.tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
            }
        }
        when(run.medalMaxSpeed){
            "gold"->{
                holder.ivMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                holder.ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                holder.tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
            }
            "silver"->{
                holder.ivMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                holder.ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                holder.tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
            }
            "bronze"->{
                holder.ivMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                holder.ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                holder.tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
            }
        }

        if(run.lastImage != ""){
            var path = run.lastImage
            var storageRef = FirebaseStorage.getInstance().reference.child(path!!)
            var localFile = File.createTempFile("tempImage","jpg")
            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    val metaRef = FirebaseStorage.getInstance().getReference(run.lastImage!!)
                    val metadata: Task<StorageMetadata> = metaRef.metadata
                    metadata.addOnSuccessListener {
                        var or = it.getCustomMetadata("orientation")
                        if (or == "horizontal") {
                            var porcent = 100 / bitmap.width.toFloat()

                            setHeightLinearLayout(holder.lyPicture, (bitmap.height * porcent).toInt())
                            holder.ivPicture.setImageBitmap(bitmap)
                        }
                        else{
                            var porcent = 100/bitmap.width.toFloat()
                            setHeightLinearLayout(holder.lyPicture,(bitmap.height * porcent).toInt())
                            holder.ivPicture.setImageBitmap(bitmap)
                            holder.ivPicture.setRotation(90f)
                        }
                    }
                }

                .addOnFailureListener{
                    Toast.makeText(context,"Debes proporcionar permisos si quieres tomar fotos",Toast.LENGTH_LONG).show()
                }
        }

        holder.tvPlay.setOnClickListener {
            var idRun = run.date + run.startTime
            idRun = idRun.replace(":", "")
            idRun = idRun.replace("/", "")

            //ENVIO DE PARAMETROS
            val intent = Intent(context, RunActivity::class.java)
            val inParameter = intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            inParameter.putExtra("user", run.user)
            inParameter.putExtra("idRun", idRun)


            inParameter.putExtra("countPhotos", run.countPhotos)
            inParameter.putExtra("lastImage", run.lastImage)

            inParameter.putExtra("centerLatitude", run.centerLatitude)
            inParameter.putExtra("centerLongitude", run.centerLongitude)

            inParameter.putExtra("date", run.date)
            inParameter.putExtra("startTime", run.startTime)
            inParameter.putExtra("duration", run.duration)
            inParameter.putExtra("distance", run.distance)
            inParameter.putExtra("maxSpeed", run.maxSpeed)
            inParameter.putExtra("avgSpeed", run.avgSpeed)
            inParameter.putExtra("minAltitude", run.minAltitude)
            inParameter.putExtra("maxAltitude", run.maxAltitude)
            inParameter.putExtra("medalDistance", run.medalDistance)
            inParameter.putExtra("medalAvgSpeed", run.medalAvgSpeed)
            inParameter.putExtra("medalMaxSpeed", run.medalMaxSpeed)
            inParameter.putExtra("activatedGPS", run.activatedGPS)
            inParameter.putExtra("sport", run.sport)
            inParameter.putExtra("intervalMode", run.intervalMode)
            inParameter.putExtra("intervalDuration", run.intervalDuration)
            inParameter.putExtra("runningTime", run.runningTime)
            inParameter.putExtra("walkingTime", run.walkingTime)
            inParameter.putExtra("challengeDistance", run.challengeDistance)
            inParameter.putExtra("challengeDuration", run.challengeDuration)


            context.startActivity(intent)

        }

        holder.tvDelete.setOnClickListener{
            var id:String = useremail + run.date + run.startTime
            id = id.replace(":","")
            id = id.replace("/","")

            var currentRun = Runs()
            currentRun.distance = run.distance
            currentRun.avgSpeed = run.avgSpeed
            currentRun.maxSpeed = run.maxSpeed
            currentRun.duration = run.duration
            currentRun.activatedGPS = run.activatedGPS
            currentRun.date = run.date
            currentRun.startTime = run.startTime
            currentRun.user = run.user
            currentRun.sport = run.sport

            deleteRunAndLinkedData(id,currentRun.sport!!,holder.lyDataRunHeader,currentRun)
            runsList.removeAt(position)
            notifyItemRemoved(position)

        }

    }

    override fun getItemCount(): Int {
      return runsList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val lyDataRunHeader: LinearLayout = itemView.findViewById(R.id.lyDataRunHeader)
        val tvHeaderDate: TextView = itemView.findViewById(R.id.tvHeaderDate)
        val tvHeaderDuration: TextView = itemView.findViewById(R.id.tvHeaderDuration)
        val tvHeaderDistance: TextView = itemView.findViewById(R.id.tvHeaderDistance)
        val tvHeaderAvgSpeed: TextView = itemView.findViewById(R.id.tvHeaderAvgSpeed)
        val ivHeaderMedalDistance: ImageView = itemView.findViewById(R.id.ivHeaderMedalDistance)
        val ivHeaderMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalAvgSpeed)
        val ivHeaderMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalMaxSpeed)
        val ivHeaderOpenClose: ImageView = itemView.findViewById(R.id.ivHeaderOpenClose)

        val lyDataRunBody: LinearLayout = itemView.findViewById(R.id.lyDataRunBody)
        val lyDataRunBodyContainer: LinearLayout = itemView.findViewById(R.id.lyDataRunBodyContainer)

        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)


        val tvDurationRun: TextView = itemView.findViewById(R.id.tvDurationRun)
        val lyChallengeDurationRun: LinearLayout = itemView.findViewById(R.id.lyChallengeDurationRun)
        val tvChallengeDurationRun: TextView = itemView.findViewById(R.id.tvChallengeDurationRun)
        val lyIntervalRun: LinearLayout = itemView.findViewById(R.id.lyIntervalRun)
        val tvIntervalRun: TextView = itemView.findViewById(R.id.tvIntervalRun)


        val tvDistanceRun: TextView = itemView.findViewById(R.id.tvDistanceRun)
        val lyChallengeDistance: LinearLayout = itemView.findViewById(R.id.lyChallengeDistance)
        val tvChallengeDistanceRun: TextView = itemView.findViewById(R.id.tvChallengeDistanceRun)
        val lyUnevennessRun: LinearLayout = itemView.findViewById(R.id.lyUnevennessRun)
        val tvMaxUnevennessRun: TextView = itemView.findViewById(R.id.tvMaxUnevennessRun)
        val tvMinUnevennessRun: TextView = itemView.findViewById(R.id.tvMinUnevennessRun)


        val tvAvgSpeedRun: TextView = itemView.findViewById(R.id.tvAvgSpeedRun)
        val tvMaxSpeedRun: TextView = itemView.findViewById(R.id.tvMaxSpeedRun)

        val ivMedalDistance: ImageView = itemView.findViewById(R.id.ivMedalDistance)
        val tvMedalDistanceTitle: TextView = itemView.findViewById(R.id.tvMedalDistanceTitle)
        val ivMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivMedalAvgSpeed)
        val tvMedalAvgSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalAvgSpeedTitle)
        val ivMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivMedalMaxSpeed)
        val tvMedalMaxSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalMaxSpeedTitle)


        val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)

        val lyPicture: LinearLayout = itemView.findViewById(R.id.lyPicture)
        val tvPlay: TextView = itemView.findViewById(R.id.tvPlay)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
    }


}

