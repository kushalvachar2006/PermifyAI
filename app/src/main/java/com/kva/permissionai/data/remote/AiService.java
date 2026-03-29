package com.kva.permissionai.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.util.List;

public interface AiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    Call<GeminiResponse> generateContent(@Query("key") String apiKey, @Body GeminiRequest request);

    class GeminiRequest {
        public List<Content> contents;
        public GeminiRequest(List<Content> contents) { this.contents = contents; }
    }

    class Content {
        public List<Part> parts;
        public Content(List<Part> parts) { this.parts = parts; }
    }

    class Part {
        public String text;
        public Part(String text) { this.text = text; }
    }

    class GeminiResponse {
        public List<Candidate> candidates;
    }

    class Candidate {
        public Content content;
    }
}