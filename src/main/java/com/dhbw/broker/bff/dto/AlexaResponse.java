package com.dhbw.broker.bff.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlexaResponse {

    private String version;
    private AlexaResponseBody response;

    @Getter
    @Setter
    public static class AlexaResponseBody {
        private AlexaOutputSpeech outputSpeech;
        private AlexaOutputSpeech reprompt;
        private Boolean shouldEndSession;
        private AlexaCard card;
    }

    @Getter
    @Setter
    public static class AlexaOutputSpeech {
        private String type;
        private String text;
        private String ssml;
    }

    @Getter
    @Setter
    public static class AlexaCard {
        private String type;
        private String title;
        private String content;
    }
}
