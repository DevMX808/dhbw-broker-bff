package com.dhbw.broker.bff.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlexaResponse {

    private String version;
    private AlexaResponseBody response;

    @Setter
    @Getter
    public static class AlexaResponseBody {
        private AlexaOutputSpeech outputSpeech;
        private Boolean shouldEndSession;

    }

    @Setter
    @Getter
    public static class AlexaOutputSpeech {
        private String type;
        private String text;
        private String ssml;

    }
}
