package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.AttendanceRepository
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AttendanceViewModel
import com.example.viewmodel.AttendanceViewModelFactory

class MainActivity : ComponentActivity() {
    
    // Lazy initialize App Database, Repository, and ViewModel
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { AttendanceRepository(database.attendanceDao()) }
    
    private val viewModel: AttendanceViewModel by viewModels {
        AttendanceViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
