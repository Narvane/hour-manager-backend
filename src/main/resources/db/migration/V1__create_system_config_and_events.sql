CREATE TABLE system_config
(
    id                 UUID PRIMARY KEY,
    closure_start_day  INT          NOT NULL,
    closure_end_day    INT          NOT NULL,
    created_at         TIMESTAMP    NOT NULL
);

CREATE TABLE hour_entries
(
    id          UUID PRIMARY KEY,
    entry_date  DATE           NOT NULL,
    hours       DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE hour_adjustments
(
    id              UUID PRIMARY KEY,
    adjustment_date DATE           NOT NULL,
    delta_hours     DECIMAL(10, 2) NOT NULL,
    description     VARCHAR(500)
);

CREATE INDEX idx_hour_entries_entry_date ON hour_entries (entry_date);
CREATE INDEX idx_hour_adjustments_adjustment_date ON hour_adjustments (adjustment_date);
