package com.arrap.fimefit

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    companion object{
        lateinit var useremail: String
        lateinit var providerSession: String
    }

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var  etEmail: EditText
    private lateinit var  etPassword: EditText
    private lateinit var  lyTerms: LinearLayout

    private lateinit var mAuth: FirebaseAuth


    //Google Auth
    private var REQUEST_CODE_GOOGLE_SIGN_IN = 100
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        useremail =""
        //ocultar actividad de t&c
        lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE

        //obtenemos email y contraseñas del textbox
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        //iniciamos instancia de firebase
        mAuth = FirebaseAuth.getInstance()

        manageButtonLogin()
        etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }


        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.googleWebClientId))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(false)
            .build()

    }

    private fun manageButtonLogin() {
        var tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        //Activar o desactivar boton login
        if(!ValidatePassword.isPassword(password) || !ValidateEmail.isEmail(email)){
            tvLogin.setBackgroundColor(ContextCompat.getColor(this,R.color.gray))
            tvLogin.isEnabled = false

        }else{
            tvLogin.setBackgroundColor(ContextCompat.getColor(this,R.color.green))
            tvLogin.isEnabled = true
        }
    }

    fun login(view:View){
        loginUser()
    }

    private fun loginUser() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        //Revisar en la DB si el usuario existe , si no registrarlo
        mAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task->
                //el provedor en este caso es por email
                if(task.isSuccessful) goHome(email,"email")
                else{
                    if(lyTerms.visibility == View.INVISIBLE) {
                        lyTerms.visibility = View.VISIBLE
                    }
                    else{
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if(cbAcept.isChecked) {
                            register()
                        }
                    }
                }
            }


    }

    //cuando se cierre redirigirlo a la ventana home
    public override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser != null){
            goHome(currentUser.email.toString(),currentUser.providerId)
        }
    }

    //que al pulsar atras de diriga a la ventana main
    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    //redireccionar a pantalla principal
    private fun goHome(email:String,provider: String) {
        //le asignamos los datos a estas variables globales para verlos desde tras activcivties
        useremail = email
        providerSession = provider

        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    //Regitrar usaurio en base da datos
    private fun register(){
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    var dataRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                    var dbRegsiter = FirebaseFirestore.getInstance()
                    dbRegsiter.collection("users").document(email).set(hashMapOf(
                        "user" to email,
                        "dataRegister" to dataRegister
                    ))

                    goHome(email,"email")
                }else{
                     Toast.makeText(this,"Error algo ha ido mal",Toast.LENGTH_SHORT).show()
                }
            }
    }

    //ir a la activity terms
    fun goTerms(v:View){
        val intent = Intent(this,TermsActivity::class.java)
        startActivity(intent)
    }

    //ir a la activity forgotPassword
    fun forgotPassword(view:View){
        //startActivity(Intent(this,forgotPasswordActivity::class.java))

        resetPassowrd()
    }

    //Resetear contraseña
    private fun resetPassowrd() {
        var e = etEmail.text.toString()
        if(!TextUtils.isEmpty(e)){
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener{ task->
                    if(task.isSuccessful){
                        Toast.makeText(this,"Email enviado a $e",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"No se encontro el usuario con este correo",Toast.LENGTH_SHORT).show()
                    }

                }
        }
        else   Toast.makeText(this,"Indica un email",Toast.LENGTH_SHORT).show()
    }

    //boton login google
    fun callSignInGoogle(view: View) {
        signInGoogle()
    }

    //Iniciar sesion con google
    private fun signInGoogle() {

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(result.pendingIntent.intentSender, REQUEST_CODE_GOOGLE_SIGN_IN,null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    println("Google Sign --> Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                println("Google Sign --> No credential: ${e.localizedMessage}")
            }
    }

    //configurar iniciar secion google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            REQUEST_CODE_GOOGLE_SIGN_IN -> {

                try {

                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    email = credential.id

                    when {
                        idToken != null -> {

                            println("Google Sign -> Got ID token.")

                            val credential = GoogleAuthProvider.getCredential(idToken, null)
                            mAuth.signInWithCredential(credential).addOnCompleteListener{ task ->

                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Acceso correcto!", Toast.LENGTH_LONG).show()
                                    goHome(email, "Google")
                                } else {
                                    Toast.makeText(this, "Error: Algo salió mal!", Toast.LENGTH_LONG).show()
                                }

                            }

                        }
                        password != null -> {
                            println("Google Sign -> Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            println("Google Sign -> No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    println("Google Sign -> Error: ${e.localizedMessage}")
                }
            }
        }

    }

    fun callSignInFacebook(view: View) {}


}