package com.kbyai.idcardrecognition.payment.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.theme.NobleGreen
import com.kbyai.idcardrecognition.theme.NobleGreenLight

internal fun NobleTransaction.isIncoming(): Boolean {
    return toUserId == ownerUserId ||
        fromUserId.startsWith("system") ||
        note?.contains("topup", ignoreCase = true) == true
}

@Composable
fun NobleTransactionCard(
    transaction: NobleTransaction,
    modifier: Modifier = Modifier,
) {
    val incoming = transaction.isIncoming()
    val amountColor = if (incoming) NobleGreen else MaterialTheme.colorScheme.onSurface
    val iconTint = if (incoming) NobleGreen else MaterialTheme.colorScheme.primary
    val iconBackground = if (incoming) NobleGreenLight else MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackground, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (incoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = iconTint,
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (incoming) "Money received" else "Money sent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = transaction.createdAt.take(16),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "${if (incoming) "+" else "-"}${transaction.amount} ${transaction.currency}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
        }
    }
}
