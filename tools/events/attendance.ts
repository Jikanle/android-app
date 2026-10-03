export interface AttendanceBatch {
  event_id: string;
  evidence_ref: string;
  rows: Array<{ user_id: string; checked_in_at: string }>;
}
const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function validateBatch(input: unknown): AttendanceBatch {
  if (!input || typeof input !== "object" || Array.isArray(input)) throw new Error("Expected batch object");
  const batch = input as Record<string, unknown>;
  if (Object.keys(batch).some(k => !["event_id", "evidence_ref", "rows"].includes(k)) ||
    typeof batch.event_id !== "string" || !uuid.test(batch.event_id) ||
    typeof batch.evidence_ref !== "string" || !/^roster:[a-zA-Z0-9_-]{1,64}$/.test(batch.evidence_ref) ||
    !Array.isArray(batch.rows) || batch.rows.length < 1 || batch.rows.length > 100) {
    throw new Error("Invalid event, evidence reference or batch size (1-100)");
  }
  const seen = new Set<string>();
  const rows = batch.rows.map((raw: unknown) => {
    if (!raw || typeof raw !== "object" || Array.isArray(raw)) throw new Error("Invalid roster row");
    const row = raw as Record<string, unknown>;
    if (Object.keys(row).some(k => !["user_id", "checked_in_at"].includes(k)) ||
      typeof row.user_id !== "string" || !uuid.test(row.user_id) ||
      typeof row.checked_in_at !== "string" ||
      !/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:\d{2})$/.test(row.checked_in_at) ||
      !Number.isFinite(Date.parse(row.checked_in_at))) throw new Error("Expected UUID and offset-aware check-in only");
    const id = row.user_id.toLowerCase();
    if (seen.has(id)) throw new Error("Duplicate participant");
    seen.add(id);
    return { user_id: id, checked_in_at: row.checked_in_at };
  });
  return { event_id: batch.event_id, evidence_ref: batch.evidence_ref, rows };
}
