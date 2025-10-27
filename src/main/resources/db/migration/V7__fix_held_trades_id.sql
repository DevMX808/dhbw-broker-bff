DROP TABLE IF EXISTS broker.held_trades;
CREATE TABLE broker.held_trades (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id UUID NOT NULL,
                                    trade_id BIGINT NOT NULL UNIQUE,
                                    asset_symbol VARCHAR(255) NOT NULL,
                                    quantity NUMERIC NOT NULL,
                                    buy_price_usd NUMERIC NOT NULL,
                                    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);