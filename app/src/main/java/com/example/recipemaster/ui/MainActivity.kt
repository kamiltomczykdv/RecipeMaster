package com.example.recipemaster.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipemaster.R
import com.example.recipemaster.model.User
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton

class MainActivity : AppCompatActivity() {
    private val logOutText = "Wyloguj się z Facebooka"
    private val logInText = "Zaloguj przez Facebooka"
    private lateinit var fbFloatingActionButton: com.github.clans.fab.FloatingActionButton
    private lateinit var recipeFloatingActionButton: com.github.clans.fab.FloatingActionButton
    private lateinit var fbLoginButton: LoginButton
    private lateinit var callbackManager: CallbackManager
    private lateinit var profileTracker: ProfileTracker
    private var user: User? = null

    companion object {
        const val argumentName: String = "USER"
        const val titleRecipe: String = "Pizza Recipe!"
        const val titleMain: String = "RecipeMaster"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        labelCheck()
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), 6
        )
        fbFloatingActionButton.setOnClickListener {
            if (!isLoggedIn()) logIn()
            else logOut()
        }

        recipeFloatingActionButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.main_layout, getRecipeFragment(user))
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        labelCheck()
    }

    private fun labelCheck() {
        if (isLoggedIn()) fbFloatingActionButton.labelText = logOutText
        else fbFloatingActionButton.labelText = logInText
    }

    private fun init() {
        callbackManager = CallbackManager.Factory.create()
        fbFloatingActionButton = findViewById(R.id.fab_fb)
        recipeFloatingActionButton = findViewById(R.id.fab_recipe)
    }

    private fun setUser(firstName: String, lastName: String) {
        user = User(firstName, lastName)
    }

    private fun logIn() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"))
        LoginManager.getInstance()
            .registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        labelCheck()
                        if (Profile.getCurrentProfile() == null) {
                            profileTracker = object : ProfileTracker() {
                                override fun onCurrentProfileChanged(
                                    oldProfile: Profile?,
                                    currentProfile: Profile
                                ) {
                                    setUser(currentProfile.firstName, currentProfile.lastName)
                                    profileTracker.stopTracking()
                                }
                            }
                        } else {
                            setUser(
                                Profile.getCurrentProfile().firstName,
                                Profile.getCurrentProfile().lastName
                            )
                        }
                    }

                    override fun onError(error: FacebookException?) {
                        labelCheck()
                        Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_LONG)
                            .show()
                    }

                    override fun onCancel() {
                        Toast.makeText(applicationContext, "Canceled", Toast.LENGTH_LONG).show()
                    }
                })
    }

    private fun logOut() {
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()
                    labelCheck()
                    user = null
                }
            ).executeAsync()
        }
    }

    private fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    private fun getRecipeFragment(user: User?): RecipeFragment {
        val recipeFragment = RecipeFragment()
        val argument = Bundle()
        argument.putParcelable(argumentName, user)
        recipeFragment.arguments = argument
        return recipeFragment
    }

}
