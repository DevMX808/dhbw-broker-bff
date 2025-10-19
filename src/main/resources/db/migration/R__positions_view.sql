CREATE OR REPLACE VIEW positions AS
SELECT
    pl.user_id,
    pl.asset_symbol,
    SUM(pl.remaining_quantity)                                       AS quantity,
    SUM(pl.remaining_quantity * pl.buy_price_usd)                    AS cost_basis,
    CASE WHEN SUM(pl.remaining_quantity) > 0
             THEN SUM(pl.remaining_quantity * pl.buy_price_usd)
            / SUM(pl.remaining_quantity)
         ELSE NULL END                                               AS avg_buy_price,
    alp.price_usd                                                    AS last_price_usd,
    SUM(pl.remaining_quantity) * COALESCE(alp.price_usd, 0)          AS market_value,
    (SUM(pl.remaining_quantity) * COALESCE(alp.price_usd, 0))
        - SUM(pl.remaining_quantity * pl.buy_price_usd)                AS pl_abs,
    CASE WHEN SUM(pl.remaining_quantity * pl.buy_price_usd) > 0
             THEN ((SUM(pl.remaining_quantity) * COALESCE(alp.price_usd, 0))
            - SUM(pl.remaining_quantity * pl.buy_price_usd))
            / SUM(pl.remaining_quantity * pl.buy_price_usd)
         ELSE NULL END                                               AS pl_pct,
    alp.updated_at                                                   AS last_price_ts
FROM portfolio_lots pl
         LEFT JOIN asset_last_price alp USING (asset_symbol)
GROUP BY pl.user_id, pl.asset_symbol, alp.price_usd, alp.updated_at;