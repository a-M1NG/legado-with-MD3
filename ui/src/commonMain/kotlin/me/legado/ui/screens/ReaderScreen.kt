package me.legado.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import me.legado.core.Chapter
import me.legado.jsengine.ReaderController
import me.legado.jsengine.ReaderTheme

/**
 * 阅读器屏幕 - Compose Multiplatform
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapter: Chapter,
    content: List<String>,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    theme: ReaderTheme = ReaderTheme.LIGHT
) {
    val controller = remember { ReaderController() }
    var currentPage by controller.currentPage.collectAsState()
    
    LaunchedEffect(content) {
        controller.loadChapter(content)
    }
    
    // 计算每页显示的行数 (简化实现)
    val pageSize = 20
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = chapter.title,
                        maxLines = 1,
                        fontSize = 14.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Default.MoreVert, contentDescription = "菜单")
                    }
                }
            )
        },
        bottomBar = {
            ReaderBottomBar(
                currentPage = currentPage,
                totalPages = (content.size - 1) / pageSize + 1,
                onPrevPage = { controller.prevPage() },
                onNextPage = { controller.nextPage(pageSize) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(theme.backgroundColor))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        if (dragAmount > 0) {
                            controller.prevPage()
                        } else if (dragAmount < 0) {
                            controller.nextPage(pageSize)
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 显示当前页内容
                val pageContent = content.drop(currentPage * pageSize).take(pageSize)
                
                pageContent.forEach { line ->
                    Text(
                        text = line,
                        color = Color(theme.textColor),
                        fontSize = theme.fontSize.sp,
                        lineHeight = (theme.fontSize * theme.lineSpacing).sp,
                        modifier = Modifier.padding(bottom = (8 * theme.paragraphSpacing).dp),
                        textAlign = TextAlign.Justify
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 页码显示
                Text(
                    text = "${currentPage + 1} / ${(content.size - 1) / pageSize + 1}",
                    color = Color(theme.textColor).copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 阅读器底部控制栏
 */
@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    totalPages: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevPage,
                enabled = currentPage > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("上一页")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "$currentPage / ${totalPages - 1}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            OutlinedButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages - 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("下一页")
            }
        }
    }
}

/**
 * 阅读设置对话框
 */
@Composable
fun ReaderSettingsDialog(
    currentTheme: ReaderTheme,
    onThemeChange: (ReaderTheme) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("阅读设置") },
        text = {
            Column {
                Text("主题选择:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = currentTheme == ReaderTheme.LIGHT,
                        onClick = { onThemeChange(ReaderTheme.LIGHT) },
                        label = { Text("默认") }
                    )
                    FilterChip(
                        selected = currentTheme == ReaderTheme.DARK,
                        onClick = { onThemeChange(ReaderTheme.DARK) },
                        label = { Text("深色") }
                    )
                    FilterChip(
                        selected = currentTheme == ReaderTheme.EYE_PROTECTION,
                        onClick = { onThemeChange(ReaderTheme.EYE_PROTECTION) },
                        label = { Text("护眼") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("字体大小:")
                Slider(
                    value = currentTheme.fontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..24f,
                    steps = 11
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}
