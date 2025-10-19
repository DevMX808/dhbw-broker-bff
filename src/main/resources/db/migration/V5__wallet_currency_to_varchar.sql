ALTER TABLE broker.wallet_accounts
    ALTER COLUMN currency DROP DEFAULT;

ALTER TABLE broker.wallet_accounts
    ALTER COLUMN currency TYPE varchar(3)
        USING trim(currency);

ALTER TABLE broker.wallet_accounts
    DROP CONSTRAINT IF EXISTS wallet_accounts_currency_check;

ALTER TABLE broker.wallet_accounts
    ALTER COLUMN currency SET DEFAULT 'USD';

ALTER TABLE broker.wallet_accounts
    ADD CONSTRAINT wallet_accounts_currency_check CHECK (currency = 'USD');