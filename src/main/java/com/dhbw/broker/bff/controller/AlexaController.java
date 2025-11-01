package com.dhbw.broker.bff.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dhbw.broker.bff.dto.SpeakableAssetPriceDto;
import com.dhbw.broker.bff.dto.AlexaRequest;
import com.dhbw.broker.bff.dto.AlexaResponse;
import com.dhbw.broker.bff.dto.AlexaResponse.AlexaOutputSpeech;
import com.dhbw.broker.bff.dto.AlexaResponse.AlexaResponseBody;
import com.dhbw.broker.bff.service.GraphqlPriceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/integrations/alexa")
@RequiredArgsConstructor
public class AlexaController {

    private final GraphqlPriceService priceService;

    @PostMapping
    public ResponseEntity<AlexaResponse> handleAlexa(@RequestBody AlexaRequest request) {
        if (request == null || request.getRequest() == null) {
            return ResponseEntity.ok(buildPlainResponse("Ich habe keine gültige Anfrage erhalten."));
        }

        String requestType = request.getRequest().getType();

        if ("LaunchRequest".equalsIgnoreCase(requestType)) {
            return ResponseEntity.ok(buildPlainResponse(
                    "Willkommen beim Demo Broker. Du kannst sagen: lies mir die aktuellen Marktpreise vor."
            ));
        }

        if ("IntentRequest".equalsIgnoreCase(requestType)) {
            String intentName = request.getRequest().getIntent() != null
                    ? request.getRequest().getIntent().getName()
                    : null;

            if (intentName == null) {
                return ResponseEntity.ok(buildPlainResponse("Ich habe den gewünschten Befehl nicht verstanden."));
            }

            switch (intentName) {
                case "ReadCurrentMarketPricesIntent":
                    return ResponseEntity.ok(handleReadAllPrices());
                case "ReadSingleAssetIntent":
                    return ResponseEntity.ok(handleReadSingleAsset(request));
                case "AMAZON.HelpIntent":
                    return ResponseEntity.ok(buildPlainResponse(
                            "Du kannst sagen: lies mir die aktuellen Marktpreise vor. Oder: wie steht Gold."
                    ));
                default:
                    return ResponseEntity.ok(buildPlainResponse(
                            "Diesen Befehl kenne ich noch nicht."
                    ));
            }
        }

        return ResponseEntity.ok(buildPlainResponse("Diese Anfrage kann ich derzeit nicht verarbeiten."));
    }

    private AlexaResponse handleReadAllPrices() {
        List<SpeakableAssetPriceDto> prices = priceService.getCurrentPricesForActiveAssets();

        if (prices == null || prices.isEmpty()) {
            return buildPlainResponse("Zurzeit liegen mir keine aktuellen Marktpreise vor.");
        }

        StringBuilder ssml = new StringBuilder();
        ssml.append("<speak>");
        ssml.append("Hier sind die aktuellen Marktpreise. ");
        for (SpeakableAssetPriceDto p : prices) {
            ssml.append(buildPriceLine(p));
        }
        ssml.append("</speak>");

        return buildSsmlResponse(ssml.toString());
    }

    private AlexaResponse handleReadSingleAsset(AlexaRequest request) {
        String assetNameOrSymbol = extractAssetFromSlots(request);

        if (assetNameOrSymbol == null || assetNameOrSymbol.isBlank()) {
            return buildPlainResponse("Welches Asset meinst du? Zum Beispiel Gold, Bitcoin oder Ethereum.");
        }

        SpeakableAssetPriceDto dto = priceService.getCurrentPriceForSymbol(assetNameOrSymbol.toUpperCase(Locale.ROOT));

        if (dto == null) {
            List<SpeakableAssetPriceDto> all = priceService.getCurrentPricesForActiveAssets();
            dto = all.stream()
                    .filter(a -> equalsIgnoreCase(a.getName(), assetNameOrSymbol)
                            || equalsIgnoreCase(a.getSymbol(), assetNameOrSymbol))
                    .findFirst()
                    .orElse(null);
        }

        if (dto == null) {
            return buildPlainResponse("Dieses Asset finde ich gerade nicht in deinem Markt.");
        }

        String ssml = "<speak>" + buildPriceLine(dto) + "</speak>";
        return buildSsmlResponse(ssml);
    }

    private String buildPriceLine(SpeakableAssetPriceDto p) {
        if (p == null || p.getPriceUsd() == null) {
            return "";
        }

        BigDecimal price = p.getPriceUsd();
        String priceSpoken = price.stripTrailingZeros().toPlainString().replace(".", " Komma ");

        String name = p.getName() != null ? p.getName() : p.getSymbol();

        return name + ", " + priceSpoken + " Dollar. <break time=\"400ms\" />";
    }

    private String extractAssetFromSlots(AlexaRequest request) {
        if (request == null
                || request.getRequest() == null
                || request.getRequest().getIntent() == null
                || request.getRequest().getIntent().getSlots() == null) {
            return null;
        }

        String[] possibleNames = { "asset", "assetSymbol", "symbol", "marketAsset" };

        for (String key : possibleNames) {
            var slot = request.getRequest().getIntent().getSlots().get(key);
            if (slot != null && slot.getValue() != null && !slot.getValue().isBlank()) {
                return slot.getValue();
            }
        }
        return null;
    }

    private AlexaResponse buildPlainResponse(String text) {
        AlexaOutputSpeech outputSpeech = new AlexaOutputSpeech();
        outputSpeech.setType("PlainText");
        outputSpeech.setText(text);

        AlexaResponseBody responseBody = new AlexaResponseBody();
        responseBody.setOutputSpeech(outputSpeech);
        responseBody.setShouldEndSession(true);

        AlexaResponse response = new AlexaResponse();
        response.setVersion("1.0");
        response.setResponse(responseBody);
        return response;
    }

    private AlexaResponse buildSsmlResponse(String ssml) {
        AlexaOutputSpeech outputSpeech = new AlexaOutputSpeech();
        outputSpeech.setType("SSML");
        outputSpeech.setSsml(ssml);

        AlexaResponseBody responseBody = new AlexaResponseBody();
        responseBody.setOutputSpeech(outputSpeech);
        responseBody.setShouldEndSession(true);

        AlexaResponse response = new AlexaResponse();
        response.setVersion("1.0");
        response.setResponse(responseBody);
        return response;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }
}
