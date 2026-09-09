package co.com.jikanle

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import co.com.jikanle.core.data.local.BetaDatabase
import co.com.jikanle.core.data.local.OutboxRow
import co.com.jikanle.core.data.local.ProgressRow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BetaStorageTest {
    @Test fun pendingDeliveryIsScopedToItsOwner() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, BetaDatabase::class.java).build()
        try {
            val dao = db.betaDao()
            dao.enqueue(OutboxRow("a1", "user-a", "{}", 1))
            dao.enqueue(OutboxRow("b1", "user-b", "{}", 2))
            assertEquals(listOf("a1"), dao.pending("user-a").map { it.id })
            dao.acknowledge("a1")
            assertEquals(emptyList<OutboxRow>(), dao.pending("user-a"))
            assertEquals(listOf("b1"), dao.pending("user-b").map { it.id })
        } finally { db.close() }
    }

    @Test fun clearingSubmissionsPreservesLearningProgress() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, BetaDatabase::class.java).build()
        try {
            val dao = db.betaDao()
            val progress = ProgressRow("lesson-a", 3, false)
            dao.save(progress)
            dao.enqueue(OutboxRow("a1", "user-a", "{}", 1))
            dao.clearOutbox()
            assertEquals(progress, dao.progress("lesson-a").first())
            assertEquals(emptyList<OutboxRow>(), dao.pending("user-a"))
        } finally { db.close() }
    }
}
