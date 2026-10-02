export interface Preference {
  userId: string;
  languages: string[];
  interests: string[];
  professionalDomains: string[];
  matchingOptIn: boolean;
  metricsOptIn: boolean;
}
export interface Attendance {
  userId: string;
  eventId: string;
  startsAt: string;
  language: string | null;
  verified: boolean;
  analyticsAllowed: boolean;
}

/** Human-reviewed suggestions only: no directory, contact disclosure or automatic outreach. */
export function suggestConnections(me: Preference, peers: Preference[]) {
  if (!me.matchingOptIn) return [];
  const overlap = (a: string[], b: string[]) => [...new Set(a.filter((x) => b.includes(x)))].sort();
  return peers.filter((p) => p.userId !== me.userId && p.matchingOptIn).map((p) => ({
    userId: p.userId,
    languages: overlap(me.languages, p.languages),
    interests: overlap(me.interests, p.interests),
    professionalDomains: overlap(me.professionalDomains, p.professionalDomains),
  })).filter((p) => p.languages.length > 0 && (p.interests.length + p.professionalDomains.length > 0))
    .sort((a, b) => a.userId.localeCompare(b.userId));
}

/** Reference definition for the SQL seven-day metric; not an authorization boundary. */
export function consecutiveLanguageReturn(rows: Attendance[], preferences: Preference[], asOf: Date) {
  const now = asOf.getTime();
  if (!Number.isFinite(now)) throw new Error("invalid observation time");
  const consent = new Set(preferences.filter((p) => p.metricsOptIn).map((p) => p.userId));
  const unique = new Map<string, Attendance>();
  for (const row of rows) {
    const time = Date.parse(row.startsAt);
    if (!Number.isFinite(time)) throw new Error("invalid event timestamp");
    if (!row.verified || !row.analyticsAllowed || !consent.has(row.userId) || time >= now) continue;
    const key = JSON.stringify([row.userId, row.eventId]);
    const previous = unique.get(key);
    if (previous && JSON.stringify(previous) !== JSON.stringify(row)) throw new Error("conflicting attendance");
    unique.set(key, row);
  }
  const users = new Map<string, Attendance[]>();
  for (const row of unique.values()) users.set(row.userId, [...(users.get(row.userId) ?? []), row]);
  const result: Record<string, { eligible: number; returned: number }> = {};
  const week = 7 * 24 * 60 * 60 * 1000;
  for (const visits of users.values()) {
    visits.sort((a, b) => Date.parse(a.startsAt) - Date.parse(b.startsAt) || a.eventId.localeCompare(b.eventId));
    const [first, next] = visits;
    const time = Date.parse(first.startsAt);
    if (!first.language || time + week > now) continue;
    const bucket = result[first.language] ??= { eligible: 0, returned: 0 };
    bucket.eligible++;
    if (next && next.language === first.language && Date.parse(next.startsAt) > time && Date.parse(next.startsAt) <= time + week) bucket.returned++;
  }
  return result;
}
