package me.legado.ui.source

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import me.legado.core.data.model.BookSource

/**
 * 书源管理屏幕
 */
@Composable
fun BookSourceScreen(
    viewModel: BookSourceViewModel,
    onNavigateBack: () -> Unit,
    onEditSource: (BookSource) -> Unit,
    onAddSource: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书源管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddSource) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加书源"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSource,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加书源"
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BookSourceUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is BookSourceUiState.Success -> {
                if (state.sources.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LibraryBooks,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "暂无书源",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击右上角 + 添加书源",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.sources, key = { it.bookSourceUrl }) { source ->
                            BookSourceItem(
                                bookSource = source,
                                onClick = { onEditSource(source) },
                                onToggleEnable = { 
                                    viewModel.toggleSourceEnabled(source, !source.enabled)
                                }
                            )
                        }
                    }
                }
            }
            
            is BookSourceUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败：${state.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadSources() }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 书源列表项
 */
@Composable
private fun BookSourceItem(
    bookSource: BookSource,
    onClick: () -> Unit,
    onToggleEnable: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bookSource.sourceName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 启用状态标签
                    AssistChip(
                        onClick = onToggleEnable,
                        label = { 
                            Text(
                                if (bookSource.enabled) "已启用" else "已禁用",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (bookSource.enabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (bookSource.enabled) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "类型：${if (bookSource.searchUrl.isNotBlank()) "搜索" else ""}${if (bookSource.tocUrl.isNotBlank()) "目录" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                
                if (bookSource.lastUpdateTime > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "最后更新：${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(bookSource.lastUpdateTime))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 书源 UI 状态
 */
sealed class BookSourceUiState {
    object Loading : BookSourceUiState()
    data class Success(val sources: List<BookSource>) : BookSourceUiState()
    data class Error(val message: String) : BookSourceUiState()
}

/**
 * 书源管理 ViewModel
 */
class BookSourceViewModel(
    private val bookSourceRepository: me.legado.core.data.repository.BookSourceRepository
) {
    private val _uiState = MutableStateFlow<BookSourceUiState>(BookSourceUiState.Loading)
    val uiState: StateFlow<BookSourceUiState> = _uiState

    init {
        loadSources()
    }

    fun loadSources() {
        _uiState.value = BookSourceUiState.Loading
        
        kotlinx.coroutines.GlobalScope.launch {
            try {
                val sources = bookSourceRepository.selectAllBookSources()
                _uiState.value = BookSourceUiState.Success(sources)
            } catch (e: Exception) {
                _uiState.value = BookSourceUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun toggleSourceEnabled(source: BookSource, enabled: Boolean) {
        kotlinx.coroutines.GlobalScope.launch {
            try {
                val updatedSource = source.copy(enabled = enabled, lastUpdateTime = System.currentTimeMillis())
                bookSourceRepository.insertBookSource(updatedSource)
                loadSources() // 重新加载
            } catch (e: Exception) {
                // 可以添加错误提示
            }
        }
    }
}

// 需要导入协程作用域
private fun launch(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(block = block)
}
