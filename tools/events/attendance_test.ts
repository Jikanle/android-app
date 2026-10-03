import { assertEquals, assertThrows } from "https://deno.land/std@0.224.0/assert/mod.ts";
import { validateBatch } from "./attendance.ts";
const batch = {
  event_id: "00000000-0000-4000-8000-000000000001", evidence_ref: "roster:synthetic",
  rows: [{ user_id: "00000000-0000-4000-8000-000000000002", checked_in_at: "2026-10-01T17:00:00-05:00" }],
};
Deno.test("attendance accepts UUID roster without deriving consent", () => {
  assertEquals(validateBatch(batch), batch);
});
Deno.test("attendance rejects duplicates, emails, forged consent and naive timestamps", () => {
  for (const input of [null, {}, { ...batch, rows: [] }, { ...batch, rows: [batch.rows[0], batch.rows[0]] },
    { ...batch, rows: [{ ...batch.rows[0], analytics_allowed: true }] },
    { ...batch, rows: [{ ...batch.rows[0], user_id: "someone@example.test" }] },
    { ...batch, rows: [{ ...batch.rows[0], checked_in_at: "2026-10-01T17:00:00" }] }]) {
    assertThrows(() => validateBatch(input));
  }
});
