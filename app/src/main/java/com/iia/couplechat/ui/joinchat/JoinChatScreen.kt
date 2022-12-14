package com.iia.couplechat.ui.joinchat

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iia.couplechat.ui.components.CoupleChatAppBar
import com.iia.couplechat.ui.components.CoupleChatOutlinedTextField
import com.iia.couplechat.ui.destinations.CreateChatPageDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
@Destination
fun JoinChatScreen(navigator: DestinationsNavigator = EmptyDestinationsNavigator) {
    Scaffold(
        topBar = {
            CoupleChatAppBar(
                title = "Join Chat",
                navigationIcon = Icons.Default.ArrowBack,
                navigationIconClick = { navigator.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary.copy(.9f))) {
                    pushStringAnnotation(tag = "Non Link", annotation = "")
                    append("Enter invitation code. If you don't have ask your partner or ")
                    pushStringAnnotation(tag = "Link", "")
                    withStyle(
                        style = SpanStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("create new one")
                    }
                    pop()
                }
            }
            var invitationCode by remember { mutableStateOf("") }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(.8f)
            ) {
                Text(text = "Invitation Code")
                ClickableText(
                    text = annotatedString,
                ) { offset ->
                    annotatedString
                        .getStringAnnotations(tag = "Link", start = offset, end = offset)
                        .firstOrNull()
                        .let {
                            navigator.navigate(CreateChatPageDestination)
                        }
                }

                CoupleChatOutlinedTextField(
                    value = invitationCode,
                    onValueChange = {
                        invitationCode = it
                    },
                    trailingIcon = {
                        IconButton(onClick = {  }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "")
                        }
                    })
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Preview
@Composable
fun JoinChatScreenPreview() {
    JoinChatScreen()
}