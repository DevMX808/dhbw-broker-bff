INSERT INTO assets (asset_symbol, name, min_trade_increment, is_active)
VALUES
  ('XAG','Silver',   0.01, true),
  ('XAU','Gold',     0.01, true),
  ('BTC','Bitcoin',  0.01, true),
  ('ETH','Ethereum', 0.01, true),
  ('XPD','Palladium',0.01, true),
  ('HG', 'Copper',   0.01, true)
ON CONFLICT (asset_symbol) DO UPDATE
SET name = EXCLUDED.name,
    min_trade_increment = EXCLUDED.min_trade_increment,
    is_active = EXCLUDED.is_active;