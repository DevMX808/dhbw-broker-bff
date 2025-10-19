ALTER TABLE broker.asset_last_price  ADD CONSTRAINT ck_asset_last_price_pos  CHECK (price_usd > 0);
ALTER TABLE broker.asset_prices_ring ADD CONSTRAINT ck_asset_prices_ring_pos CHECK (price_usd > 0);

CREATE INDEX IF NOT EXISTS ix_lots_user_asset_open
    ON broker.portfolio_lots (user_id, asset_symbol)
    WHERE remaining_quantity > 0;

