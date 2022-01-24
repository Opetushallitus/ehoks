SELECT * FROM hoksit
WHERE id > ? AND ((CAST(? AS DATE) IS NULL) OR ? < updated_at)
ORDER BY id
LIMIT ?
