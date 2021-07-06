SELECT * FROM hoksit
    WHERE created_at >= ?
    AND created_at <= ?
    AND deleted_at IS NULL
