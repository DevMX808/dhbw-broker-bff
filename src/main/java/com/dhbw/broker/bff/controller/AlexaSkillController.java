package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.dto.AlexaRequest;
import com.dhbw.broker.bff.dto.AlexaResponse;
import com.dhbw.broker.bff.repository.AssetRepository;
import com.dhbw.broker.bff.service.GraphqlPriceService;
import com.dhbw.broker.bff.util.AssetNameNormalizer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/integrations/alexa")
@RequiredArgsConstructor
public class AlexaSkillController {

    private static final Logger log = LoggerFactory.getLogger(AlexaSkillController.class);

    private final AssetRepository assetRepository;
    private final GraphqlPriceService priceService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlexaResponse> handleAlexa(@RequestBody AlexaRequest request) {
        if (request == null || request.getRequest() == null) {
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
        log.info("Alexa single asset request: raw='{}' -> normalized='{}'", rawAsset, normalized);

        if (normalized != null) {
            var bySymbol = assetRepository.findByAssetSymbolAndActive(normalized);
            if (bySymbol.isPresent()) {
                var asset = bySymbol.get();
                BigDecimal price = priceService.getCurrentPrice(asset.getAssetSymbol());
                if (price == null) {
                    return simplePlainText("Für " + asset.getName() + " konnte ich gerade keinen Preis holen.");
                }
                String text = asset.getName() + " steht aktuell bei " + price + " US Dollar.";
                return simplePlainText(text);
            }
        }

        // damit das Lambda nicht über eine nicht-finale Variable meckert
        final String rawAssetTrimmed = rawAsset.trim();

        var all = assetRepository.findAllActive();
        var match = all.stream()
                .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(rawAssetTrimmed))
                .findFirst();

        if (match.isPresent()) {
            var asset = match.get();
            BigDecimal price = priceService.getCurrentPrice(asset.getAssetSymbol());
            if (price == null) {
                return simplePlainText("Für " + asset.getName() + " konnte ich gerade keinen Preis holen.");
            }
            String text = asset.getName() + " steht aktuell bei " + price + " US Dollar.";
            return simplePlainText(text);
        }

        return simplePlainText("Das Asset " + rawAsset + " konnte ich nicht finden. Sage zum Beispiel: Lies mir Bitcoin vor.");
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
