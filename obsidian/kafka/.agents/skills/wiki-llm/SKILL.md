---
name: wiki-llm
description: A pattern for building personal knowledge bases using LLMs. Maintains a persistent, compounding wiki from raw sources — the LLM incrementally builds and maintains structured, interlinked markdown files instead of re-deriving knowledge on every query.
---

# LLM Wiki Skill

This skill turns the agent into a disciplined wiki maintainer for personal knowledge bases.
Instead of just retrieving from raw documents at query time, the agent incrementally builds and maintains a persistent wiki — a structured, interlinked collection of markdown files that sits between the user and the raw sources. The knowledge is compiled once and kept current, not re-derived on every query.

## Core Principles
1. **The Wiki is a Compounding Artifact:** The knowledge is compiled once and kept current. Cross-references, contradictions, and synthesis are already present in the wiki pages.
2. **Immutable Raw Sources:** Raw sources (articles, papers, images, data) are immutable. Read them but never modify them.
3. **Agent Owns the Wiki:** The agent maintains the wiki layer (generated markdown files). Create pages, update them, maintain cross-references, and ensure consistency.
4. **Atomicity Over Volume.** Prefer a dedicated page per concept over a "dumping ground." Each page should cover one entity, concept, or topic.
5. **Conservative by Default.** Better to note uncertainty or a conflict than to silently delete or rewrite knowledge. Better to propose a new page than to dilute the content of an existing one.
6. **Use-cases. Examples. 

## Layers
- **Raw sources**: Curated collection of source documents (e.g. in a `raw/` directory).
- **The wiki**: Directory of generated markdown files (summaries, entities, concepts, overview, comparisons, index).
- **The schema**: This skill file, defining operations and structures.

## Operations

### 1. Ingest
When the user asks you to process a new source (dropped into the raw collection):
- Read the source.
- Discuss key takeaways with the user (if they want to be involved) or process it autonomously.
- Write a summary page in the wiki.
- Extract key ideas, terms, entities, and potential connections.
- Create or update relevant pages. Atomicity matters: one page per concept.
- Update the `index.md` file.
- Update relevant entity and concept pages across the wiki.
- Append an entry to the `log.md` file.
- Always add cross-references between related pages.

### 2. Query
When the user asks a question against the wiki:
- Search for relevant pages (starting with `index.md`), read them, and synthesize an answer with citations.
- Good answers should be filed back into the wiki as new pages (e.g., a comparison, an analysis, a connection) so knowledge compounds.

### 3. Lint
When the user asks to health-check the wiki, look for:
- Contradictions between pages.
- Stale claims that newer sources superseded.
- Orphan pages with no inbound links.
- Important concepts lacking their own page.
- Missing cross-references.
- Data gaps that could be filled.
- Suggest new questions to investigate and new sources to look for.

## Indexing and Logging
- **`index.md`**: A catalog of everything in the wiki. Each page listed with a link, a one-line summary, and metadata. Organized by category. Update on every ingest. Read first during queries.
- **`log.md`**: An append-only chronological record of what happened and when (ingests, queries, lint passes). Use format: `## [YYYY-MM-DD] ingest | Article Title`.
