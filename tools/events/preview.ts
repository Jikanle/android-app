import { discoverJsonLd, type EventSource, nextReview } from "./discovery.ts";

// Operator-supplied JSON-LD only. No network permissions, attendees or auto-publishing.
if (Deno.args.length !== 3) throw new Error("usage: preview.ts SOURCE_ID INPUT.json OUTPUT.json");
const [id, input, output] = Deno.args;
if (input === output) throw new Error("output must not replace the input snapshot");
const sources: EventSource[] = JSON.parse(await Deno.readTextFile(new URL("./sources.json", import.meta.url)));
const source = sources.find((item) => item.id === id);
if (!source) throw new Error("unknown source");
const result = discoverJsonLd(JSON.parse(await Deno.readTextFile(input)), source);
const now = new Date();
await Deno.writeTextFile(output, JSON.stringify({
  ...result, sourceId: id, observedAt: now.toISOString(),
  nextReviewAt: nextReview(now, source.cadence).toISOString(), publication: "blocked_pending_review",
}, null, 2));
console.log(`${result.candidates.length} candidate(s), ${result.errors.length} error(s); no events published`);
