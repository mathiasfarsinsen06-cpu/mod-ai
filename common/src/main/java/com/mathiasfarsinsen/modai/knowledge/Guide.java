package com.mathiasfarsinsen.modai.knowledge;

import java.util.List;

/**
 * A local, structured piece of strategic advice (e.g. "how to run an iron
 * farm safely" or "village growth tips") that villagers/AI logic can consult
 * when deciding what to build or prioritize next.
 */
public final class Guide {

    private String id;
    private String topic;
    private List<String> steps;

    public Guide() {
        // Default constructor for Gson deserialization.
    }

    public Guide(String id, String topic, List<String> steps) {
        this.id = id;
        this.topic = topic;
        this.steps = steps;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getSteps() {
        return steps == null ? List.of() : steps;
    }
}
