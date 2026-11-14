# AI_USAGE

This file serves as a record of AI usage in this project;
each entry corresponds to a specific use of a particular tool.

As with the changelog, the entries are ordered so that the oldest
appear at the end and the newest at the beginning of the document.

All content generated with AI assistance has been reviewed and adapted by the student, who remains responsible for the correctness and quality of the project deliverables.

## Template

* Date
* Phase
* Objective
* Tool
* Specific version
* Tool configuration (model, reasoning level, agentic mode, chat vs. plan, etc.)
* Usage details (examples): 
  * When an error occurred, the prompt was copied, and the AI was asked to identify possible reasons for the error.
  * The AI was provided with a general description of the problem and asked to generate a significant portion of the code.
* Tool add-ons: Plugins, skills, MCP servers, tools, etc.
* Context files regarding AI usage in the project (CLAUDE.md or similar)
* If tools relying on specific files for AI-driven development are used (e.g., Spec-Driven Development tools), indicate the location of these documents within the repository and how they were utilized.

## Claude: documentation of version 0.1

* Date: 2026/11/14
* Phase: 3
* Objective: Update the whole project documentation to the structure required for Phase 3, using the code of the repository as the only source of truth
* Tool: Claude (Anthropic)
* Specific version: Claude Sonnet 5.5 (configured model `claude-sonnet-5-5`; the model that serves a turn can differ from the configured one)
* Tool configuration: Agentic mode with access to a Linux workspace (file reading and editing, shell, web search), working on a read-only clone of the repository branch `51-alignment-with-project-evaluation-guidelines`. Delivered the result as a zip file that the student applied and committed.
* Usage details:
  * The AI was given the Word guide of the tutors and asked to rewrite the documentation following it, without adding anything that is not implemented.
  * The AI read the code (controllers, security configuration, domain model, persistence, Docker files, workflows, tests, frontend) and compared it with the existing documentation, correcting statements that did not match the code (technologies and versions that are not used, features that are not implemented).
  * It generated the new documents (features, detailed functionalities, execution, architecture with Mermaid diagrams, development guide, quality control, CI/CD and releases, REST API), the OpenAPI additions for the avatar endpoints, the HTML rendering of the specification and the Postman collection.
  * It ran the frontend coverage command to obtain the statistics included in the quality control document.
  * The student reviewed the result and provided the information that cannot be derived from the code (IDEs, AI usage, screenshots and video).
* Tool add-ons: None.
* Context files regarding AI usage in the project: None.
* AI-driven development files: The Word guide of the tutors was supplied as an attachment to the chat; it is not stored in the repository.

## Claude: refactoring of the frontend

* Date: 2026/11/05 (approximate: date of the commit "refactor frontend for better quality")
* Phase: 3
* Objective: Refactor the frontend to improve its quality and structure (components, hooks, API layer and model)
* Tool: Claude (Anthropic)
* Specific version: Claude Sonnet 5.5
* Tool configuration: Chat, reasoning level medium
* Usage details:
  * The AI refactored the frontend code and proposed improvements to its structure.
  * The student reviewed every change manually and verified it by running the automated tests.
* Tool add-ons: None.
* Context files regarding AI usage in the project: None.
* AI-driven development files: None.

## Qwen 3.6 (local model): TDD pair programming

* Date: Occasional trials during Phase 3 (until 2026/11/14)
* Phase: 3
* Objective: Try a locally executed model as the pair programming agent, with the same TDD workflow used with Junie and Copilot
* Tool: Qwen 3.6, executed locally and connected to JetBrains Air
* Specific version: Qwen 3.6, executed with llama.cpp from the terminal (size and quantization not recorded)
* Tool configuration: Local model connected to JetBrains Air as an additional agent
* Usage details:
  * Same red, green, refactor workflow as with the other agents.
  * It was used as a trial, to evaluate working without a cloud service.
* Tool add-ons: the same MCP server enabled in JetBrains Air.
* Context files regarding AI usage in the project: `.junie/guidelines/` (see the Junie entry)
* AI-driven development files: None.

## JetBrains Junie: TDD pair programming

* Date: Throughout Phase 3 (until 2026/11/14)
* Phase: 3
* Objective: Develop the backend (users, security, avatars, incidences) and its tests following Test-Driven Development
* Tool: JetBrains Junie, connected through JetBrains Air
* Specific version: Junie Lite (free plan). Junie and JetBrains Air were updated automatically with the IDE during the phase, so no fixed version number was recorded
* Tool configuration: Agent working as a pair programmer from JetBrains Air, which is integrated in the IDE (IntelliJ IDEA and WebStorm) and is the component that connects the agents (Junie, GitHub Copilot and Claude) to the project for better integration and communication
* Usage details:
  * Red, green, refactor cycle: the AI writes the failing test, the student implements the code that makes it pass, and both refactor the code together while keeping the tests green.
  * The commit history reflects the cycle: tests are committed (`test(...)`) before the code that satisfies them (`feat(...)`), followed by refactoring commits.
  * The student reviews and adapts every change before committing it.
* Tool add-ons: the MCP server that JetBrains installs automatically with the IDE (latest update), enabled in JetBrains Air during the whole phase
* Context files regarding AI usage in the project: `.junie/guidelines/` (see below)
* AI-driven development files: The guideline files in `.junie/guidelines/` are instructions for the agents. They were created during the phase and refined while trying them with the agents:
  * `TDD_PAIR_PROGRAMMING.md`: roles and steps of the red, green, refactor workflow.
  * `TESTING_CONVENTIONS.md`: package structure, naming and conventions of the tests.
  * `SONARQUBE_QUALITY.md`: quality and security rules that the generated code has to follow.
  * `JETBRAINS_AI_PRO_TIPS.md`: tips to work with the AI assistant in the IDE.

## GitHub Copilot: TDD pair programming

* Date: Throughout Phase 3 (until 2026/11/14)
* Phase: 3
* Objective: Develop the backend and its tests following Test-Driven Development, in the same way as with Junie
* Tool: GitHub Copilot, connected through JetBrains Air
* Specific version: GitHub Copilot, updated automatically (no fixed version recorded). The model was chosen automatically ("Auto") for each prompt
* Tool configuration: Model selection in automatic mode ("Auto"). Agent working as a pair programmer from JetBrains Air (IntelliJ IDEA and WebStorm)
* Usage details:
  * Red, green, refactor cycle: the AI writes the failing test, the student implements the code that makes it pass, and both refactor the code together.
  * The student reviews and adapts every change before committing it.
* Tool add-ons: the same MCP server enabled in JetBrains Air.
* Context files regarding AI usage in the project: `.junie/guidelines/` (see the Junie entry)
* AI-driven development files: None.

## GitHub Copilot: repository audit

* Date: 2026/10/06
* Phase: 2
* Objective: Validate the repository and clean up the documentation against the Phase 2 requirements
* Tool: GitHub Copilot
* Specific version: GitHub Copilot, model chosen automatically by the service (not recorded)
* Tool configuration: Chat from the GitHub website, for code analysis and repository review
* Usage details:
  * Audit of the repository against the Phase 2 specification.
  * Removal of documentation that did not match the implementation (references to Spring Security, the template README of the frontend).
  * Documentation of the real API endpoints of `IncidenceController`.
  * Validation of the CI/CD workflows, the test strategy and the build configuration, and a consistency check between the documentation and the code.
  * Organization of the repository structure and modularization of the README into the `docs` folder, and generation of CI and test guidance aligned with the project.
* Tool add-ons: None.
* Context files regarding AI usage in the project: None.
* AI-driven development files: None.

## OpenAI ChatGPT

* Date: 2026/09/07
* Phase: 1
* Objective: Documentation and analysis
* Tool: OpenAI ChatGPT
* Specific version: GPT-5.6 Luna
* Tool configuration: Chat
* Usage details:
  * It was used to review and refine the project's README, particularly the functional and technical objectives, the methodology description, and the analysis section.
  * It was used to improve the consistency and clarity of the English documentation.
  * It was used to review the domain analysis, including the distinction between entities, value objects, and domain/integration events following the project's DDD approach.
  * It was used to check the README against the requirements defined for Phase 1 and identify missing or potentially incomplete documentation.
  * The AI-generated suggestions were reviewed and adapted by the student before being incorporated into the project documentation.
* Tool add-ons: None.
* Context files regarding AI usage in the project: None.
* AI-driven development files: None.

## Google Gemini

* Date: 2026/08/16
* Phase: 1
* Objective: Research
* Tool: Google Gemini
* Specific version: Gemini 3.5 Flash
* Tool configuration: Chat (from Google research)
* Usage details: 
  * It was used directly from google browser to quickly research important topics such as similar apps to this one.
  * It was also used to check how to create a table in markdown.
* Tool add-ons: None.
* Context files regarding AI usage in the project: None.
* AI-driven development files: None.
