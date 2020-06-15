package com.example.myapp

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapp.database.AppData
import com.example.myapp.database.AppDatabase
import com.example.myapp.database.AppDatabaseDao
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var  appDao: AppDatabaseDao
    private lateinit var db: AppDatabase
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        //Using an in-memory database because the information stored here disappears
        //when the process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        appDao = db.appDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb(){
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        assertEquals("com.example.myapp", appContext.packageName)
    }

    @Test
    @Throws(Exception::class)
    fun insertAppData(){
        val pm = appContext.packageManager
        val app = pm.getInstalledApplications(0)[0]
        val appData = AppData(app.loadLabel(pm).toString(),app.loadIcon(pm),"テスト",app.publicSourceDir)
        appDao.insert(appData)
    }

}
