package co.com.jikanle.feature.lesson

import co.com.jikanle.core.domain.repository.LessonProgress

/** A manual step acknowledgement, independent of slide content or research scoring. */
internal fun advanceLesson(progress: LessonProgress, slideCount: Int): LessonProgress {
    if (slideCount <= 0 || progress.completed) return progress
    val step = progress.step.coerceIn(0, slideCount - 1)
    return if (step == slideCount - 1) LessonProgress(step, completed = true) else LessonProgress(step + 1)
}
