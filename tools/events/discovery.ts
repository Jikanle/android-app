/** Offline adapter boundary. Fetching, platform permissions and publication are separate. */
export interface EventSource {
  id: string;
  url: string;
  cadence: "weekly" | "monthly";
  adapter: "jsonld" | "manual";
  automationApproved: boolean;
}

export interface EventCandidate {
  sourceId: string;
  externalId: string;
  sourceUrl: string;
  title: string;
  startsAt: string;
  endsAt: string | null;
  venue: string;
  language: null;
  sourceStatus: "scheduled" | "cancelled" | "postponed";
  reviewStatus: "pending";
}

export function nextReview(previous: Date, cadence: EventSource["cadence"]): Date {
  if (!Number.isFinite(previous.getTime())) throw new Error("invalid review timestamp");
  const next = new Date(previous);
  if (cadence === "weekly") next.setUTCDate(next.getUTCDate() + 7);
  else {
    const day = next.getUTCDate();
    next.setUTCDate(1);
    next.setUTCMonth(next.getUTCMonth() + 1);
    const last = new Date(Date.UTC(next.getUTCFullYear(), next.getUTCMonth() + 1, 0)).getUTCDate();
    next.setUTCDate(Math.min(day, last));
  }
  return next;
}

function object(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== "object" || Array.isArray(value)) throw new Error("expected object");
  return value as Record<string, unknown>;
}
function text(value: unknown, max = 300): string {
  if (typeof value !== "string" || !value.trim() || value.length > max) throw new Error("invalid text field");
  return value.trim();
}
function timestamp(value: unknown): string {
  const raw = text(value, 40);
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?(Z|[+-]\d{2}:\d{2})$/.test(raw)) {
    throw new Error("date needs time and explicit UTC offset; send ambiguous dates to manual review");
  }
  const date = new Date(raw);
  const [year, month, day] = raw.slice(0, 10).split("-").map(Number);
  const last = new Date(Date.UTC(year, month, 0)).getUTCDate();
  if (!Number.isFinite(date.getTime()) || month < 1 || month > 12 || day < 1 || day > last) throw new Error("invalid date");
  return date.toISOString();
}

export function normalizeEvent(value: unknown, source: EventSource): EventCandidate {
  const row = object(value);
  const types = Array.isArray(row["@type"]) ? row["@type"] : [row["@type"]];
  if (!types.includes("Event")) throw new Error("not an Event");
  const url = new URL(text(row.url, 1500), source.url);
  const origin = new URL(source.url);
  if (url.protocol !== "https:" || url.origin !== origin.origin || url.username || url.password) {
    throw new Error("event link must belong to configured HTTPS source");
  }
  url.hash = "";
  for (const key of [...url.searchParams.keys()]) {
    if (key.startsWith("utm_") || key === "fbclid") url.searchParams.delete(key);
  }
  url.searchParams.sort();
  const start = timestamp(row.startDate);
  const end = row.endDate == null ? null : timestamp(row.endDate);
  if (end && end <= start) throw new Error("end must follow start");
  const status = row.eventStatus == null ? "EventScheduled" : text(row.eventStatus).split("/").pop();
  const statuses = { EventScheduled: "scheduled", EventCancelled: "cancelled", EventPostponed: "postponed" } as const;
  if (!status || !(status in statuses)) throw new Error("unsupported event status needs review");
  return {
    sourceId: source.id,
    externalId: row["@id"] == null ? url.href : text(row["@id"], 1500),
    sourceUrl: url.href,
    title: text(row.name), startsAt: start, endsAt: end,
    venue: text(object(row.location).name),
    // Venue or country does not prove the language practiced at an event.
    language: null, sourceStatus: statuses[status as keyof typeof statuses], reviewStatus: "pending",
  };
}

export function discoverJsonLd(input: unknown, source: EventSource) {
  const candidates: EventCandidate[] = [];
  const errors: string[] = [];
  const visit = (node: unknown) => {
    if (Array.isArray(node)) { node.forEach(visit); return; }
    if (!node || typeof node !== "object") return;
    const row = node as Record<string, unknown>;
    if (row["@graph"]) visit(row["@graph"]);
    const types = Array.isArray(row["@type"]) ? row["@type"] : [row["@type"]];
    if (!types.includes("Event")) return;
    try { candidates.push(normalizeEvent(row, source)); }
    catch (error) { errors.push(error instanceof Error ? error.message : "invalid event"); }
  };
  visit(input);
  const unique = new Map<string, EventCandidate>();
  const conflicts = new Set<string>();
  for (const candidate of candidates) {
    const key = JSON.stringify([candidate.sourceId, candidate.externalId]);
    if (conflicts.has(key)) continue;
    const existing = unique.get(key);
    if (existing && JSON.stringify(existing) !== JSON.stringify(candidate)) {
      // Conflicting snapshots are not resolved by whichever row happened to arrive last.
      errors.push(`conflicting candidate identity: ${candidate.externalId}`);
      unique.delete(key);
      conflicts.add(key);
    } else if (!existing) unique.set(key, candidate);
  }
  return { candidates: [...unique.values()], errors };
}
