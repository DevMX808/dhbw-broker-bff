package com.dhbw.broker.bff.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AlexaRequest {

    private AlexaSession session;
    private AlexaInnerRequest request;

    @Setter
    @Getter
    public static class AlexaSession {
        private String sessionId;
        private AlexaUser user;

    }

    @Setter
    @Getter
    public static class AlexaUser {
        private String userId;

    }

    @Setter
    @Getter
    public static class AlexaInnerRequest {
        private String type;
        private AlexaIntent intent;

    }

    @Setter
    @Getter
    public static class AlexaIntent {
        private String name;
        private Map<String, AlexaSlot> slots;

    }

    @Setter
    @Getter
    public static class AlexaSlot {
        private String name;
        private String value;

    }
}
