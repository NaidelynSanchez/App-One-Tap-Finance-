package com.example.onetapfinanzas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Modelos de datos para el estado dinámico
data class Account(val name: String, val balance: Double)
data class FinanceRecord(val concept: String, val amount: Double)

// Paleta de colores
val ButtonTeal = Color(0xFF00BCD4)
val TopBarCyan = Color(0xFF0097A7)
val CardNavyTransparent = Color(0xCC1E293B)
val CardNavySolid = Color(0xFF1E293B)

val DrawerGreenHeader = Color(0xFF2ECC71)
val DrawerSelectedItemBg = Color(0xFFD0E8FF)
val DrawerSelectedItemIcon = Color(0xFFE91E63)

enum class DrawerMenuSection(val title: String, val icon: ImageVector, val badge: String? = null) {
    PREMIUM("Obtener Premium", Icons.Default.Star),
    SYNC("Sincronización bancaria", Icons.Default.Refresh),
    INICIO("Inicio", Icons.Default.Home),
    CUENTAS("Cuentas", Icons.Default.AccountBox),
    REGISTROS("Registros", Icons.Default.List),
    INVERSIONES("Inversiones", Icons.Default.AddCircle, badge = "Nuevo"),
    ESTADISTICAS("Estadísticas", Icons.Default.DateRange),
    PAGOS_PLANIFICADOS("Pagos planificados", Icons.Default.Build),
    PRESUPUESTOS("Presupuestos", Icons.Default.DateRange),
    DEUDAS("Deudas", Icons.Default.ShoppingCart),
    METAS("Metas", Icons.Default.CheckCircle)
}

enum class ScreenState {
    WELCOME, LOGIN, HOME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppBackgroundWrapper {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppBackgroundWrapper(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E3A8A),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(ScreenState.WELCOME) }
    var userName by remember { mutableStateOf("Usuario") }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            ScreenState.WELCOME -> WelcomeScreen(
                onLoginClick = { currentScreen = ScreenState.LOGIN }
            )
            ScreenState.LOGIN -> LoginScreen(
                onBackClick = { currentScreen = ScreenState.WELCOME },
                onLoginSuccess = { nameInput ->
                    userName = if (nameInput.isNotBlank()) {
                        if (nameInput.contains("@")) nameInput.substringBefore("@") else nameInput
                    } else {
                        "Usuario"
                    }
                    currentScreen = ScreenState.HOME
                }
            )
            ScreenState.HOME -> HomeScreen(
                userName = userName,
                onLogout = { currentScreen = ScreenState.WELCOME }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "Usuario",
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf(DrawerMenuSection.INICIO) }
    var selectedTab by remember { mutableIntStateOf(0) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFF8F9FA),
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DrawerGreenHeader)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Perfil",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mi Wallet",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DrawerMenuSection.entries.forEach { section ->
                        val isSelected = selectedSection == section

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) DrawerSelectedItemBg else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSection = section
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = section.title,
                                    tint = when {
                                        isSelected -> DrawerSelectedItemIcon
                                        section == DrawerMenuSection.PREMIUM -> Color(0xFFF57C00)
                                        else -> Color(0xFF37474F)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = section.title,
                                    color = Color(0xFF263238),
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )

                                if (section.badge != null) {
                                    Surface(
                                        color = Color(0xFF007AFF),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = section.badge,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )

                    // Opción de Cerrar Sesión
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        drawerState.close()
                                        onLogout()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Cerrar sesión",
                                color = Color(0xFFD32F2F),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.background(TopBarCyan)) {
                    TopAppBar(
                        title = { Text(selectedSection.title, color = Color.White, fontSize = 22.sp) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = TopBarCyan)
                    )
                    if (selectedSection == DrawerMenuSection.INICIO) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = TopBarCyan,
                            contentColor = Color.White
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Cuentas", color = Color.White) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Presupuestos y objetos", color = Color.White) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedSection) {
                    DrawerMenuSection.INICIO -> MainDashboardContent(selectedTab)
                    else -> GenericScreenContent(sectionTitle = selectedSection.title)
                }
            }
        }
    }
}

@Composable
fun MainDashboardContent(selectedTab: Int) {
    val accountsList = remember { mutableStateListOf(Account("Efectivo", 100.0)) }
    val recordsList = remember { mutableStateListOf<FinanceRecord>() }

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddRecordDialog by remember { mutableStateOf(false) }

    val totalAccountsBalance = accountsList.sumOf { it.balance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Mis cuentas",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accountsList) { account ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ButtonTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(account.name, color = Color.White, fontSize = 12.sp)
                            Text("MXN ${account.balance}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Button(
                onClick = { showAddAccountDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(60.dp)
            ) {
                Text("Agregar cuenta +", color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardNavyTransparent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Estructura de datos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Últimos 30 días", color = Color.LightGray, fontSize = 12.sp)
                        Text("MXN ${recordsList.sumOf { it.amount }}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F7FA))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardNavyTransparent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tendencia del saldo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("HOY", color = Color.Gray, fontSize = 10.sp)
                                Text("MXN $totalAccountsBalance", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("vs período anterior", color = Color.Gray, fontSize = 10.sp)
                                Text("0%", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp)
                                .background(Color(0xFFE3F2FD))
                                .padding(8.dp)
                        ) {
                            if (recordsList.isEmpty()) {
                                Text("No hay registros añadidos", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                recordsList.forEach { record ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(record.concept, color = Color.Black, fontSize = 13.sp)
                                        Text("MXN ${record.amount}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { showAddRecordDialog = true },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text("Añadir registro", color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddAccountDialog) {
        var accountName by remember { mutableStateOf("") }
        var initialBalance by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Nueva Cuenta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Nombre de la cuenta") }
                    )
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("Monto inicial") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = initialBalance.toDoubleOrNull() ?: 0.0
                        if (accountName.isNotBlank()) {
                            accountsList.add(Account(accountName, amount))
                            showAddAccountDialog = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddRecordDialog) {
        var concept by remember { mutableStateOf("") }
        var recordAmount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRecordDialog = false },
            title = { Text("Añadir Registro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = { Text("Concepto") }
                    )
                    OutlinedTextField(
                        value = recordAmount,
                        onValueChange = { recordAmount = it },
                        label = { Text("Monto (MXN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = recordAmount.toDoubleOrNull() ?: 0.0
                        if (concept.isNotBlank()) {
                            recordsList.add(FinanceRecord(concept, amount))
                            showAddRecordDialog = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRecordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun GenericScreenContent(sectionTitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardNavyTransparent),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pantalla de $sectionTitle",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Contenido en desarrollo...",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "One Tap Finanzas",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = ButtonTeal),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Log in",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Don't have an account? ",
                color = Color.White,
                fontSize = 14.sp
            )
            TextButton(onClick = { }, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = "Sign up",
                    color = Color(0xFFFF5252),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = CardNavySolid),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenid@ a tus finanzas",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(28.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("E-mail o Nombre", color = Color.LightGray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onLoginSuccess(email) },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonTeal),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Log in",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onBackClick) {
                    Text(
                        text = "← Volver",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppNavigationPreview() {
    MaterialTheme {
        AppBackgroundWrapper {
            HomeScreen()
        }
    }
}