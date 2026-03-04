import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.min

class CardNumberVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter(Char::isDigit).take(16)

        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                append(c)
                if ((index + 1) % 4 == 0 && index != digits.lastIndex) append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {

            private fun spacesBeforeOriginalOffset(offset: Int): Int {
                // space بعد كل 4 أرقام قبل الـoffset
                // مثال: offset=4 => 1 space, offset=8 => 2 spaces, offset=16 => 3 spaces
                return min(offset / 4, 3)
            }

            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, digits.length)
                val out = clamped + spacesBeforeOriginalOffset(clamped)
                return out.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)

                // كم space قبل مكان الـcursor في النص المتحوّل؟
                val spaces = formatted.take(clamped).count { it == ' ' }
                val out = clamped - spaces
                return out.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
