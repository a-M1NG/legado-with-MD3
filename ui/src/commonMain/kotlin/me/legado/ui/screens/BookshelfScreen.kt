package me.legado.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import me.legado.core.Book
import me.legado.core.BookRepository
import me.legado.ui.components.BookshelfGrid

/**
 * 书架主屏幕 - Compose Multiplatform
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    bookRepository: BookRepository,
    onBookClick: (Book) -> Unit,
    onAddBook: () -> Unit,
    onSearch: () -> Unit
) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    
    LaunchedEffect(bookRepository) {
        bookRepository.getBooks().collectLatest { bookList ->
            books = bookList
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的书架") },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = onAddBook) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (books.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "书架空空如也",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "点击右上角添加按钮导入书籍",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                BookshelfGrid(
                    books = books,
                    onBookClick = onBookClick,
                    columns = 3
                )
            }
        }
    }
}
