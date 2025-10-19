CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA broker;;

CREATE OR REPLACE FUNCTION broker.set_updated_at()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_users_updated_at ON broker.users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON broker.users
    FOR EACH ROW EXECUTE FUNCTION broker.set_updated_at();

DROP TRIGGER IF EXISTS trg_asset_last_price_updated_at ON broker.asset_last_price;
CREATE TRIGGER trg_asset_last_price_updated_at
    BEFORE UPDATE ON broker.asset_last_price
    FOR EACH ROW EXECUTE FUNCTION broker.set_updated_at();