package edu.tufts.hrilab.llm.openai.request;

public class OpenaiResponsesRequestBody {
  public String model;
  public String input;
  public String previous_response_id;

  public OpenaiResponsesRequestBody(String model, String input, String responseId) {
    this.model = model;
    this.input = input;
    this.previous_response_id = responseId;
  }
}