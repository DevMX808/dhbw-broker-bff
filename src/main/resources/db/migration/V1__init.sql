CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA broker;

CREATE TABLE assets (
                        asset_symbol         varchar(10) PRIMARY KEY,
                        name                 varchar(100)        NOT NULL,
                        min_trade_increment  numeric(18,2)       NOT NULL DEFAULT 0.01 CHECK (min_trade_increment >= 0.01),
                        is_active            boolean             NOT NULL DEFAULT true,
                        created_at           timestamptz         NOT NULL DEFAULT now()
);

CREATE TABLE asset_last_price (
                                  asset_symbol   varchar(10) PRIMARY KEY REFERENCES assets(asset_symbol) ON DELETE RESTRICT ON UPDATE CASCADE,
                                  price_usd      numeric(20,8)     NOT NULL,
                                  source_ts_utc  timestamptz       NOT NULL,
                                  is_carry       boolean           NOT NULL DEFAULT false,
                                  updated_at     timestamptz       NOT NULL DEFAULT now()
);

CREATE TABLE asset_prices_ring (
                                   asset_symbol    varchar(10)      NOT NULL REFERENCES assets(asset_symbol) ON DELETE RESTRICT ON UPDATE CASCADE,
                                   slot            integer          NOT NULL CHECK (slot BETWEEN 0 AND 1439),
                                   price_usd       numeric(20,8)    NOT NULL,
                                   source_ts_utc   timestamptz      NOT NULL,
                                   ingested_ts_utc timestamptz      NOT NULL DEFAULT now(),
                                   is_carry        boolean          NOT NULL DEFAULT false,
                                   CONSTRAINT pk_asset_prices_ring PRIMARY KEY (asset_symbol, slot)
);

CREATE UNIQUE INDEX ux_asset_prices_ring_symbol_ts ON asset_prices_ring (asset_symbol, source_ts_utc);

CREATE TABLE users (
                       user_id       uuid            PRIMARY KEY DEFAULT gen_random_uuid(),
                       email         varchar(320)    NOT NULL UNIQUE,
                       password_hash varchar(255)    NOT NULL,
                       first_name    varchar(100)    NOT NULL,
                       last_name     varchar(100)    NOT NULL,
                       role          varchar(10)     NOT NULL CHECK (role IN ('USER','ADMIN')),
                       status        varchar(12)     NOT NULL CHECK (status IN ('ACTIVATED','DEACTIVATED')),
                       created_at    timestamptz     NOT NULL DEFAULT now(),
                       updated_at    timestamptz     NOT NULL DEFAULT now()
);

CREATE TABLE wallet_accounts (
                                 user_id     uuid        PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
                                 currency    char(3)     NOT NULL DEFAULT 'USD' CHECK (currency = 'USD'),
                                 created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE wallet_transactions (
                                     tx_id       uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id     uuid          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                     type        varchar(20)   NOT NULL CHECK (type IN ('DEPOSIT','WITHDRAWAL','ADJUSTMENT','INITIAL_CREDIT')),
                                     amount_usd  numeric(20,2) NOT NULL CHECK (amount_usd > 0),
                                     created_at  timestamptz   NOT NULL DEFAULT now(),
                                     note        varchar(500)
);
CREATE INDEX ix_wallet_tx_user_time ON wallet_transactions (user_id, created_at);

CREATE TABLE trades (
                        trade_id     uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
                        user_id      uuid           NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                        asset_symbol varchar(10)    NOT NULL REFERENCES assets(asset_symbol) ON DELETE RESTRICT ON UPDATE CASCADE,
                        side         varchar(4)     NOT NULL CHECK (side IN ('BUY','SELL')),
                        quantity     numeric(20,8)  NOT NULL CHECK (quantity >= 0.01 AND quantity = round(quantity, 2)),
                        price_usd    numeric(20,8)  NOT NULL,
                        executed_at  timestamptz    NOT NULL,
                        created_at   timestamptz    NOT NULL DEFAULT now()
);
CREATE INDEX ix_trades_user_time        ON trades (user_id, executed_at);
CREATE INDEX ix_trades_user_asset       ON trades (user_id, asset_symbol);

CREATE TABLE portfolio_lots (
                                lot_id             uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id            uuid           NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                asset_symbol       varchar(10)    NOT NULL REFERENCES assets(asset_symbol) ON DELETE RESTRICT ON UPDATE CASCADE,
                                buy_trade_id       uuid           NOT NULL UNIQUE REFERENCES trades(trade_id) ON DELETE RESTRICT,
                                buy_date           date           NOT NULL,
                                buy_price_usd      numeric(20,8)  NOT NULL,
                                initial_quantity   numeric(20,8)  NOT NULL CHECK (initial_quantity >= 0.01 AND initial_quantity = round(initial_quantity, 2)),
                                remaining_quantity numeric(20,8)  NOT NULL CHECK (remaining_quantity >= 0    AND remaining_quantity = round(remaining_quantity, 2) AND remaining_quantity <= initial_quantity),
                                lot_cost_usd       numeric(20,8)  NOT NULL,
                                created_at         timestamptz    NOT NULL DEFAULT now()
);
CREATE INDEX ix_lots_user_asset       ON portfolio_lots (user_id, asset_symbol);
CREATE INDEX ix_lots_user_asset_date  ON portfolio_lots (user_id, asset_symbol, buy_date);

CREATE TABLE lot_consumption (
                                 consumption_id     uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
                                 sell_trade_id      uuid           NOT NULL REFERENCES trades(trade_id) ON DELETE CASCADE,
                                 lot_id             uuid           NOT NULL REFERENCES portfolio_lots(lot_id) ON DELETE CASCADE,
                                 consumed_quantity  numeric(20,8)  NOT NULL CHECK (consumed_quantity > 0 AND consumed_quantity = round(consumed_quantity, 2)),
                                 cost_per_unit_usd  numeric(20,8)  NOT NULL,
                                 created_at         timestamptz    NOT NULL DEFAULT now(),
                                 CONSTRAINT ux_sell_lot UNIQUE (sell_trade_id, lot_id)
);
CREATE INDEX ix_lot_consumption_lot ON lot_consumption (lot_id);