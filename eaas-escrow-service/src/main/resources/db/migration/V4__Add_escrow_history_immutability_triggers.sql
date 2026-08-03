DROP TRIGGER IF EXISTS trg_prevent_escrow_history_update;
GO

CREATE TRIGGER trg_prevent_escrow_history_update
ON escrow_state_history
INSTEAD OF UPDATE
AS
BEGIN
    RAISERROR('Escrow state history is immutable and cannot be updated.', 16, 1);
END;
GO

DROP TRIGGER IF EXISTS trg_prevent_escrow_history_delete;
GO

CREATE TRIGGER trg_prevent_escrow_history_delete
ON escrow_state_history
INSTEAD OF DELETE
AS
BEGIN
    RAISERROR('Escrow state history is immutable and cannot be deleted.', 16, 1);
END;
GO
