# Jikanle: Rights-Aware Music And Model Training

Status: policy proposal, 2026-10-02. This is an engineering and product control, not
legal advice. A qualified music-rights lawyer must review each public release and
partner agreement in the relevant countries.

Implementation clarification, 2026-10-03: worker/manifest controls below are required
behavior, not a deployed training system. Pending rights code needs security and
full-rights-chain review; it does not certify licensed inputs or production readiness.

## Position

Jikanle will only train a generative model on music, lyrics, performances, voices or
derived datasets when the training right is explicit, documented and traceable to the
specific asset and purpose. “Opt-in” means an affirmative agreement by the relevant
rights holder or contributor, not silence, public availability, an upload to the
internet, a cover license, or permission to analyse a file.

The product may use public-domain works and partner-licensed works as the first safe
catalog. A creator can contribute a cover or source recording under a separate agreement
that states whether Jikanle may analyse it, create adaptations, publish a video, train
models, train a voice model, and use it for commercial products. Each checkbox has its
own scope, territory, duration, attribution, compensation/reporting, withdrawal and
output terms.

Jikanle should be compatible with the principles behind [Fairly Trained](https://www.fairlytrained.org/),
but must not claim certification. Fairly Trained describes certification scope and
licensed training-data requirements; certification does not automatically prove that
every contributor opted in, set a particular compensation rate, or grants rights to
every model output. We will seek an audit only after our records, exclusions and
contracts are mature.

## The rights matrix

Treat these as different assets and permissions:

| Asset / activity | Needed decision | Safe default before approval |
|---|---|---|
| Musical composition | composer/publisher, lyrics and music | no public use |
| Existing master recording | label/producer/recording owner | do not download, process or redistribute |
| Lyrics and translation | text rights plus adaptation/translation permission | private review only; do not publish full lyrics |
| New cover recording | performers, producer and composition/adaptation permissions | private draft only |
| Video or launch edit | synchronization, visual assets, performers and music permissions | no public upload |
| Feature extraction / embeddings | permission to process input and define retention/training status | local/private only |
| Dataset inclusion | explicit `model_training` scope and manifest lineage | exclude from training |
| Voice or likeness | performer-specific recording, consent and synthetic-use terms | no cloning or imitation |
| Model output | output policy, attribution, revenue/reporting and similarity review | research artifact, not publication |

Composition and sound recording are separate works; a new performance does not erase
the composition or adaptation issue. Audiovisual use adds synchronization questions.
WIPO describes separate composition, master and synchronization/adaptation rights, and
Brazil's government guidance says a recorded, shared or internet-distributed cover
requires prior authorization/licensing. [WIPO music rights](https://www.wipo.int/export/sites/www/sme/en/documents/guides/customization/creative_expression_nig.pdf)
and [Brazil government guidance](https://www.gov.br/propriedade-intelectual/pt-br/publicacoes/cartilhas/ebookperguntaserespostas.pdf/%40%40display-file/file).

“Fair use” is a jurisdiction-specific exception, not a product-wide permission label.
It can differ from Colombian, Brazilian or Japanese rules and may not cover a public
video, a translated adaptation, a master recording, or model training. Do not write
“fair use” into a creator agreement as a substitute for a licensed scope.

## Consent and contract design

At contribution time show a plain-language rights panel with:

1. What the contributor owns or controls, including whether they speak for all co-writers,
   performers, publishers and the master owner.
2. The exact files and hashes covered by the grant.
3. Separate toggles for `private_analysis`, `translation_adaptation`, `cover_recording`,
   `sync_video`, `public_distribution`, `model_training`, `voice_model_training` and
   `voice_likeness`.
4. Countries/territories, start/end date, attribution, compensation/reporting and
   whether the grant is exclusive or non-exclusive.
5. Whether a model may learn from the material, whether the dataset can be shared,
   which model families are covered, and whether downstream model users may use outputs
   commercially.
6. Withdrawal mechanics, effective date, deletion/exclusion process and what cannot be
   technically undone after a model checkpoint is trained. No retroactive deletion claim
   should be implied; stop future training and document remediation with counsel.
7. Contact and identity verification appropriate to the rights holder. A WhatsApp message
   may be a lead, not a durable license record.

The UI should never bundle “publish this cover” and “train models with this recording”.
For minors, collect guardian consent through a separate reviewed flow. Keep raw contracts
and identity documents in restricted storage, not in Postgres or Git. Store only the
minimal reference, checksum and decision needed for the product audit.

## Dataset and model controls

Each training run must have a manifest containing:

```text
dataset_manifest_id
asset_id + sha256 for every input or derived feature
rights_grant_id and permitted purpose
territory / effective dates / withdrawal snapshot
preprocessing and separation tools with versions
model name, commit, base checkpoint and configuration
excluded/revoked assets and reason
operator, approval, timestamp and artifact hashes
```

The training worker refuses a manifest unless every asset has an active approved grant
containing `model_training`. A cover grant without that scope fails closed. Public-domain
and permissive open-license inputs still need license and attribution records; “open” is
not the same as “unrestricted for every model output”. Provider models are not automatically
safe: record the provider's model/data terms and do not send opt-out assets to external
inference or logging services.

Embeddings are derived data, not a rights eraser. Keep them linked to source asset and
purpose. If a creator withdraws training permission, exclude the source and its derived
training features from future runs and open a remediation task for already-built models.

## Jikanle rollout

### Stage 0: private research

Use synthetic or public-domain material for pipeline development. Commercial Fukahi
remains private until composition, master, translation/adaptation, synchronization and
performer permissions are reviewed. The current Android importer is text-only and debug
only; it does not grant rights or upload data.

### Stage 1: opt-in partner catalog

Recruit independent artists or rights holders who can sign a narrow non-exclusive grant.
Start with analysis, a private demo and an agreed cover. Add public video and model
training as separate amendments. Return usage reports and attribution; negotiate revenue
share rather than promising an automatic percentage before a contract exists.

### Stage 2: auditable training program

Build the rights ledger, manifest validator, exclusion list, model card and creator
dashboard. Run a small benchmark with held-out opted-in assets. Publish only after legal,
music, language and model-quality review agree on scope.

### Stage 3: hosted catalog

Host only authorized masters/covers in access-controlled storage. Keep playback,
processing, training and public distribution as separate policy checks. A future
mini-datacenter in Colombia/Latin America is an infrastructure decision; it does not
replace licenses, creator consent, backups/egress controls or takedown procedures.

## Fukahi decision

The proposed JA-PT demo can show a private comparison using material the team is allowed
to possess and review. It must not be described as a licensed public cover, a campaign
endorsement or a model-training sample. For a public launch, use an authorized partner
work or public-domain composition until the relevant Fukahi rights holders approve the
exact cover, video, territories and duration.

## Acceptance tests

- A grant for analysis but not training blocks dataset export.
- A grant for cover recording but not sync blocks video publication.
- A revoked or expired grant blocks new processing and training manifests.
- A master-only grant cannot authorize lyric translation or composition adaptation.
- A voice-model grant names the performer and permitted model/output scope.
- Every published asset links to a rights decision, source hash, attribution and expiry.
- A user cannot grant permissions for another contributor without verified authority.
- Provider calls receive only assets whose purpose and provider transfer terms allow it.
