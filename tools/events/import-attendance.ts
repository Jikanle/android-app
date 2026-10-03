/** Authorized host roster only. Default is offline validation, never inferred attendance. */
import { validateBatch } from "./attendance.ts";

async function main() {
  const [path, mode] = Deno.args;
  if (!path || (mode !== undefined && mode !== "--apply")) {
    throw new Error("Usage: import-attendance.ts private-roster.json [--apply]");
  }
  const file = await Deno.open(path, { read: true });
  let text: string;
  try {
    const buffer = new Uint8Array(128 * 1024 + 1);
    let size = 0;
    while (size < buffer.length) {
      const count = await file.read(buffer.subarray(size));
      if (count === null) break;
      size += count;
    }
    if (size > 128 * 1024) throw new Error("Roster exceeds 128 KiB");
    text = new TextDecoder().decode(buffer.subarray(0, size));
  } finally { file.close(); }
  const batch = validateBatch(JSON.parse(text));
  if (mode !== "--apply") {
    console.log(`Validated ${batch.rows.length} rows locally. Nothing uploaded; host evidence remains to be reviewed.`);
    return;
  }
  const url = new URL(Deno.env.get("SUPABASE_URL") ?? "");
  if (url.protocol !== "https:" || url.username || url.password || url.pathname !== "/" || url.search || url.hash) {
    throw new Error("Expected HTTPS Supabase origin");
  }
  const key = Deno.env.get("SUPABASE_ANON_KEY");
  const token = Deno.env.get("HOST_ACCESS_TOKEN");
  if (!key || !token) throw new Error("Missing anon key or signed-in host access token");
  const response = await fetch(new URL("/rest/v1/rpc/import_verified_attendance", url), {
    method: "POST", redirect: "error", signal: AbortSignal.timeout(30000),
    headers: { apikey: key, Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify({ p_event_id: batch.event_id, p_rows: batch.rows, p_evidence_ref: batch.evidence_ref }),
  });
  if (!response.ok) throw new Error(`Import not confirmed (HTTP ${response.status}); check identity, event and roster in staging`);
  const result = await response.json();
  if (!Number.isInteger(result.verified) || !Number.isInteger(result.unchanged)) throw new Error("Unexpected import response");
  console.log(`Verified: ${result.verified}; already recorded: ${result.unchanged}. No participant details logged.`);
}

if (import.meta.main) {
  try { await main(); }
  catch { console.error("Import not confirmed. Check batch format, access and connectivity; no roster or credentials logged."); Deno.exitCode = 1; }
}
