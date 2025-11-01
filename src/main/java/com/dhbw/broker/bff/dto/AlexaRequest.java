package com.dhbw.broker.bff.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AlexaRequest {

    private String version;

    private AlexaSession session;
    private AlexaInnerRequest request;
    private AlexaContext context;

    @Getter
    @Setter
    public static class AlexaSession {
        private String sessionId;
        private AlexaApplication application;
        private AlexaUser user;
        private Boolean newSession;
    }

    @Getter
    @Setter
    public static class AlexaApplication {
        private String applicationId;
    }

    @Getter
    @Setter
    public static class AlexaUser {
        private String userId;
    }

    @Getter
    @Setter
    public static class AlexaInnerRequest {
        private String type;
        private String requestId;
        private String locale;
        private AlexaIntent intent;
    }

    @Getter
    @Setter
    public static class AlexaIntent {
        private String name;
        private Map<String, AlexaSlot> slots;
    }

    @Getter
    @Setter
    public static class AlexaSlot {
        private String name;
        private String value;
    }

    @Getter
    @Setter
    public static class AlexaContext {
        private AlexaSystem system;
    }

    @Getter
    @Setter
    public static class AlexaSystem {
        private AlexaApplication application;
        private AlexaUser user;
        private AlexaDevice device;
    }

    @Getter
    @Setter
    public static class AlexaDevice {
        private String deviceId;
    }
}
