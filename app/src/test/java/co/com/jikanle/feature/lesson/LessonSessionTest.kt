package co.com.jikanle.feature.lesson

import co.com.jikanle.core.domain.repository.LessonProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class LessonSessionTest {
    @Test fun acknowledgementAdvancesButDoesNotCompleteEarly() {
        assertEquals(LessonProgress(1), advanceLesson(LessonProgress(), 8))
    }
    @Test fun finalAcknowledgementCompletesOnce() {
        val completed = advanceLesson(LessonProgress(7), 8)
        assertEquals(LessonProgress(7, true), completed)
        assertEquals(completed, advanceLesson(completed, 8))
    }
    @Test fun shortenedDeckClampsOldPosition() {
        assertEquals(LessonProgress(2, true), advanceLesson(LessonProgress(7), 3))
    }
    @Test fun emptyDeckCannotBeCompleted() {
        assertEquals(LessonProgress(), advanceLesson(LessonProgress(), 0))
    }
}
