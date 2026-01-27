import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.ethiopay.payment.tap.TapAddCardActivity

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var tokenId by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            tokenId = res.data?.getStringExtra("tap_token_id")
        }
    }

    Column {
        Button(onClick = {
            launcher.launch(Intent(context, TapAddCardActivity::class.java))
        }) { Text("Add Card (Tap)") }

        if (tokenId != null) {
            Text("Token: $tokenId")
        }
    }
}
