package com.example.singinwithgoogle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.singinwithgoogle.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val TAG = "IdTokenActivity"
    private val RC_GET_TOKEN = 9002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.signInBtn.setOnClickListener {
            // For sample only: make sure there is a valid server client ID.
            validateServerClientID()
            if (!isUserSignedIn()) {
                signinInWithGoogle()
            } else {
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show()
            }
        }
        binding.signOutBtn.setOnClickListener { signOut() }
        binding.disconnectButton.setOnClickListener { revokeAccess() }

    }

    private fun isUserSignedIn(): Boolean {

        val account = GoogleSignIn.getLastSignedInAccount(this)
        return account != null

    }

    private fun signinInWithGoogle() {

        // [START configure_signin]
        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()
        // [END configure_signin]

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        // [END configure_signin]

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //getIDToken
        getIdToken()
    }

    private fun getIdToken() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(
            signInIntent, RC_GET_TOKEN
        )
    }

    /**
     * Validates that there is a reasonable server client ID in strings.xml, this is only needed
     * to make sure users of this sample follow the README.
     */
    private fun validateServerClientID() {
        val serverClientId = getString(R.string.server_client_id)
        val suffix = ".apps.googleusercontent.com"
        if (!serverClientId.trim { it <= ' ' }.endsWith(suffix)) {
            val message =
                "Invalid server client ID in strings.xml, must end with $suffix"
            Log.w("TAG", message)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    // [START handle_sign_in_result]
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            Log.v(TAG, idToken.toString())
            Toast.makeText(this, idToken, Toast.LENGTH_LONG).show()

            // TODO: send ID Token to server and validate
            updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "handleSignInResult:error", e)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()

            updateUI(null)
        }
    }
    // [END handle_sign_in_result]

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GET_TOKEN) {
            // [START get_id_token]
            // This task is always completed immediately, there is no need to attach an
            // asynchronous listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            // [END get_id_token]
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            val idToken = account.idToken
            Log.v(TAG, idToken.toString())
            Toast.makeText(this, idToken, Toast.LENGTH_LONG).show()
        } else {
            Log.v(TAG, "idToken null")
            Toast.makeText(this, "idToken null", Toast.LENGTH_LONG).show()

        }
    }

    private fun signOut() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) { updateUI(null) }
    }

    private fun revokeAccess() {
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this) { updateUI(null) }
    }
}