package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.service.GraphqlPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceRingController {

  private final GraphqlPriceService priceService;

  /**
   * Holt alle Preispunkte der letzten 24h für Chart-Darstellung
   */
  @GetMapping("/24h/{assetSymbol}")
  public ResponseEntity<?> get24hPrices(@PathVariable String assetSymbol) {
    if (assetSymbol == null || assetSymbol.isBlank() || assetSymbol.length() > 10) {
      return ResponseEntity.badRequest().body("assetSymbol invalid");
    }
    
    var prices = priceService.get24hPrices(assetSymbol);
    return prices.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(prices);
  }
}