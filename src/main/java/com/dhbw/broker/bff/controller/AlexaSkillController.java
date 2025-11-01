package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.dto.AlexaRequest;
import com.dhbw.broker.bff.dto.AlexaResponse;
import com.dhbw.broker.bff.repository.AssetRepository;
import com.dhbw.broker.bff.service.GraphqlPriceService;
import com.dhbw.broker.bff.util.AlexaSignatureVerifier;
import com.dhbw.broker.bff.util.AssetNameNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@RestController
@RequestMapping("/integrations/alexa")
@RequiredArgsConstructor
public class AlexaSkillController {

    private static final Logger log = LoggerFactory.getLogger(AlexaSkillController.class);
    private static final String EXPECTED_SKILL_ID = "amzn1.ask.skill.b5a01dcf-b1e7-43d2-bcf3-8af3c3bbef06";
    private static final Set<String> DEMO_ORIGINS = Set.of(
            "http://localhost:4200",
            "https://localhost:4200",
            "https://rocky-atoll-88358-b10b362cee67.herokuapp.com"
    );

    private final AssetRepository assetRepository;
    private final GraphqlPriceService priceService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlexaResponse> handleAlexa(
            @RequestBody String rawJson,
            @RequestHeader(value = "Signature", required = false) String signature,
            @RequestHeader(value = "SignatureCertChainUrl", required = false) String certUrl,
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {

        boolean isDemoCall = isDemoOrigin(origin, referer);

        AlexaRequest request;
        try {
            request = objectMapper.readValue(rawJson, AlexaRequest.class);
        } catch (Exception e) {
            return ResponseEntity.ok(simplePlainText("Die Anfrage konnte nicht gelesen werden."));
        }

        boolean hasAlexaHeaders = signature != null && certUrl != null;

        if (hasAlexaHeaders && !isDemoCall) {
            String ts = request.getRequest() != null ? request.getRequest().getTimestamp() : null;
            if (!AlexaSignatureVerifier.isTimestampValid(ts)) {
                return ResponseEntity.ok(simplePlainText("Die Anfrage ist abgelaufen."));
            }

            byte[] rawBytes = rawJson.getBytes(StandardCharsets.UTF_8);
            boolean sigOk = AlexaSignatureVerifier.isSignatureValid(signature, certUrl, rawBytes);
            if (!sigOk) {
                return ResponseEntity.ok(simplePlainText("Die Anfrage konnte nicht verifiziert werden."));
            }

            String appId = null;
            if (request.getSession() != null && request.getSession().getApplication() != null) {
                appId = request.getSession().getApplication().getApplicationId();
            }
            if (!EXPECTED_SKILL_ID.equals(appId)) {
                return ResponseEntity.ok(simplePlainText("Diese Anfrage stammt nicht von dem erwarteten Skill."));
            }
        }

        if (request.getRequest() == null) {
            return ResponseEntity.ok(simplePlainText("Ich habe keine Anfrage erhalten."));
        }

        String type = request.getRequest().getType();
        if ("LaunchRequest".equals(type)) {
            return ResponseEntity.ok(simplePlainText("Willkommen beim Demo Broker. Du kannst sagen: Lies mir die aktuellen Marktpreise vor."));
        }

        if (!"IntentRequest".equals(type)) {
            return ResponseEntity.ok(simplePlainText("Diesen Anfrage-Typ unterstütze ich noch nicht."));
        }

        String intentName = request.getRequest().getIntent() != null
                ? request.getRequest().getIntent().getName()
                : null;

        if (intentName == null) {
            return ResponseEntity.ok(simplePlainText("Ich habe kein Intent bekommen."));
        }

        return switch (intentName) {
            case "ReadCurrentMarketPricesIntent" -> ResponseEntity.ok(buildAllPricesResponse());
            case "ReadSingleAssetIntent" -> ResponseEntity.ok(buildSingleAssetResponse(request));
            default -> ResponseEntity.ok(simplePlainText("Das Intent " + intentName + " kenne ich nicht."));
        };
    }

    private boolean isDemoOrigin(String origin, String referer) {
        if (origin != null && DEMO_ORIGINS.contains(origin)) {
            return true;
        }
        if (referer != null) {
            for (String allowed : DEMO_ORIGINS) {
                if (referer.startsWith(allowed)) {
                    return true;
                }
            }
        }
        return false;
    }

    private AlexaResponse buildAllPricesResponse() {
        var assets = assetRepository.findAllActive();
        if (assets.isEmpty()) {
            return simplePlainText("Es sind aktuell keine Assets im Markt verfügbar.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hier sind die aktuellen Marktpreise. ");

        for (int i = 0; i < assets.size(); i++) {
            var asset = assets.get(i);
            BigDecimal price = priceService.getCurrentPrice(asset.getAssetSymbol());
            if (price == null) {
                continue;
            }
            sb.append(asset.getName())
                    .append(" steht bei ")
                    .append(price)
                    .append(" US Dollar. ");
            if (i >= 7) {
                sb.append("Weitere Assets lasse ich aus, um es kurz zu halten.");
                break;
            }
        }

        return simplePlainText(sb.toString());
    }

    private AlexaResponse buildSingleAssetResponse(AlexaRequest request) {
        String rawAsset = null;
        if (request.getRequest().getIntent() != null && request.getRequest().getIntent().getSlots() != null) {
            var slots = request.getRequest().getIntent().getSlots();
            if (slots.containsKey("asset")) {
                rawAsset = slots.get("asset").getValue();
            } else if (slots.containsKey("Asset")) {
                rawAsset = slots.get("Asset").getValue();
            } else if (slots.containsKey("symbol")) {
                rawAsset = slots.get("symbol").getValue();
            }
        }

        if (rawAsset == null || rawAsset.isBlank()) {
            return simplePlainText("Welches Asset möchtest du hören? Sage zum Beispiel: Lies mir Bitcoin vor.");
        }

        String normalized = AssetNameNormalizer.normalize(rawAsset);

        if (normalized != null) {
            var bySymbol = assetRepository.findByAssetSymbolAndActive(normalized);
            if (bySymbol.isPresent()) {
                var asset = bySymbol.get();
                BigDecimal price = priceService.getCurrentPrice(asset.getAssetSymbol());
                return buildAssetPriceResponse(asset.getName(), price);
            }
        }

        final String rawAssetTrimmed = rawAsset.trim();

        var all = assetRepository.findAllActive();
        var match = all.stream()
                .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(rawAssetTrimmed))
                .findFirst();

        if (match.isPresent()) {
            var asset = match.get();
            BigDecimal price = priceService.getCurrentPrice(asset.getAssetSymbol());
            return buildAssetPriceResponse(asset.getName(), price);
        }

        return simplePlainText("Das Asset " + rawAsset + " konnte ich nicht finden. Sage zum Beispiel: Lies mir Bitcoin vor.");
    }

    private AlexaResponse buildAssetPriceResponse(String assetName, BigDecimal price) {
        if (price == null) {
            return simplePlainText("Für " + assetName + " konnte ich gerade keinen Preis holen.");
        }
        String text = assetName + " steht aktuell bei " + price + " US Dollar.";
        return simplePlainText(text);
    }

    private AlexaResponse simplePlainText(String text) {
        AlexaResponse.AlexaOutputSpeech outputSpeech = new AlexaResponse.AlexaOutputSpeech();
        outputSpeech.setType("PlainText");
        outputSpeech.setText(text);

        AlexaResponse.AlexaResponseBody body = new AlexaResponse.AlexaResponseBody();
        body.setOutputSpeech(outputSpeech);
        body.setShouldEndSession(Boolean.FALSE);

        AlexaResponse resp = new AlexaResponse();
        resp.setVersion("1.0");
        resp.setResponse(body);

        return resp;
    }
}
