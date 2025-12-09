package com.example.banana

import android.net.Uri
import android.os.Bundle
import android.view.View.MeasureSpec
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

val GeologicaFont = FontFamily(
    Font(R.font.geologica, FontWeight.Normal),
    Font(R.font.geologica, FontWeight.Bold)
)
val DarkColors = darkColorScheme(background = Color(0xFF0A0A0A), surface = Color(0xFF1C1C1E), primary = Color(0xFF4D79FF), onBackground = Color(0xFFEEEEEE), onSurface = Color(0xFF8E8E93))
val LightColors = lightColorScheme(background = Color(0xFFFFFFFF), surface = Color(0xFFF2F2F7), primary = Color(0xFF0055FF), onBackground = Color(0xFF000000), onSurface = Color(0xFF666666))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LuminaRootApp() }
    }
}

@Composable
fun LuminaRootApp() {
    var showSplashScreen by remember { mutableStateOf(true) }
    var isDarkTheme by remember { mutableStateOf(true) }
    val viewModel: LuminaViewModel = viewModel()

    MaterialTheme(colorScheme = if(isDarkTheme) DarkColors else LightColors) {
        Crossfade(targetState = showSplashScreen, animationSpec = tween(1000), label = "Intro") { isSplash ->
            if (isSplash) VideoSplashScreen { showSplashScreen = false }
            else MainAppStructure(isDarkTheme, { isDarkTheme = !isDarkTheme }, viewModel)
        }
    }
}

@Composable
fun VideoSplashScreen(onVideoFinished: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { ctx ->
            object : VideoView(ctx) {
                override fun onMeasure(w: Int, h: Int) = setMeasuredDimension(MeasureSpec.getSize(w), MeasureSpec.getSize(h))
            }.apply {
                setVideoURI(Uri.parse("android.resource://${ctx.packageName}/${R.raw.intro_video}"))
                setOnCompletionListener { onVideoFinished() }
                setOnErrorListener { _,_,_ -> onVideoFinished(); true }
                start()
            }
        }, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun MainAppStructure(isDarkTheme: Boolean, onThemeToggle: () -> Unit, viewModel: LuminaViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 8.dp) {
                listOf(
                    Triple("home", "Explore", Icons.Outlined.Home),
                    Triple("create_flow", "Create", Icons.Filled.AddCircle),
                    Triple("history", "History", Icons.Outlined.History)
                ).forEach { (route, title, icon) ->
                    val selected = currentRoute == route || (route == "create_flow" && currentRoute in listOf("upload","templates","processing","result"))
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(if(route == "create_flow") "upload" else route) {
                                popUpTo(navController.graph.startDestinationId); launchSingleTop = true
                            }
                        },
                        icon = { Icon(icon, title, modifier = Modifier.size(28.dp), tint = if(selected) MaterialTheme.colorScheme.primary else Color.Gray) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None }, exitTransition = { ExitTransition.None }) {

            composable("home") { HomeScreen(isDarkTheme, onThemeToggle, viewModel) }

            composable("upload") { UploadScreen(navController, viewModel) }
            composable("templates") { TemplateSelectionScreen(navController, viewModel) }
            composable("processing") { ProcessingScreen(navController, viewModel) }
            composable("result") { ResultScreen(navController, viewModel) }

            composable("history") { HistoryScreen(viewModel) }
        }
    }
}

@Composable
fun HomeScreen(isDarkTheme: Boolean, onThemeToggle: () -> Unit, viewModel: LuminaViewModel) {
    val feedImages by viewModel.feedImages.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    if(showSettings) SettingsDialog(isDarkTheme, onThemeToggle) { showSettings = false }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Lumina Feed", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)
            IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onBackground) }
        }
        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(feedImages.size) { index ->
                val image = feedImages[index]
                NetworkPhotoCard(image.author, image.download_url)
            }
        }
    }
}

@Composable
fun NetworkPhotoCard(author: String, url: String) {
    Column {
        Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).background(Color.DarkGray)) {
            AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Text(author, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
fun UploadScreen(navController: NavController, viewModel: LuminaViewModel) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.selectedPhotoUri = uri
    }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("New Creation", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp).border(2.dp, if(viewModel.selectedPhotoUri != null) MaterialTheme.colorScheme.primary else Color.Gray, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.selectedPhotoUri != null) AsyncImage(model = viewModel.selectedPhotoUri, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(60.dp)); Text("Pick from Gallery", color = MaterialTheme.colorScheme.onBackground) }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { navController.navigate("templates") }, enabled = viewModel.selectedPhotoUri != null, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Select Template", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
    }
}

@Composable
fun TemplateSelectionScreen(navController: NavController, viewModel: LuminaViewModel) {
    var selectedId by remember { mutableStateOf(1) }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Choose Style", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(20.dp))

        TemplateOption("Studio", "Pro Light", Icons.Filled.LightMode, selectedId == 1) { selectedId = 1; viewModel.selectedTemplateRes = R.drawable.ic_launcher_background }
        TemplateOption("Nature", "Wild Vibe", Icons.Filled.Landscape, selectedId == 2) { selectedId = 2; viewModel.selectedTemplateRes = R.drawable.ic_launcher_background }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { navController.navigate("processing") }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Generate", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
    }
}

@Composable
fun TemplateOption(title: String, desc: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if(isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    val borderColor = if(isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp)).background(bgColor).border(2.dp, borderColor, RoundedCornerShape(16.dp)).clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.width(16.dp))
        Column { Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold); Text(desc, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp) }
    }
}

@Composable
fun ProcessingScreen(navController: NavController, viewModel: LuminaViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.processImage(context) {
            navController.navigate("result") { popUpTo("upload") { inclusive = true } }
        }
    }
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("AI Magic in Progress...", fontFamily = GeologicaFont, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun ResultScreen(navController: NavController, viewModel: LuminaViewModel) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigate("home") }) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onBackground) }
            Spacer(modifier = Modifier.weight(1f))
            Text("Result", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(Color.DarkGray)) {
            viewModel.generatedBitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { viewModel.saveResult(context) }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Download, null); Spacer(Modifier.width(8.dp)); Text("Save & History", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun HistoryScreen(viewModel: LuminaViewModel) {
    val historyItems by viewModel.history.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My History", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(historyItems.size) { index ->
                val item = historyItems[index]
                Column {
                    Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color.DarkGray)) {
                        AsyncImage(model = item.imageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Text("Saved Item #${item.id}", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(isDarkTheme: Boolean, onThemeToggle: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Settings", fontFamily = GeologicaFont, color = MaterialTheme.colorScheme.onBackground) },
        text = { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("Dark Mode", color = MaterialTheme.colorScheme.onBackground); Switch(checked = isDarkTheme, onCheckedChange = { onThemeToggle() }) } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}