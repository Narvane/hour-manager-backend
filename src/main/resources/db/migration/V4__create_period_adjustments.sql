CREATE TABLE period_adjustments
(
    period_start   DATE            NOT NULL,
    period_end     DATE            NOT NULL,
    adjusted_hours DECIMAL(10, 2)  NOT NULL DEFAULT 0,
    PRIMARY KEY (period_start, period_end)
);
