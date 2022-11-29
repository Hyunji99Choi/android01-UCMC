package com.gta.presentation.ui.login

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.gta.domain.model.LoginResult
import com.gta.presentation.databinding.ActivityLoginBinding
import com.gta.presentation.ui.MainActivity
import com.gta.presentation.ui.base.BaseActivity
import com.gta.presentation.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private val requestActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val authIntent = it.data ?: return@registerForActivityResult
                val account =
                    Auth.GoogleSignInApi.getSignInResultFromIntent(authIntent)?.signInAccount
                viewModel.signInWithToken(account?.idToken)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        initCollector()
        setUpWithBottomSheet()
        binding.btnLoginGoogle.setOnClickListener {
            googleLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCurrentUser()
    }

    private fun initCollector() {
        repeatOnStarted {
            viewModel.loginEvent.collectLatest { state ->
                when (state) {
                    LoginResult.SUCCESS -> startMainActivity()
                    LoginResult.FAILURE -> {}
                    LoginResult.NEWUSER -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
        }
    }

    private fun setUpWithBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.tvDescriptionServices.text = getAssetData("terms_of_service.txt")
        binding.tvDescriptionPrivacy.text = getAssetData("terms_of_privacy.txt")

        binding.tvDescriptionServices.movementMethod = ScrollingMovementMethod()
        binding.tvDescriptionPrivacy.movementMethod = ScrollingMovementMethod()

        bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })

        binding.btnAccept.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.signUp()
        }
        binding.btnCancel.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun googleLogin() {
        requestActivity.launch(googleSignInClient.signInIntent)
    }

    private fun getAssetData(fileName: String): String {
        var result: String
        try {
            val inputStream = resources.assets.open(fileName, AssetManager.ACCESS_BUFFER)
            val reader = BufferedReader(InputStreamReader(inputStream))
            result = reader.readLines().joinToString("\n")
            inputStream.close()
        } catch (e: IOException) {
            result = ""
        }
        return result
    }
}
