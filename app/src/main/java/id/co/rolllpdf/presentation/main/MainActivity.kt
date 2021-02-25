package id.co.rolllpdf.presentation.main

import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
    }

    override fun onViewModelObserver() {
    }
}