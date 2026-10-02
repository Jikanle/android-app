import { parseStudy, studyPreview } from "./study.ts";
if (Deno.args.length !== 2) throw new Error("usage: run-study.ts INPUT.json ANDROID_PREVIEW.json");
const [input, output] = Deno.args;
if (input === output) throw new Error("output must differ from input");
const raw = await Deno.readTextFile(input);
if (new TextEncoder().encode(raw).length > 131072) throw new Error("study exceeds 128 KiB");
const study = parseStudy(JSON.parse(raw));
await Deno.writeTextFile(output, JSON.stringify(studyPreview(study), null, 2));
console.log("Private text preview exported. Music, pedagogy, publication and training remain unapproved.");
