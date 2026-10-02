import { assertEquals, assertThrows } from "https://deno.land/std@0.224.0/assert/mod.ts";
import { discoverJsonLd, type EventSource, nextReview, normalizeEvent } from "./discovery.ts";
const source: EventSource = { id: "fixture", url: "https://example.org/agenda", cadence: "weekly", adapter: "jsonld", automationApproved: false };
const event = { "@type": "Event", name: "Language exchange", url: "/event/1?utm_source=test", startDate: "2026-10-05T17:00:00-05:00", location: { name: "Test venue" } };
Deno.test("normalizes timezone and deduplicates graph entries without guessing language", () => {
  const result = discoverJsonLd({ "@graph": [event, event] }, source);
  assertEquals(result.candidates.length, 1);
  assertEquals(result.candidates[0].startsAt, "2026-10-05T22:00:00.000Z");
  assertEquals(result.candidates[0].language, null);
  assertEquals(result.candidates[0].reviewStatus, "pending");
});
Deno.test("rejects ambiguous dates, missing venue and external URLs", () => {
  for (const change of [{ startDate: "2026-10-05" }, { startDate: "2026-02-30T17:00:00Z" }, { location: null }, { url: "https://evil.example/a" }, { endDate: "2026-10-04T17:00:00Z" }]) {
    assertThrows(() => normalizeEvent({ ...event, ...change }, source));
  }
});
Deno.test("conflicts are quarantined and cancellation is preserved", () => {
  assertEquals(discoverJsonLd([event, { ...event, name: "Changed" }, event], source).candidates.length, 0);
  assertEquals(normalizeEvent({ ...event, eventStatus: "https://schema.org/EventCancelled" }, source).sourceStatus, "cancelled");
});
Deno.test("weekly and month-end schedules are deterministic", () => {
  assertEquals(nextReview(new Date("2026-01-31T12:00:00Z"), "monthly").toISOString(), "2026-02-28T12:00:00.000Z");
  assertEquals(nextReview(new Date("2026-10-02T12:00:00Z"), "weekly").toISOString(), "2026-10-09T12:00:00.000Z");
});
