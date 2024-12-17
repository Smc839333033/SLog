package ui.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smc.slog.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogPanel(
    isShow: Boolean = false,
    pasteTipDialogDismiss: () -> Unit,
    pasteTipDialogSubmit: () -> Unit,
) {
    AnimatedVisibility(isShow, enter = scaleIn(), exit = scaleOut()) {
        PasteTipDialog(disMiss = {
            pasteTipDialogDismiss()
        }, submit = {
            pasteTipDialogSubmit()
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasteTipDialog(disMiss: () -> Unit, submit: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().onClick(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(250.dp).height(250.dp)
                .background(Color(232, 232, 233), shape = RoundedCornerShape(10.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painterResource(Res.drawable.icon),
                null,
                modifier = Modifier.size(60.dp)
            )

            Spacer(Modifier.height(30.dp))
            Text(
                text = stringResource(Res.string.ask_open_text),
                color = Color.Black,
                fontSize = 12.sp,
                softWrap = true,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(Res.string.open_tab),
                color = Color.Black,
                fontSize = 10.sp,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(30.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Button(
                    modifier = Modifier.size(95.dp, 30.dp),
                    onClick = {
                        disMiss()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(147, 148, 148)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = Color.White,
                        fontSize = 10.sp,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(18.dp))

                Button(
                    modifier = Modifier.size(95.dp, 30.dp),
                    onClick = {
                        submit()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(60, 130, 247)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.submit),
                        color = Color.White,
                        fontSize = 10.sp,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}