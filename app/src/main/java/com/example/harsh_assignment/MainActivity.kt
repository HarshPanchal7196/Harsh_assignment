package com.example.harsh_assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.harsh_assignment.ui.LearningDashboardApp
import com.example.harsh_assignment.ui.theme.Harsh_assignmentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Harsh_assignmentTheme {
                LearningDashboardApp()
            }
        }
    }
}
