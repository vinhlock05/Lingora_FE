package com.example.lingora_fe.user.practice.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import kotlinx.coroutines.delay

data class ReadingQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPracticeScreen(
    navController: NavController
) {
    // Sample passage
    val passage = """
        The Industrial Revolution, which began in Britain in the late 18th century, marked a major turning point in history. Almost every aspect of daily life was influenced in some way. Most notably, average income and population began to exhibit unprecedented sustained growth.
        
        The textile industry was the first to adopt modern methods of production. The use of machinery powered by water and later steam transformed manufacturing. These innovations spread to other industries, including mining, transportation, and agriculture.
        
        The social and economic changes were profound. Urbanization accelerated as people moved from rural areas to cities for factory work. New social classes emerged, including a wealthy industrial middle class and a large working class. Working conditions in early factories were often harsh, with long hours and dangerous machinery.
        
        The Industrial Revolution also brought significant technological advances. The development of the steam engine by James Watt revolutionized transportation and manufacturing. Railways expanded rapidly, connecting cities and facilitating trade. Communication improved with the invention of the telegraph.
        
        Environmental impacts were substantial. Air and water pollution increased in industrial areas. Deforestation expanded as demand for timber grew. However, the increased productivity also raised living standards for many people over time.
    """.trimIndent()
    
    // Sample questions
    val questions = remember {
        listOf(
            ReadingQuestion(
                id = 1,
                question = "Where did the Industrial Revolution begin?",
                options = listOf("France", "Britain", "America", "Germany"),
                correctAnswer = 1
            ),
            ReadingQuestion(
                id = 2,
                question = "Which industry was the first to adopt modern production methods?",
                options = listOf("Mining", "Textile", "Agriculture", "Transportation"),
                correctAnswer = 1
            ),
            ReadingQuestion(
                id = 3,
                question = "What powered the machinery in early factories?",
                options = listOf("Electricity", "Wind", "Water and steam", "Coal"),
                correctAnswer = 2
            ),
            ReadingQuestion(
                id = 4,
                question = "What happened to the population during the Industrial Revolution?",
                options = listOf("It decreased", "It remained stable", "It grew rapidly", "It fluctuated"),
                correctAnswer = 2
            ),
            ReadingQuestion(
                id = 5,
                question = "Who developed the steam engine?",
                options = listOf("Thomas Edison", "James Watt", "George Stephenson", "Isaac Newton"),
                correctAnswer = 1
            )
        )
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showPassage by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    val currentQuestion = questions[currentQuestionIndex]
    
    // Timer (60 minutes total)
    var timeRemaining by remember { mutableStateOf(60 * 60) }
    
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        }
    }
    
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    
    // Back handler
    BackHandler {
        showExitDialog = true
    }
    
    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát khỏi bài tập?") },
            text = { Text("Tiến trình làm bài của bạn sẽ không được lưu. Bạn có chắc muốn thoát?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Thoát", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tiếp tục làm bài")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reading",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showPassage = !showPassage }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = GradientStart
                            )
                            Text(
                                text = if (showPassage) "Ẩn đoạn văn" else "Xem đoạn văn",
                                color = GradientStart,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = timerText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Part indicator and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Part 1: Short Passage",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF166534)
                    )
                }
            }

            // Question number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu ${currentQuestionIndex + 1}/${questions.size}",
                    fontSize = 14.sp,
                    color = NavBarText
                )
            }

            // Reading Passage
            if (showPassage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFECFDF5)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reading Passage",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Part 1",
                                fontSize = 12.sp,
                                color = Color(0xFF059669)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Divider(color = Color(0xFFA7F3D0))
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Scrollable passage content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = passage,
                                fontSize = 14.sp,
                                color = MainText,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Question Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Q${currentQuestion.id}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = currentQuestion.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MainText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Answer options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        currentQuestion.options.forEachIndexed { index, answer ->
                            ReadingAnswerOption(
                                text = answer,
                                isSelected = selectedAnswer == index,
                                onClick = {
                                    selectedAnswer = index
                                }
                            )
                        }
                    }
                }
            }

            // Tip card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF6FF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💡",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Đọc lý câu hỏi trước, sau đó tìm thông tin trong đoạn văn",
                        fontSize = 13.sp,
                        color = Color(0xFF1E40AF),
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentQuestionIndex > 0) {
                            currentQuestionIndex--
                            selectedAnswer = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavBarText
                    ),
                    enabled = currentQuestionIndex > 0
                ) {
                    Text(
                        text = "Câu trước",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswer = null
                        } else {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    )
                ) {
                    Text(
                        text = if (currentQuestionIndex < questions.size - 1) "Câu tiếp" else "Hoàn thành",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingAnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFFD1FAE5) else Color(0xFFF9FAFB),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) Color(0xFF10B981) else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF10B981)
                )
            )
            Text(
                text = text,
                fontSize = 15.sp,
                color = MainText,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

