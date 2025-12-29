package com.example.lingora_fe.admin.dashboard.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.dashboard.domain.model.*
import com.example.lingora_fe.admin.dashboard.presentation.DashboardViewModel
import com.example.lingora_fe.core.ui.theme.*
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


// Tab items
private enum class DashboardTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    USERS("Users", Icons.Default.People),
    LEARNING("Learning", Icons.Default.School),
    REVENUE("Revenue", Icons.Default.AttachMoney),
    EXAMS("Exams", Icons.Default.Assignment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { DashboardTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    // Display label from state
    val dateRangeText = state.dateRangeLabel

    // Trigger API loading when tab changes
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {


            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = GradientStart,
                edgePadding = 8.dp
            ) {
                DashboardTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) }
                    )
                }
            }
            
            // Date Filter Row (Below Tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Box {
                     OutlinedButton(
                         onClick = { showDateRangePicker = true },
                         shape = RoundedCornerShape(8.dp),
                         contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                     ) {
                         Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(text = dateRangeText, style = MaterialTheme.typography.labelLarge)
                     }

                     DatePresetDropdown(
                         expanded = showDateRangePicker,
                         onDismiss = { showDateRangePicker = false },
                         onPresetSelected = { start, end, label ->
                             viewModel.onDateRangeSelected(start, end, label)
                             showDateRangePicker = false
                         }
                     )
                 }
            }

            if (state.isLoading && state.overview == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (DashboardTab.entries[page]) {
                        DashboardTab.OVERVIEW -> OverviewTab(
                            overview = state.overview,
                            activities = state.activities
                        )
                        DashboardTab.USERS -> UsersTab(
                            userAnalytics = state.userAnalytics,
                            isLoading = state.isLoadingUsers
                        )
                        DashboardTab.LEARNING -> LearningTab(
                            learning = state.learningAnalytics,
                            isLoading = state.isLoadingLearning
                        )
                        DashboardTab.REVENUE -> RevenueTab(
                            revenue = state.revenueAnalytics,
                            isLoading = state.isLoadingRevenue
                        )
                        DashboardTab.EXAMS -> ExamsTab(
                            exam = state.examAnalytics,
                            isLoading = state.isLoadingExams
                        )
                    }
                }
            }
        }
    }
}

// ==================== OVERVIEW TAB ====================
// ==================== OVERVIEW TAB ====================
@Composable
private fun OverviewTab(
    overview: DashboardOverview?,
    activities: List<Activity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            overview?.let { KPICardsSection(it) }
        }
        item {
            if (activities.isNotEmpty()) {
                RecentActivitiesSection(activities)
            } else {
                EmptyDataMessage(message = "No recent activities")
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun KPICardsSection(overview: DashboardOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KPICard(
            modifier = Modifier.fillMaxWidth(),
            title = "Users",
            value = overview.users.total.toString(),
            subtitle = "+${overview.users.new} new",
            icon = Icons.Default.People,
            iconBackground = Color(0xFF3B82F6),
            growth = overview.users.growth
        )
        KPICard(
            modifier = Modifier.fillMaxWidth(),
            title = "Study Sets",
            value = overview.studySets.total.toString(),
            subtitle = "${overview.studySets.published} published",
            icon = Icons.Default.LibraryBooks,
            iconBackground = Color(0xFF8B5CF6)
        )
        KPICard(
            modifier = Modifier.fillMaxWidth(),
            title = "Revenue",
            value = formatCurrency(overview.revenue.thisMonth),
            subtitle = "This period",
            icon = Icons.Default.AttachMoney,
            iconBackground = Color(0xFF10B981),
            growth = overview.revenue.growth
        )
        KPICard(
            modifier = Modifier.fillMaxWidth(),
            title = "Exams",
            value = overview.exams.total.toString(),
            subtitle = "${overview.exams.totalAttempts} attempts",
            icon = Icons.Default.Assignment,
            iconBackground = Color(0xFFF59E0B)
        )
    }
}

@Composable
private fun KPICard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Color,
    growth: Int? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBackground.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconBackground, modifier = Modifier.size(24.dp))
                }
                growth?.let { GrowthBadge(it) }
            }
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = NavBarText)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = NavBarText.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun GrowthBadge(growth: Int) {
    val isPositive = growth >= 0
    val backgroundColor = if (isPositive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val textColor = if (isPositive) Color(0xFF16A34A) else Color(0xFFDC2626)
    val icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    
    Surface(shape = RoundedCornerShape(8.dp), color = backgroundColor) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = textColor)
            Text(
                text = "${if (isPositive) "+" else ""}$growth%",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentActivitiesSection(activities: List<Activity>) {
    DashboardCard(title = "Recent Activities", subtitle = "Last ${activities.size}") {
        activities.forEach { activity ->
            ActivityItem(activity)
        }
    }
}

@Composable
private fun ActivityItem(activity: Activity) {
    val (iconBg, icon) = when (activity.type) {
        ActivityType.USER_REGISTER -> Color(0xFF3B82F6) to Icons.Default.PersonAdd
        ActivityType.PURCHASE -> Color(0xFF10B981) to Icons.Default.ShoppingCart
        ActivityType.EXAM_COMPLETED -> Color(0xFF8B5CF6) to Icons.Default.CheckCircle
        ActivityType.UNKNOWN -> Color.Gray to Icons.Default.Info
    }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = activity.user.username, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = activity.action, style = MaterialTheme.typography.bodySmall, color = NavBarText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        activity.details?.amount?.let { amount ->
            Text(text = formatCurrency(amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = GradientStart)
        }
    }
}

// ==================== USERS TAB ====================
@Composable
private fun UsersTab(userAnalytics: UserAnalytics?, isLoading: Boolean = false) {
    if (isLoading || userAnalytics == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Users
        userAnalytics.activeUsers?.let { active ->
            item {
                DashboardCard(title = "Active Users") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(label = "Daily", value = active.daily.toString(), color = Color(0xFF3B82F6))
                        StatItem(label = "Weekly", value = active.weekly.toString(), color = Color(0xFF8B5CF6))
                        StatItem(label = "Monthly", value = active.monthly.toString(), color = Color(0xFF10B981))
                    }
                }
            }
        }

        // User Growth Chart
        item {
            DashboardCard(title = "User Growth", subtitle = "Last ${userAnalytics.growth.size} days") {
                if (userAnalytics.growth.size >= 2) {
                    UserGrowthChart(data = userAnalytics.growth)
                } else {
                    EmptyDataMessage(message = "Not enough data to display chart")
                }
            }
        }

        // By Proficiency
        item {
            DashboardCard(title = "By Proficiency") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    userAnalytics.byProficiency.forEach { item ->
                        StatItem(label = item.proficiency.replace("_", " "), value = item.count.toString())
                    }
                }
            }
        }

        // By Status
        item {
            DashboardCard(title = "By Status") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    userAnalytics.byStatus.forEach { item ->
                        StatusChip(status = item.status, count = item.count)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun UserGrowthChart(data: List<UserGrowth>) {
    val primaryColor = Color(0xFF3B82F6).toArgb()

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                axisLeft.setDrawGridLines(true)
                animateY(1000)
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, item -> 
                Entry(index.toFloat(), item.count.toFloat()) 
            }
            
            if (entries.isNotEmpty()) {
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { formatDateForChart(it.date) })
                
                val dataSet = LineDataSet(entries, "Growth").apply {
                    color = primaryColor
                    setDrawCircles(false)
                    setDrawFilled(true)
                    fillColor = primaryColor
                    fillAlpha = 50
                    lineWidth = 2f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                }
                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        }
    )
}

private fun formatDateForChart(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
        val date = inputFormat.parse(dateString.take(10)) // Take first 10 chars (yyyy-MM-dd)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

// ==================== LEARNING TAB ====================
@Composable
private fun LearningTab(learning: LearningAnalytics?, isLoading: Boolean = false) {
    if (isLoading || learning == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Overview
        item {
            DashboardCard(title = "Learning Overview") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(label = "Categories", value = learning.categories.total.toString(), color = Color(0xFF3B82F6))
                    StatItem(label = "Topics", value = learning.topics.total.toString(), color = Color(0xFF8B5CF6))
                    StatItem(label = "Words", value = learning.words.total.toString(), color = Color(0xFF10B981))
                }
            }
        }

        // Learning Trend Chart
        item {
            DashboardCard(title = "Learning Trend", subtitle = "Words learned per day") {
                val trend = learning.learningTrend
                if (trend != null && trend.size >= 2) {
                    LearningTrendChart(data = trend)
                } else {
                    EmptyDataMessage(message = "Not enough data to display chart")
                }
            }
        }

        // Popular Categories
        if (learning.categories.popular.isNotEmpty()) {
            item {
                DashboardCard(title = "Popular Categories") {
                    learning.categories.popular.take(5).forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = category.name, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${category.usersCount} users", style = MaterialTheme.typography.bodySmall, color = NavBarText)
                        }
                    }
                }
            }
        }

        // Popular Topics
        if (learning.topics.popular.isNotEmpty()) {
            item {
                DashboardCard(title = "Popular Topics") {
                    learning.topics.popular.take(5).forEach { topic ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = topic.name, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${topic.usersCount} users", style = MaterialTheme.typography.bodySmall, color = NavBarText)
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun LearningTrendChart(data: List<LearningTrend>) {
    val primaryColor = Color(0xFF8B5CF6).toArgb() // Purple

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                axisLeft.setDrawGridLines(true)
                animateY(1000)
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, item -> 
                Entry(index.toFloat(), item.wordsLearned.toFloat()) 
            }
            
            if (entries.isNotEmpty()) {
                 chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { formatDateForChart(it.date) })

                val dataSet = LineDataSet(entries, "Words Learned").apply {
                    color = primaryColor
                    setDrawCircles(false)
                    setDrawFilled(true)
                    fillColor = primaryColor
                    fillAlpha = 50
                    lineWidth = 2f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                }
                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        }
    )
}

// ==================== REVENUE TAB ====================
@Composable
private fun RevenueTab(revenue: RevenueAnalytics?, isLoading: Boolean = false) {
    if (isLoading || revenue == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Transaction Stats
        item {
            DashboardCard(title = "Transactions") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(label = "Success", value = revenue.transactions.success.toString(), color = Color(0xFF10B981))
                    StatItem(label = "Pending", value = revenue.transactions.pending.toString(), color = Color(0xFFF59E0B))
                    StatItem(label = "Failed", value = revenue.transactions.failed.toString(), color = Color(0xFFEF4444))
                    StatItem(label = "Rate", value = "${revenue.transactions.successRate}%", color = Color(0xFF3B82F6))
                }
            }
        }

        // Revenue Trend Chart
        item {
            DashboardCard(title = "Revenue Trend", subtitle = "Monthly") {
                if (revenue.trend.size >= 2) {
                    RevenueTrendChart(data = revenue.trend)
                } else {
                    EmptyDataMessage(message = "Not enough data to display chart")
                }
            }
        }

        // Top Selling
        if (revenue.topSelling.isNotEmpty()) {
            item {
                DashboardCard(title = "Top Selling Study Sets") {
                    revenue.topSelling.take(5).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "by ${item.ownerUsername} • ${item.sales} sales", style = MaterialTheme.typography.bodySmall, color = NavBarText)
                            }
                            Text(text = formatCurrency(item.revenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = GradientStart)
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun RevenueTrendChart(data: List<RevenueTrend>) {
    val primaryColor = Color(0xFF10B981).toArgb() // Green

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                axisLeft.setDrawGridLines(true)
                animateY(1000)
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, item -> 
                BarEntry(index.toFloat(), item.revenue.toFloat() / 1_000_000f) 
            }
            
            if (entries.isNotEmpty()) {
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.month })
                
                val dataSet = BarDataSet(entries, "Revenue (M)").apply {
                    color = primaryColor
                    setDrawValues(false)
                }
                chart.data = BarData(dataSet)
                chart.invalidate()
            }
        }
    )
}

// ==================== EXAMS TAB ====================
@Composable
private fun ExamsTab(exam: ExamAnalytics?, isLoading: Boolean = false) {
    if (isLoading || exam == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Stats
        item {
            DashboardCard(title = "Exam Overview") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(label = "Exams", value = exam.overview.totalExams.toString(), color = Color(0xFF3B82F6))
                    StatItem(label = "Attempts", value = exam.overview.totalAttempts.toString(), color = Color(0xFF8B5CF6))
                    StatItem(label = "Completed", value = exam.overview.completedAttempts.toString(), color = Color(0xFF10B981))
                    StatItem(label = "Rate", value = "${exam.overview.completionRate}%", color = Color(0xFFF59E0B))
                }
            }
        }

        // Additional Stats
        item {
            DashboardCard(title = "Performance") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    exam.averageScore?.let { StatItem(label = "Avg Score", value = String.format("%.1f", it), color = Color(0xFF3B82F6)) }
                    exam.averageTimeMinutes?.let { StatItem(label = "Avg Time", value = "${it}m", color = Color(0xFF8B5CF6)) }
                }
            }
        }

        // Attempts Trend Chart
        item {
            DashboardCard(title = "Attempts Trend", subtitle = "Daily") {
                if (exam.trend.size >= 2) {
                    ExamTrendChart(data = exam.trend)
                } else {
                    EmptyDataMessage(message = "Not enough data to display chart")
                }
            }
        }

        // Score Distribution Chart
        item {
            DashboardCard(title = "Score Distribution") {
                val dist = exam.scoreDistribution
                if (dist != null && dist.size >= 2) {
                    ScoreDistributionChart(data = dist)
                } else {
                    EmptyDataMessage(message = "Not enough data to display chart")
                }
            }
        }

        // Top Exams
        if (exam.examPerformance.isNotEmpty()) {
            item {
                DashboardCard(title = "Top Exams") {
                    exam.examPerformance.take(5).forEach { examItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = examItem.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = examItem.code, style = MaterialTheme.typography.bodySmall, color = NavBarText)
                            }
                            Text(text = "${examItem.attempts} attempts", style = MaterialTheme.typography.bodySmall, color = GradientStart)
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun ExamTrendChart(data: List<ExamTrend>) {
    val primaryColor = Color(0xFFF59E0B).toArgb() // Orange

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                axisLeft.setDrawGridLines(true)
                animateY(1000)
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, item -> 
                Entry(index.toFloat(), item.attempts.toFloat()) 
            }
            
            if (entries.isNotEmpty()) {
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { formatDateForChart(it.date) })

                val dataSet = LineDataSet(entries, "Attempts").apply {
                    color = primaryColor
                    setDrawCircles(false)
                    setDrawFilled(true)
                    fillColor = primaryColor
                    fillAlpha = 50
                    lineWidth = 2f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                }
                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        }
    )
}

@Composable
private fun ScoreDistributionChart(data: List<ScoreDistribution>) {
    val primaryColor = Color(0xFF6366F1).toArgb() // Indigo

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                axisLeft.setDrawGridLines(true)
                animateY(1000)
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, item -> 
                BarEntry(index.toFloat(), item.count.toFloat()) 
            }
            
            if (entries.isNotEmpty()) {
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.range })

                val dataSet = BarDataSet(entries, "Distribution").apply {
                    color = primaryColor
                    setDrawValues(false)
                }
                chart.data = BarData(dataSet)
                chart.invalidate()
            }
        }
    )
}

// ==================== COMMON COMPONENTS ====================
@Composable
private fun DashboardCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                subtitle?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = NavBarText) }
            }
            content()
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = NavBarText)
    }
}

@Composable
private fun StatusChip(status: String, count: Int) {
    val color = when (status) {
        "ACTIVE" -> Color(0xFF10B981)
        "INACTIVE" -> Color(0xFF6B7280)
        "BANNED" -> Color(0xFFEF4444)
        "SUSPENDED" -> Color(0xFFF59E0B)
        else -> Color.Gray
    }
    
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Text(text = "$status ($count)", style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

private fun formatCurrency(amount: Long): String {
    return NumberFormat.getInstance(Locale("vi", "VN")).format(amount) + " VNĐ"
}

@Composable
private fun EmptyDataMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = NavBarText,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = NavBarText
            )
        }
    }
}

@Composable
private fun DatePresetDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPresetSelected: (Long, Long, String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        val today = java.util.Calendar.getInstance()

        // 7 Days
        DropdownMenuItem(
            text = { Text("7 days") },
            onClick = {
                val end = System.currentTimeMillis()
                val start = today.clone() as java.util.Calendar
                start.add(java.util.Calendar.DAY_OF_YEAR, -7)
                onPresetSelected(start.timeInMillis, end, "7 days")
            }
        )

        // 15 Days
        DropdownMenuItem(
            text = { Text("15 days") },
            onClick = {
                val end = System.currentTimeMillis()
                val start = today.clone() as java.util.Calendar
                start.add(java.util.Calendar.DAY_OF_YEAR, -15)
                onPresetSelected(start.timeInMillis, end, "15 days")
            }
        )

        // 30 Days
        DropdownMenuItem(
            text = { Text("30 days") },
            onClick = {
                val end = System.currentTimeMillis()
                val start = today.clone() as java.util.Calendar
                start.add(java.util.Calendar.DAY_OF_YEAR, -30)
                onPresetSelected(start.timeInMillis, end, "30 days")
            }
        )
    }
}
