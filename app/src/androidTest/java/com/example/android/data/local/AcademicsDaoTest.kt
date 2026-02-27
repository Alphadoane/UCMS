package com.example.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AcademicsDaoTest {
    private lateinit var academicsDao: AcademicsDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        academicsDao = db.academicsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetCourses() = runBlocking {
        val course = CourseEntity(
            course_code = "CS101",
            course_name = "Intro to CS",
            credits = 3,
            semester = "Sem 1",
            status = "Enrolled"
        )
        academicsDao.insertCourses(listOf(course))
        
        val courses = academicsDao.getCourses().first()
        assertEquals(courses[0].course_code, "CS101")
    }
}
