package me.legado.ui.search

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
import me.legado.core.parser.model.SearchBook

/**
 * 搜索屏幕组件
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBookClick: (SearchBook) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索书籍") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            viewModel.search(searchQuery)
                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索输入框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("输入书名或作者") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "清除"
                            )
                        }
                    }
                }
            )

            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("输入关键词开始搜索")
                    }
                }
                
                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SearchUiState.Success -> {
                    if (state.books.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("未找到相关书籍")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.books, key = { it.bookUrl }) { book ->
                                SearchBookItem(
                                    searchBook = book,
                                    onClick = { onBookClick(book) }
                                )
                            }
                        }
                    }
                }
                
                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("搜索失败：${state.message}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.search(searchQuery) }) {
                                Text("重试")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 搜索结果项组件
 */
@Composable
private fun SearchBookItem(
    searchBook: SearchBook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面图片（占位）
            Surface(
                modifier = Modifier
                    .size(60.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                if (searchBook.coverUrl != null) {
                    // TODO: 使用 AsyncImage 加载封面
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 书籍信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = searchBook.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = searchBook.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                
                if (searchBook.lastChapter != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = searchBook.lastChapter,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (searchBook.intro != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = searchBook.intro!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 来源标识
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = searchBook.sourceName ?: "未知",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 搜索 UI 状态
 */
sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val books: List<SearchBook>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

/**
 * 搜索 ViewModel
 */
class SearchViewModel(
    private val searchExecutor: me.legado.core.parser.SearchExecutor
) {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    fun search(query: String) {
        _uiState.value = SearchUiState.Loading
        
        // TODO: 在实际项目中应该使用协程作用域
        kotlinx.coroutines.GlobalScope.launch {
            try {
                val results = searchExecutor.multiSourceSearch(query)
                _uiState.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "搜索失败")
            }
        }
    }
}
