/*
 * :app — single Activity hosting the Compose navigation graph.
 *
 * Responsibility: own the NavHost, route between feature entry points, and forward hardware
 * key events (volume keys, Bluetooth HID buttons) to the active capture screen as a "trigger"
 * so planter mode works with gloves and without looking at the screen.
 *
 * Interface: feature modules expose a `NavGraphBuilder.<feature>Graph(...)` extension and a
 * route constant; this file is the only place that knows all of them (nowinandroid pattern).
 * Screens are placeholders until M1; see PLAN.md, "Milestones".
 */
package org.fieldcapture.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import org.fieldcapture.ui.FieldTheme

/** Hardware trigger events shared with capture screens (volume keys, BT buttons). */
object HardwareTrigger {
    val events = MutableSharedFlow<Long>(extraBufferCapacity = 8)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FieldTheme {
                FieldCaptureNavHost()
            }
        }
    }

    /**
     * Volume keys and most Bluetooth "selfie" buttons arrive as KEYCODE_VOLUME_UP/DOWN or
     * KEYCODE_CAMERA. While a capture screen is active they are consumed as triggers; otherwise
     * the system handles them normally.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val isTrigger = keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_CAMERA ||
            keyCode == KeyEvent.KEYCODE_HEADSETHOOK
        if (isTrigger && event?.repeatCount == 0 && HardwareTrigger.events.tryEmit(System.currentTimeMillis())) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

object Routes {
    const val HOME = "home"
    const val PLANTER = "planter"
    const val WALK = "walk"
    const val FIELDBOOK = "fieldbook"
    const val INTEGRATIONS = "integrations"
    const val CROSSING = "crossing"
}

@Composable
fun FieldCaptureNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { Text("Field Capture — scaffold / pre-alpha") }
        // M1: planterGraph(nav); M2: walkGraph(nav); M3: fieldbookGraph, integrationsGraph, crossingGraph.
    }
}
