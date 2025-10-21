# Deep Research Agent

A multi-agent research system built with Kotlin and Koog Agents that conducts comprehensive research through intelligent agent coordination and web search.

## 🏗️ Architecture

Three-phase pipeline with specialized AI agents:

```
SCOPING → RESEARCH → REPORT GENERATION
```

**Agents:**
- **ClarificationAgent**: Interactive questioning to refine research scope
- **BriefGenerationAgent**: Converts user input into detailed research briefs  
- **ResearcherAgent**: Individual research with web search capabilities
- **SupervisorAgent**: Coordinates multiple researchers and parallel research
- **FinalReportAgent**: Synthesizes findings into comprehensive reports

**Key Features:**
- Parallel research execution (up to 3 concurrent tasks)
- Tavily web search integration with source attribution
- Structured report generation with citations
- Built-in evaluation framework

## 🧠 Agent Hierarchy & Planning

### Hierarchical Structure
The system uses a **supervisor-subagent architecture** for specialized research:

```
SupervisorAgent (Coordinator)
├── ResearcherAgent 1 (Context A)
├── ResearcherAgent 2 (Context B)  
└── ResearcherAgent 3 (Context C)
```

**Supervisor Agent:**
- Analyzes research brief and breaks it into specialized subtopics
- Decides between single vs. parallel research based on complexity
- Each subtopic gets independent context and instructions
- Aggregates findings from all sub-agents

**Researcher Agents:**
- Work independently with focused context
- Execute parallel research on different aspects
- Use ReAct pattern: **Reason** → **Act** → **Observe** → **Plan**
- Built-in reflection after each search to plan next steps

### ReAct Planning Pattern
Each researcher follows a **Reasoning-Acting** cycle:

1. **Reason**: Analyze current findings and identify gaps
2. **Act**: Execute targeted web searches with specific queries  
3. **Observe**: Process search results and extract key information
4. **Plan**: Reflect on progress and determine next actions

This creates deliberate, strategic research rather than random searching.

## 🚀 Installation

### Prerequisites
- Java 21+
- OpenAI API Key
- Tavily API Key

### Setup

1. **Clone and build**
```bash
git clone <repository-url>
cd deep-research-agent
./gradlew build
```

2. **Set environment variables**
```bash
export OPENAI_API_KEY="your-openai-api-key"
export TAVILY_API_KEY="your-tavily-api-key"
```

3. **Run**
```bash
./gradlew run
```

## ⚙️ Configuration

### Research Parameters
```kotlin
val researchWorkflow = ResearchWorkflow(
    apiKey = openaiKey,
    tavilyApiKey = tavilyKey,
    maxConcurrentResearchers = 2,  // Parallel research limit
    maxIterations = 20             // Maximum research iterations
)
```

### Search Settings
```kotlin
TavilySearchTool.Args(
    query = "research topic",
    searchDepth = "advanced",      // "basic" or "advanced"
    maxResults = 5,                // 1-10 results
    includeRawContent = true,      // Include full content
    topic = "general"              // "general" or "news"
)
```

## 🧪 Evaluation

Run the built-in evaluation:
```kotlin
val scopeEvaluator = ScopeEvaluationRunner(openaiKey)
val passed = scopeEvaluator.run()
```
