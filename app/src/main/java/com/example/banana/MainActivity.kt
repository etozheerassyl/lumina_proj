package com.example.banana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

val AppBlack = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF1C1C1E)
val AccentBlue = Color(0xFF4D79FF)
val TextWhite = Color(0xFFEEEEEE)
val TextGray = Color(0xFF8E8E93)

val GeologicaFont = FontFamily(
    Font(R.font.geologica, FontWeight.Normal),
    Font(R.font.geologica, FontWeight.Bold)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = AppBlack,
                    surface = SurfaceDark,
                    primary = AccentBlue,
                    onBackground = TextWhite
                )
            ) {
                MainAppStructure()
            }
        }
    }
}

@Composable
fun MainAppStructure() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = AppBlack,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, SurfaceDark)
            ) {
                val items = listOf(
                    Triple("home", "Explore", Icons.Outlined.Home),
                    Triple("create_flow", "Create", Icons.Filled.AddCircle),
                    Triple("history", "History", Icons.Outlined.History)
                )

                items.forEach { (route, title, icon) ->
                    val selected = currentRoute == route
                    val isCreateActive = route == "create_flow" && (currentRoute?.startsWith("create") == true || currentRoute == "upload" || currentRoute == "templates" || currentRoute == "processing" || currentRoute == "result")
                    val finalSelected = selected || isCreateActive

                    NavigationBarItem(
                        selected = finalSelected,
                        onClick = {
                            if (route == "create_flow") navController.navigate("upload")
                            else navController.navigate(route)
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(28.dp),
                                tint = if (finalSelected) AccentBlue else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(innerPadding)) {
            composable("home") { HomeScreen() }
            composable("upload") { UploadScreen(navController) }
            composable("templates") { TemplateSelectionScreen(navController) }
            composable("processing") { ProcessingScreen(navController) }
            composable("result") { ResultScreen(navController) }
            composable("history") { HistoryScreen() }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Lumina Feed", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextWhite)
        Spacer(modifier = Modifier.height(20.dp))

        val myPhotos = listOf(
            R.drawable.jpg,
            R.drawable.oakley,
            R.drawable.phone,
            R.drawable.purse,
            R.drawable.shoes,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(5) { index ->
                val currentPhoto = myPhotos[index % myPhotos.size]

                PhotoCard(
                    title = "Project #${index + 1}",
                    imageRes = currentPhoto
                )
            }
        }
    }
}

@Composable
fun PhotoCard(title: String, imageRes: Int) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.padding(10.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("AI", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = TextGray, fontSize = 12.sp, fontFamily = GeologicaFont)
    }
}

@Composable
fun UploadScreen(navController: NavController) {
    var isLoaded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("New Creation", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = TextWhite)
        Text("Upload your product photos", fontFamily = GeologicaFont, fontSize = 16.sp, color = TextGray)
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .border(2.dp, if(isLoaded) AccentBlue else Color.DarkGray, RoundedCornerShape(20.dp))
                .clickable { isLoaded = true },
            contentAlignment = Alignment.Center
        ) {
            if (isLoaded) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(100.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CloudUpload, null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tap to Upload", color = TextWhite, fontFamily = GeologicaFont)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navController.navigate("templates") },
            enabled = isLoaded,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, disabledContainerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Next Step", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
    }
}

@Composable
fun TemplateSelectionScreen(navController: NavController) {
    var selectedId by remember { mutableStateOf(1) }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Select Vibe", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = TextWhite)
        Spacer(modifier = Modifier.height(20.dp))
        TemplateOption("Studio Minimal", "Clean background", Icons.Filled.LightMode, selectedId == 1) { selectedId = 1 }
        TemplateOption("Nature", "Forest, mountains", Icons.Filled.Landscape, selectedId == 2) { selectedId = 2 }
        TemplateOption("Urban Life", "City blur", Icons.Filled.LocationCity, selectedId == 3) { selectedId = 3 }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navController.navigate("processing") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Generate", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
    }
}

@Composable
fun TemplateOption(title: String, desc: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if(isSelected) AccentBlue.copy(alpha = 0.15f) else SurfaceDark)
            .border(2.dp, if(isSelected) AccentBlue else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextWhite)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = GeologicaFont)
            Text(desc, color = TextGray, fontSize = 12.sp, fontFamily = GeologicaFont)
        }
    }
}

@Composable
fun ProcessingScreen(navController: NavController) {
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            progress += 0.02f
            delay(50)
        }
        navController.navigate("result") { popUpTo("upload") { inclusive = true } }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(progress = progress, modifier = Modifier.size(100.dp), color = AccentBlue, strokeWidth = 8.dp)
        Spacer(modifier = Modifier.height(32.dp))
        Text("AI Magic...", color = TextGray, fontFamily = GeologicaFont)
    }
}

@Composable
fun ResultScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigate("home") }) { Icon(Icons.Default.Close, null, tint = TextWhite) }
            Spacer(modifier = Modifier.weight(1f))
            Text("Done!", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(4) {
                Box(
                    modifier = Modifier.height(200.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceDark)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My History", fontFamily = GeologicaFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextWhite)
        Spacer(modifier = Modifier.height(20.dp))

        val historyPhotos = listOf(
            R.drawable.jpg,
            R.drawable.shoes
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { index ->
                val photo = historyPhotos[index % historyPhotos.size]

                HistoryCard(
                    index = index,
                    imageRes = photo
                )
            }
        }
    }
}
@Composable
fun HistoryCard(index: Int, imageRes: Int) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Project #$index", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = GeologicaFont)
    }
}