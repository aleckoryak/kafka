---
name: wiki-llm2
description: A pattern for building personal knowledge bases using LLMs. Maintains a persistent, compounding wiki from raw sources — the LLM incrementally builds and maintains structured, interlinked markdown files instead of re-deriving knowledge on every query.
---

# LLM Wiki Skill

This skill turns the agent into a disciplined wiki maintainer for personal knowledge bases.

Instead of just retrieving from raw documents at query time (RAG-style), the agent incrementally builds and maintains a persistent wiki — a structured, interlinked collection of markdown files that sits between the user and the raw sources. The knowledge is compiled once and kept current, not re-derived on every query.

The user chooses sources and sets direction. The agent maintains structure, links, freshness, and flags conflicts.

## Core Principles

1. **The Wiki is a Compounding Artifact.** Cross-references are already there. Contradictions have already been flagged. The synthesis already reflects everything read. The wiki gets richer with every source added and every question asked.
2. **Immutable Raw Sources.** Raw sources (articles, papers, images, data files) in `raw/` are immutable — the agent reads from them but never modifies them. This is the source of truth.
3. **Agent Owns the Wiki.** The agent maintains the `wiki/` layer entirely. It creates pages, updates them when new sources arrive, maintains cross-references, and keeps everything consistent. The user reads it; the agent writes it.
4. **Atomicity Over Volume.** Prefer a dedicated page per concept over a "dumping ground." Each page should cover one entity, concept, or topic.
5. **Conservative by Default.** Better to note uncertainty or a conflict than to silently delete or rewrite knowledge. Better to propose a new page than to dilute the content of an existing one.

## Architecture (Three Layers)

- **Raw sources** (`raw/`): Curated collection of source documents — articles, papers, web-clippings, PDFs, transcripts. Immutable; the agent reads but never modifies.
- **The wiki** (`wiki/`): Directory of agent-generated markdown files — summaries, entity pages, concept pages, comparisons, an index, a synthesis. The agent owns this layer entirely.
- **The schema** (`AGENTS.md` and this skill file): Defines how the wiki is structured, what conventions to follow, and what workflows to use. The user and agent co-evolve this over time.

## Operations

### 1. Ingest

When the user asks to process a new source (dropped into the raw collection):

1. Read the file from `raw/`.
2. Write a brief summary in your own words.
3. Extract key ideas, terms, entities, and potential connections.
4. Create or update relevant pages in `wiki/`. Atomicity matters: one page per concept.
5. Place `[[wikilinks]]` between related pages.
6. Note the path to the raw file and the source URL if available.
7. Update `wiki/Source Notes.md` (summary + key terms).
8. Update `wiki/index.md` if new or substantially changed pages should be easily discoverable.
9. Append an ingest entry to `wiki/log.md` with format: `## [YYYY-MM-DD] ingest | Source Title`.

A single source might touch 10–15 wiki pages. The user may prefer to ingest sources one at a time (staying involved, reading summaries, guiding emphasis) or batch-ingest many at once with less supervision.

### 2. Query

When the user asks a question against the wiki:

1. Check `wiki/` first, starting with `wiki/index.md` and topic pages.
2. Access `raw/` only if `wiki/` has no answer or verification is needed.
3. Synthesize an answer with citations to specific wiki pages and raw sources.
4. If the answer produces a new durable insight — propose saving it as a wiki page (**crystallization**). Chat is ephemeral; `wiki/` compounds.
5. If the answer relies on specific sources — provide citations.

### 3. Lint

Periodically (or after major changes) run a sanity-check on `wiki/`:

- Broken `[[wikilinks]]`.
- Broken markdown links to files in `raw/`.
- Duplicate pages.
- Orphan pages with no inbound links.
- Missing source references.
- Stale or inconsistent claims.
- Important concepts mentioned but lacking their own page.
- Data gaps that could be filled.

Briefly summarize lint results to the user; do not silently "fix" if the change is substantive. Suggest new questions to investigate and new sources to look for.

### 4. Raw Changes Reconciliation

When files are added, removed, or changed in `raw/`:

1. Compare current `raw/` contents with already-ingested sources from `wiki/Source Notes.md`, `wiki/index.md`, and `wiki/log.md`. Classify as added, removed, or existing-with-changes.
2. **Added sources** → follow Ingest workflow.
3. **Removed sources** → do not auto-delete wiki pages. Mark the source as absent, lower confidence on dependent claims, flag for user review.
4. **Changed sources** → update summaries and related wiki pages carefully, without silently overwriting contradictory claims.
5. Add reconciliation entries to `wiki/log.md`.

## Indexing and Logging

- **`wiki/index.md`**: Content-oriented catalog. Each page listed with a link, a one-line summary, and optionally metadata (date, source count). Organized by category (entities, concepts, sources, etc.). Updated on every ingest. Read first during queries.
- **`wiki/log.md`**: Chronological, append-only record. Format: `## [YYYY-MM-DD] action | Title`. Covers ingests, queries, lint passes. Parseable with simple tools.
- **`wiki/Source Notes.md`**: Registry of all ingested sources from `raw/` with brief summaries and key terms.

## Lifecycle and Conflicts

- **Confidence.** For non-trivial claims, optionally mark: `high`, `medium`, `low` — based on source count and freshness.
- **Freshness.** Note `last_verified` when needed. Rapidly-changing topics have a shorter staleness threshold.
- **Supersession.** If a new source updates an old claim, do not silently delete the old one. Mark as superseded and link to the new source.
- **Forgetting.** Stale or long-unused observations can be deprioritized or archived, but never discarded without a trace.
- **Conflicts.** If a new source contradicts an existing page — do not overwrite. Add a conflict note with both sources and indicate which is newer or more authoritative.
- **Typed relationships** (optional, as base grows): `supports`, `contradicts`, `extends`, `implements`, `used_by`, `supersedes`.

## Crystallization

If answering a question produces a new durable insight that could be useful again — propose saving it as a separate wiki page or section of an existing one. Valuable analyses, comparisons, and connections should not disappear into chat history. This way explorations compound in the knowledge base just like ingested sources do.

## Style and Format

- Write concisely and structurally, with subheadings where they aid navigation.
- Use Obsidian `[[wikilinks]]` between related wiki pages.
- Reference raw sources via markdown links to the path in `raw/` and original URLs when available.
- Cite sources for non-trivial claims.
- If confidence is low or a source is contradictory — mark it explicitly.
