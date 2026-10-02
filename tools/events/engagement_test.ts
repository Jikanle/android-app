import { assertEquals } from "https://deno.land/std@0.224.0/assert/mod.ts";
import { type Attendance, consecutiveLanguageReturn, type Preference, suggestConnections } from "./engagement.ts";
const me: Preference = { userId: "a", languages: ["ja"], interests: ["music"], professionalDomains: ["computing"], matchingOptIn: true, metricsOptIn: true };
const first: Attendance = { userId: "a", eventId: "1", startsAt: "2026-10-01T22:00:00Z", language: "ja", verified: true, analyticsAllowed: true };
const next = { ...first, eventId: "2", startsAt: "2026-10-06T22:00:00Z" };
Deno.test("connections require both opt-ins and shared declared context", () => {
  const peer = { ...me, userId: "b" };
  assertEquals(suggestConnections(me, [peer]).length, 1);
  assertEquals(suggestConnections(me, [{ ...peer, matchingOptIn: false }]), []);
  assertEquals(suggestConnections({ ...me, matchingOptIn: false }, [peer]), []);
  assertEquals(suggestConnections(me, [{ ...peer, languages: ["pt"] }]), []);
});
Deno.test("counts verified returns once and excludes immature windows", () => {
  assertEquals(consecutiveLanguageReturn([first, first, next], [me], new Date("2026-10-09T22:00:00Z")), { ja: { eligible: 1, returned: 1 } });
  assertEquals(consecutiveLanguageReturn([first, next], [me], new Date("2026-10-07T22:00:00Z")), {});
});
Deno.test("clicks, absent collection consent and withdrawn metrics never become attendance", () => {
  const now = new Date("2026-10-10T22:00:00Z");
  assertEquals(consecutiveLanguageReturn([{ ...first, verified: false }], [me], now), {});
  assertEquals(consecutiveLanguageReturn([{ ...first, analyticsAllowed: false }], [me], now), {});
  assertEquals(consecutiveLanguageReturn([first, next], [{ ...me, metricsOptIn: false }], now), {});
});
Deno.test("next visit in another language breaks a consecutive same-language return", () => {
  const middle = { ...first, eventId: "3", language: "pt", startsAt: "2026-10-04T22:00:00Z" };
  assertEquals(consecutiveLanguageReturn([next, middle, first], [me], new Date("2026-10-09T22:00:00Z")), { ja: { eligible: 1, returned: 0 } });
});
