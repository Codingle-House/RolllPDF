package id.co.rolllpdf.presentation.customview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import id.co.rolllpdf.databinding.ViewDialogFeatureproBinding

/**
 * Created by pertadima on 25,February,2021
 */
class DialogProFeatureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding by lazy {
        ViewDialogFeatureproBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private val viewHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private var animationRunnable: Runnable? = null

    private var action: (Action) -> Unit = { _ -> kotlin.run { } }

    init {
        isGone = true
        hideWithAnimation()
        binding.proCardviewContainer.setOnClickListener {
            action.invoke(Action.Click)
        }
    }

    fun showWithAnimation() {
        if (isVisible.not()) {
            animationRunnable = Runnable {
                isGone = false
            }
            animationRunnable?.let {
                viewHandler.postDelayed(it, SHOW_DELAY)
            }
        }
    }

    fun hideWithAnimation() {
        binding.proImageviewClose.setOnClickListener {
            isGone = true
            action.invoke(Action.Close)
        }
    }

    fun setListener(action: (Action) -> Unit): DialogProFeatureView {
        this.action = action
        return this
    }

    sealed class Action {
        object Click : Action()
        object Close : Action()
    }

    companion object {
        private const val SHOW_DELAY = 800L
    }
}