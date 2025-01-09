
DELETE	FROM palautteet p1
WHERE	p1.jakson_yksiloiva_tunniste IS NULL
AND	EXISTS (
	SELECT 1 FROM palautteet p2
	WHERE	p1.hoks_id = p2.hoks_id
	AND	p1.kyselytyyppi = p2.kyselytyyppi
	AND	p2.jakson_yksiloiva_tunniste IS NULL
	-- here we should also have a condition of having the same
	-- rahoituskausi, but it's not needed yet because these have
	-- accumulated only on RK 2024-2025
	AND	p1.created_at < p2.created_at
);

DELETE	FROM palautteet p1
WHERE	EXISTS (
	SELECT 1 FROM palautteet p2
	WHERE	p1.hoks_id = p2.hoks_id
	AND	p1.jakson_yksiloiva_tunniste = p2.jakson_yksiloiva_tunniste
	AND	p1.created_at < p2.created_at
);

