package co.com.jikanle.core.data

import co.com.jikanle.core.data.mapper.toDiscoverableEvent
import co.com.jikanle.core.data.mapper.toEntity
import co.com.jikanle.core.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Test

class EventContractTest {
    @Test fun calendarFieldsSurviveCacheRoundTrip() {
        val event = AppJson.decodeFromString<Event>("""{
          "id":"event-1","title":"Encuentro","organization_id":"org-1",
          "starts_at":"2026-10-01T22:00:00Z","language":"ja","level":"beginner",
          "summary":"Conversation","timezone":"America/Bogota","tags":["music"],
          "lat":4.6,"lng":-74.1,"visibility":"public","future_column":true
        }""")
        val restored = event.toEntity(null).toDiscoverableEvent().event
        assertEquals(event, restored)
    }
}
