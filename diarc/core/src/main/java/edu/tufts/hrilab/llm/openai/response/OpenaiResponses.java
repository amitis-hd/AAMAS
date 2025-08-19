package edu.tufts.hrilab.llm.openai.response;

import java.util.List;
import java.util.Map;

public class OpenaiResponses {

    public String id;
    public String object;
    public long created_at;
    public String status;
    public Object error;
    public Object incomplete_details;
    public String instructions;
    public Long max_output_tokens;
    public String model;
    public List<Output> output;
    public boolean parallel_tool_calls;
    public String previous_response_id;
    public Reasoning reasoning;
    public boolean store;
    public Double temperature;
    public Text text;
    public String tool_choice;
    public List<Object> tools;
    public Double top_p;
    public String truncation;
    public Usage usage;
    public String user;
    public Map<String, Object> metadata;

    public static class Output {
        public String type;
        public String id;
        public String status;
        public String role;
        public List<Content> content;
    }

    public static class Content {
        public String type;
        public String text;
        public List<Object> annotations;
    }

    public static class Reasoning {
        public Object effort;
        public Object summary;
    }

    public static class Text {
        public Format format;

        public static class Format {
            public String type;
        }
    }

    public static class Usage {
        public int input_tokens;
        public InputTokensDetails input_tokens_details;
        public int output_tokens;
        public OutputTokensDetails output_tokens_details;
        public int total_tokens;

        public static class InputTokensDetails {
            public int cached_tokens;
        }

        public static class OutputTokensDetails {
            public int reasoning_tokens;
        }
    }

}
